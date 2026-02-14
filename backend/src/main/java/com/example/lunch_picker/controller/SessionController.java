package com.example.lunch_picker.controller;

import com.example.lunch_picker.dto.RestaurantChoiceResponse;
import com.example.lunch_picker.dto.SessionResponse;
import com.example.lunch_picker.dto.SubmitRestaurantRequest;
import com.example.lunch_picker.model.LunchSession;
import com.example.lunch_picker.model.RestaurantChoice;
import com.example.lunch_picker.service.RestaurantService;
import com.example.lunch_picker.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Session Management", description = "APIs for creating and managing lunch decision sessions")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final RestaurantService restaurantService;

    @Operation(
            summary = "Create a new lunch session",
            description = "Creates a new session for restaurant selection. Only pre-defined users (loaded from CSV) are authorized to create sessions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Session created successfully",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not authorized to create sessions",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "Username of the session creator (must be pre-defined user)", required = true)
            @RequestParam String user) {
        LunchSession session = sessionService.createSession(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionResponse.from(session));
    }

    @Operation(
            summary = "Get session details",
            description = "Retrieves session information including all submitted restaurant choices"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Session found",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "Unique session identifier (UUID)", required = true)
            @PathVariable String id) {
        LunchSession session = sessionService.getSession(id);
        return ResponseEntity.ok(SessionResponse.from(session));
    }

    @Operation(
            summary = "Submit a restaurant choice",
            description = "Submits a restaurant to the session. Any user (including guests) can submit. Duplicate restaurants are rejected."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Restaurant submitted successfully",
                    content = @Content(schema = @Schema(implementation = RestaurantChoiceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (blank restaurant name)",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Session is closed or restaurant already submitted",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @PostMapping("/{id}/restaurants")
    public ResponseEntity<RestaurantChoiceResponse> submitRestaurant(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Restaurant submission details", required = true)
            @Valid @RequestBody SubmitRestaurantRequest request) {
        RestaurantChoice choice = restaurantService.submit(id, request.getRestaurant(), request.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestaurantChoiceResponse.from(choice));
    }

    @Operation(
            summary = "Pick a random restaurant",
            description = "Randomly selects a restaurant from submitted choices and closes the session. " +
                    "Only the user who submitted the FIRST restaurant can trigger this action (Stretch Goal 1)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant picked successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the first submitter can pick",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "No restaurants submitted yet",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @PostMapping("/{id}/pick")
    public ResponseEntity<Map<String, String>> pickRandom(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Username requesting the pick (must be first submitter)", required = true)
            @RequestParam String user) {
        String chosen = restaurantService.pickRandom(id, user);
        return ResponseEntity.ok(Map.of("chosenRestaurant", chosen));
    }
}
