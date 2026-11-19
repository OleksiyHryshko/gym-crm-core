package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.exception.GlobalExceptionHandler;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerTrainee_ValidRequest_Returns201Created() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "Oleksii", "Hryshko", LocalDate.of(2007, 3, 21), "Gliwice");

        Trainee created = new Trainee();
        created.setUserName("Oleksii.Hryshko");
        created.setPassword("pass123456");

        when(gymFacade.createTrainee(any(Trainee.class))).thenReturn(created);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Oleksii.Hryshko"))
                .andExpect(jsonPath("$.password").value("pass123456"));
    }

    @Test
    void registerTrainee_BlankFirstName_Returns400BadRequest() throws Exception {
        TraineeRegistrationRequest invalidRequest = new TraineeRegistrationRequest(
                "", "Hryshko", null, null);

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getTraineeByUsername_ValidAuth_Returns200AndProfile() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setUserName("John.Coach");
        trainer.setFirstName("John");
        trainer.setLastName("Coach");
        trainer.setSpecialization("Fitness");

        Trainee trainee = new Trainee();
        trainee.setUserName("Oleksii.Hryshko");
        trainee.setFirstName("Oleksii");
        trainee.setLastName("Hryshko");
        trainee.setActive(true);
        trainee.setTrainers(List.of(trainer));

        when(gymFacade.getTraineeByUsername("Oleksii.Hryshko", "secret", "Oleksii.Hryshko"))
                .thenReturn(Optional.of(trainee));

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.firstName").value("Oleksii"))
                .andExpect(jsonPath("$.trainers[0].username").value("John.Coach"));
    }

    @Test
    void getTraineeByUsername_InvalidCredentials_Returns401Unauthorized() throws Exception {
        when(gymFacade.getTraineeByUsername("Oleksii.Hryshko", "wrong", "Oleksii.Hryshko"))
                .thenThrow(new SecurityException("Authentication failed"));

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void getTraineeByUsername_NotFound_Returns404NotFound() throws Exception {
        when(gymFacade.getTraineeByUsername("Oleksii.Hryshko", "secret", "Unknown.User"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainees/Unknown.User")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTrainee_ValidRequest_Returns200WithUsername() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "Oleksii", "Hryshko", LocalDate.of(2007, 3, 21), "Warsaw", true);

        Trainee updated = new Trainee();
        updated.setUserName("Oleksii.Hryshko");
        updated.setFirstName("Oleksii");
        updated.setLastName("Hryshko");
        updated.setAddress("Warsaw");
        updated.setActive(true);

        when(gymFacade.updateTrainee(eq("Oleksii.Hryshko"), eq("secret"), any(Trainee.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Oleksii.Hryshko"))
                .andExpect(jsonPath("$.address").value("Warsaw"));
    }

    @Test
    void deleteTrainee_ValidRequest_Returns200Ok() throws Exception {
        doNothing().when(gymFacade).deleteTraineeByUsername("Oleksii.Hryshko", "secret", "Oleksii.Hryshko");

        mockMvc.perform(delete("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk());

        verify(gymFacade).deleteTraineeByUsername("Oleksii.Hryshko", "secret", "Oleksii.Hryshko");
    }

    @Test
    void toggleTraineeActivation_ValidRequest_Returns200Ok() throws Exception {
        ActivationRequest request = new ActivationRequest(false);

        mockMvc.perform(patch("/api/trainees/Oleksii.Hryshko/status")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).toggleTraineeActivation("Oleksii.Hryshko", "secret", "Oleksii.Hryshko", false);
    }
}