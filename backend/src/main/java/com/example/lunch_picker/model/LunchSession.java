package com.example.lunch_picker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a lunch decision session.
 * Each session has a unique ID, creator, status, and list of restaurant choices.
 * Uses optimistic locking to prevent race conditions in distributed systems.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "restaurantChoices")
@EqualsAndHashCode(of = "id")
public class LunchSession {

    @Id
    private String id;

    @Column(nullable = false)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    private String chosenRestaurant;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optimistic locking version field.
     * Prevents race conditions when multiple users try to pick simultaneously.
     * IMPORTANT: This replaces synchronized method for distributed systems.
     */
    @Version
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RestaurantChoice> restaurantChoices = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
