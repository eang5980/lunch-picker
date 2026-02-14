package com.example.lunch_picker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitRestaurantRequest {
    @NotBlank(message = "Restaurant name is required")
    private String restaurant;

    @NotBlank(message = "User name is required")
    private String user;
}
