package com.example.lunch_picker.dto;

import com.example.lunch_picker.model.LunchSession;
import com.example.lunch_picker.model.RestaurantChoice;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SessionResponse {
    private String id;
    private String createdBy;
    private String status;
    private String chosenRestaurant;
    private LocalDateTime createdAt;
    private List<RestaurantChoiceResponse> restaurants;

    public static SessionResponse from(LunchSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .createdBy(session.getCreatedBy())
                .status(session.getStatus().name())
                .chosenRestaurant(session.getChosenRestaurant())
                .createdAt(session.getCreatedAt())
                .restaurants(session.getRestaurantChoices() != null
                        ? session.getRestaurantChoices().stream()
                            .map(RestaurantChoiceResponse::from)
                            .toList()
                        : List.of())
                .build();
    }
}
