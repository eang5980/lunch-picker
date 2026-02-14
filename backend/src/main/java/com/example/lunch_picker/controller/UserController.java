package com.example.lunch_picker.controller;

import com.example.lunch_picker.model.User;
import com.example.lunch_picker.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User Management", description = "APIs for retrieving pre-defined users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @Operation(
            summary = "List all pre-defined users",
            description = "Returns all users loaded from CSV file via Spring Batch. " +
                    "Only these users can create sessions."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of users retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
