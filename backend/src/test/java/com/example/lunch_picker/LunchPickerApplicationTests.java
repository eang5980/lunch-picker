package com.example.lunch_picker;

import com.example.lunch_picker.dto.SubmitRestaurantRequest;
import com.example.lunch_picker.model.LunchSession;
import com.example.lunch_picker.model.SessionStatus;
import com.example.lunch_picker.model.User;
import com.example.lunch_picker.repository.SessionRepository;
import com.example.lunch_picker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LunchPickerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void contextLoads() {
    }

    @Nested
    @DisplayName("User Loading (Spring Batch)")
    class UserBatchTests {

        @Test
        @DisplayName("Pre-defined users are loaded from CSV on startup")
        void usersLoadedFromCsv() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                    .andExpect(jsonPath("$[*].username",
                            hasItems("alice", "bob", "charlie", "david", "eve")));
        }
    }

    @Nested
    @DisplayName("Session Management")
    class SessionTests {

        @Test
        @DisplayName("Pre-defined user can create a session")
        void createSession() throws Exception {
            mockMvc.perform(post("/api/sessions").param("user", "alice"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.createdBy").value("alice"))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @DisplayName("Unknown user cannot create a session")
        void unknownUserCannotCreateSession() throws Exception {
            mockMvc.perform(post("/api/sessions").param("user", "unknown_user"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Session can be retrieved by ID")
        void getSession() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/sessions").param("user", "bob"))
                    .andReturn();
            String sessionId = objectMapper.readTree(
                    result.getResponse().getContentAsString()).get("id").asText();

            mockMvc.perform(get("/api/sessions/" + sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(sessionId))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @DisplayName("Non-existent session returns 404")
        void sessionNotFound() throws Exception {
            mockMvc.perform(get("/api/sessions/non-existent-id"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Restaurant Submission")
    class RestaurantSubmissionTests {

        private String sessionId;

        @BeforeEach
        void setup() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/sessions").param("user", "alice"))
                    .andReturn();
            sessionId = objectMapper.readTree(
                    result.getResponse().getContentAsString()).get("id").asText();
        }

        @Test
        @DisplayName("User can submit a restaurant to an open session")
        void submitRestaurant() throws Exception {
            SubmitRestaurantRequest req = new SubmitRestaurantRequest();
            req.setRestaurant("McDonald's");
            req.setUser("alice");

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.restaurant").value("McDonald's"))
                    .andExpect(jsonPath("$.submittedBy").value("alice"));
        }

        @Test
        @DisplayName("Duplicate restaurant is rejected")
        void duplicateRestaurantRejected() throws Exception {
            SubmitRestaurantRequest req = new SubmitRestaurantRequest();
            req.setRestaurant("Subway");
            req.setUser("alice");

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());

            // Submit same restaurant again (case-insensitive)
            req.setRestaurant("subway");
            req.setUser("bob");
            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Empty restaurant name is rejected")
        void emptyRestaurantRejected() throws Exception {
            SubmitRestaurantRequest req = new SubmitRestaurantRequest();
            req.setRestaurant("   ");
            req.setUser("alice");

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Multiple users can submit different restaurants")
        void multipleUsersSubmit() throws Exception {
            SubmitRestaurantRequest req1 = new SubmitRestaurantRequest();
            req1.setRestaurant("Pizza Hut");
            req1.setUser("alice");

            SubmitRestaurantRequest req2 = new SubmitRestaurantRequest();
            req2.setRestaurant("KFC");
            req2.setUser("bob");

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req2)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/sessions/" + sessionId))
                    .andExpect(jsonPath("$.restaurants", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("Random Pick")
    class RandomPickTests {

        private String sessionId;

        @BeforeEach
        void setup() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/sessions").param("user", "alice"))
                    .andReturn();
            sessionId = objectMapper.readTree(
                    result.getResponse().getContentAsString()).get("id").asText();

            SubmitRestaurantRequest req1 = new SubmitRestaurantRequest();
            req1.setRestaurant("Restaurant A");
            req1.setUser("alice");
            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req1)));

            SubmitRestaurantRequest req2 = new SubmitRestaurantRequest();
            req2.setRestaurant("Restaurant B");
            req2.setUser("bob");
            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req2)));
        }

        @Test
        @DisplayName("First submitter can pick a random restaurant")
        void firstSubmitterCanPick() throws Exception {
            mockMvc.perform(post("/api/sessions/" + sessionId + "/pick")
                            .param("user", "alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chosenRestaurant",
                            anyOf(is("Restaurant A"), is("Restaurant B"))));
        }

        @Test
        @DisplayName("Non-first submitter cannot pick")
        void nonFirstSubmitterCannotPick() throws Exception {
            mockMvc.perform(post("/api/sessions/" + sessionId + "/pick")
                            .param("user", "bob"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Session is closed after pick, no more submissions allowed")
        void sessionClosedAfterPick() throws Exception {
            mockMvc.perform(post("/api/sessions/" + sessionId + "/pick")
                    .param("user", "alice"));

            SubmitRestaurantRequest req = new SubmitRestaurantRequest();
            req.setRestaurant("Restaurant C");
            req.setUser("charlie");

            mockMvc.perform(post("/api/sessions/" + sessionId + "/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Picking on empty session fails")
        void pickOnEmptySessionFails() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/sessions").param("user", "charlie"))
                    .andReturn();
            String emptySessionId = objectMapper.readTree(
                    result.getResponse().getContentAsString()).get("id").asText();

            mockMvc.perform(post("/api/sessions/" + emptySessionId + "/pick")
                            .param("user", "charlie"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Pick on already-closed session returns same result")
        void pickOnClosedSessionReturnsSameResult() throws Exception {
            MvcResult firstPick = mockMvc.perform(post("/api/sessions/" + sessionId + "/pick")
                            .param("user", "alice"))
                    .andReturn();
            String firstChoice = objectMapper.readTree(
                    firstPick.getResponse().getContentAsString()).get("chosenRestaurant").asText();

            MvcResult secondPick = mockMvc.perform(post("/api/sessions/" + sessionId + "/pick")
                            .param("user", "alice"))
                    .andReturn();
            String secondChoice = objectMapper.readTree(
                    secondPick.getResponse().getContentAsString()).get("chosenRestaurant").asText();

            assert firstChoice.equals(secondChoice);
        }
    }

    @Nested
    @DisplayName("Session Isolation")
    class SessionIsolationTests {

        @Test
        @DisplayName("Restaurants are scoped to their session")
        void restaurantsAreScopedToSession() throws Exception {
            // Create two sessions
            MvcResult result1 = mockMvc.perform(post("/api/sessions").param("user", "alice"))
                    .andReturn();
            String session1 = objectMapper.readTree(
                    result1.getResponse().getContentAsString()).get("id").asText();

            MvcResult result2 = mockMvc.perform(post("/api/sessions").param("user", "bob"))
                    .andReturn();
            String session2 = objectMapper.readTree(
                    result2.getResponse().getContentAsString()).get("id").asText();

            // Submit to session 1
            SubmitRestaurantRequest req = new SubmitRestaurantRequest();
            req.setRestaurant("Nando's");
            req.setUser("alice");
            mockMvc.perform(post("/api/sessions/" + session1 + "/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));

            // Session 2 should have no restaurants
            mockMvc.perform(get("/api/sessions/" + session2))
                    .andExpect(jsonPath("$.restaurants", hasSize(0)));

            // Session 1 should have 1 restaurant
            mockMvc.perform(get("/api/sessions/" + session1))
                    .andExpect(jsonPath("$.restaurants", hasSize(1)));
        }
    }
}
