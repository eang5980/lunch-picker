package com.example.lunch_picker.dto;

import com.example.lunch_picker.model.RestaurantChoice;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantChoiceResponse {
    private Long id;
    private String restaurant;
    private String submittedBy;

    public static RestaurantChoiceResponse from(RestaurantChoice choice) {
        return RestaurantChoiceResponse.builder()
                .id(choice.getId())
                .restaurant(choice.getRestaurant())
                .submittedBy(choice.getSubmittedBy())
                .build();
    }
}
