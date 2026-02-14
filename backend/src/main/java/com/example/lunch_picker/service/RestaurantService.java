package com.example.lunch_picker.service;

import com.example.lunch_picker.model.LunchSession;
import com.example.lunch_picker.model.RestaurantChoice;
import com.example.lunch_picker.model.SessionStatus;
import com.example.lunch_picker.repository.RestaurantRepository;
import com.example.lunch_picker.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SessionRepository sessionRepository;

    /**
     * Submits a restaurant choice to a session.
     * Validates that the session is open and the restaurant hasn't been submitted yet.
     *
     * @param sessionId  The session ID
     * @param restaurant The restaurant name
     * @param user       The user submitting the choice
     * @return The created RestaurantChoice
     * @throws IllegalArgumentException if session not found or restaurant name is empty
     * @throws IllegalStateException    if session is closed or restaurant already exists
     */
    @Transactional
    public RestaurantChoice submit(String sessionId, String restaurant, String user) {
        log.debug("Submitting restaurant '{}' to session '{}' by user '{}'", restaurant, sessionId, user);
        
        LunchSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new IllegalStateException("Session is closed. No further submissions allowed.");
        }

        String trimmedRestaurant = restaurant.trim();
        if (trimmedRestaurant.isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty");
        }

        if (restaurantRepository.existsBySessionIdAndRestaurantIgnoreCase(sessionId, trimmedRestaurant)) {
            throw new IllegalStateException("This restaurant has already been submitted in this session");
        }

        RestaurantChoice choice = RestaurantChoice.builder()
                .session(session)
                .restaurant(trimmedRestaurant)
                .submittedBy(user)
                .build();

        RestaurantChoice saved = restaurantRepository.save(choice);
        log.info("Restaurant '{}' submitted successfully to session '{}' by user '{}'", 
                 trimmedRestaurant, sessionId, user);
        
        return saved;
    }

    /**
     * Picks a random restaurant from the submitted choices and closes the session.
     * Uses optimistic locking (@Version) to prevent race conditions in distributed systems.
     * Only the first submitter can trigger the random pick (Stretch Goal 1).
     *
     * @param sessionId The session ID
     * @param user      The user requesting the pick
     * @return The chosen restaurant name
     * @throws IllegalArgumentException          if session not found
     * @throws IllegalStateException             if no restaurants submitted
     * @throws SecurityException                 if user is not the first submitter
     * @throws OptimisticLockingFailureException if session was modified by another request
     */
    @Transactional
    public String pickRandom(String sessionId, String user) {
        log.debug("Picking random restaurant for session '{}' by user '{}'", sessionId, user);
        
        LunchSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // If already closed, return the previously chosen restaurant (idempotent operation)
        if (session.getStatus() == SessionStatus.CLOSED) {
            log.info("Session '{}' already closed, returning existing choice: '{}'", 
                     sessionId, session.getChosenRestaurant());
            return session.getChosenRestaurant();
        }

        List<RestaurantChoice> choices =
                restaurantRepository.findBySessionIdOrderByIdAsc(sessionId);

        if (choices.isEmpty()) {
            throw new IllegalStateException("No restaurants have been submitted yet");
        }

        // Stretch Goal 1: Only the first submitter can trigger the random pick
        RestaurantChoice firstSubmission = choices.get(0);
        if (!firstSubmission.getSubmittedBy().equals(user)) {
            throw new SecurityException(
                    "Only the first submitter (" + firstSubmission.getSubmittedBy()
                            + ") can pick the random restaurant");
        }

        // Random selection
        RestaurantChoice chosen =
                choices.get(ThreadLocalRandom.current().nextInt(choices.size()));

        // Update session - optimistic locking will throw exception if version mismatch
        session.setChosenRestaurant(chosen.getRestaurant());
        session.setStatus(SessionStatus.CLOSED);
        
        try {
            sessionRepository.save(session);
            log.info("Session '{}' closed with chosen restaurant: '{}'", sessionId, chosen.getRestaurant());
        } catch (OptimisticLockingFailureException e) {
            log.error("Concurrent modification detected for session '{}'", sessionId);
            throw new IllegalStateException(
                "Session was modified by another request. Please try again.", e);
        }

        return chosen.getRestaurant();
    }
}
