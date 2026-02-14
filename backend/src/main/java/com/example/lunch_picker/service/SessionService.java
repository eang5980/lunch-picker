package com.example.lunch_picker.service;

import com.example.lunch_picker.model.LunchSession;
import com.example.lunch_picker.model.SessionStatus;
import com.example.lunch_picker.repository.SessionRepository;
import com.example.lunch_picker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public LunchSession createSession(String username) {
        if (!userRepository.existsById(username)) {
            throw new SecurityException("User '" + username + "' is not authorized to create sessions");
        }

        LunchSession session = LunchSession.builder()
                .id(UUID.randomUUID().toString())
                .createdBy(username)
                .status(SessionStatus.OPEN)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public LunchSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
}