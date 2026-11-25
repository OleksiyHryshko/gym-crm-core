package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.request.UpdateTraineeTrainersRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeProfileResponse;
import org.example.dto.response.TrainerSummaryResponse;
import org.example.dto.response.TrainingResponse;
import org.example.exception.GlobalExceptionHandler;
import org.example.facade.GymFacade;
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
import java.util.NoSuchElementException;

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

        when(gymFacade.createTrainee(request))
                .thenReturn(new RegistrationResponse("Oleksii.Hryshko", "pass123456"));

        mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("Oleksii.Hryshko"))
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
    void getTraineeByUsername_ValidAuth_Returns200AndProfileWithUserName() throws Exception {
        TraineeProfileResponse profile = new TraineeProfileResponse(
                "Oleksii",
                "Hryshko",
                "Oleksii.Hryshko",
                true,
                LocalDate.of(2007, 3, 21),
                "Gliwice",
                List.of(new TrainerSummaryResponse("John.Coach", "John", "Coach", "Fitness"))
        );

        when(gymFacade.getTraineeByUserName("Oleksii.Hryshko", "secret", "Oleksii.Hryshko"))
                .thenReturn(profile);

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Oleksii.Hryshko"))
                .andExpect(jsonPath("$.firstName").value("Oleksii"))
                .andExpect(jsonPath("$.trainers[0].userName").value("John.Coach"))
                .andExpect(jsonPath("$.trainers[0].firstName").value("John"))
                .andExpect(jsonPath("$.trainers[0].lastName").value("Coach"))
                .andExpect(jsonPath("$.trainers[0].specialization").value("Fitness"));
    }

    @Test
    void getTraineeByUsername_InvalidCredentials_Returns401Unauthorized() throws Exception {
        when(gymFacade.getTraineeByUserName("Oleksii.Hryshko", "wrong", "Oleksii.Hryshko"))
                .thenThrow(new SecurityException("Authentication failed"));

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void getTraineeByUsername_NotFound_Returns404NotFound() throws Exception {
        when(gymFacade.getTraineeByUserName("Oleksii.Hryshko", "secret", "Unknown.User"))
                .thenThrow(new NoSuchElementException("Trainee not found: Unknown.User"));

        mockMvc.perform(get("/api/trainees/Unknown.User")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTrainee_ValidRequest_Returns200WithUserName() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "Oleksii", "Hryshko", true, LocalDate.of(2007, 3, 21), "Warsaw");

        TraineeProfileResponse updated = new TraineeProfileResponse(
                "Oleksii", "Hryshko", "Oleksii.Hryshko", true,
                LocalDate.of(2007, 3, 21), "Warsaw", List.of());

        when(gymFacade.updateTrainee("Oleksii.Hryshko", "secret", "Oleksii.Hryshko", request))
                .thenReturn(updated);

        mockMvc.perform(put("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Oleksii.Hryshko"))
                .andExpect(jsonPath("$.address").value("Warsaw"));
    }

    @Test
    void updateTrainee_InvalidFields_Returns400BadRequest() throws Exception {
        UpdateTraineeRequest invalidRequest = new UpdateTraineeRequest(
                "", " ", null, LocalDate.now().plusDays(1), "Warsaw");

        mockMvc.perform(put("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void deleteTrainee_ValidRequest_Returns200Ok() throws Exception {
        doNothing().when(gymFacade).deleteTraineeByUserName("Oleksii.Hryshko", "secret", "Oleksii.Hryshko");

        mockMvc.perform(delete("/api/trainees/Oleksii.Hryshko")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk());

        verify(gymFacade).deleteTraineeByUserName("Oleksii.Hryshko", "secret", "Oleksii.Hryshko");
    }

    @Test
    void getUnassignedTrainers_ValidRequest_Returns200() throws Exception {
        when(gymFacade.getUnassignedTrainers("Oleksii.Hryshko", "secret", "Oleksii.Hryshko"))
                .thenReturn(List.of(new TrainerSummaryResponse("John.Coach", "John", "Coach", "Fitness")));

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko/unassigned-trainers")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("John.Coach"));
    }

    @Test
    void updateTraineeTrainers_ValidRequest_Returns200() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of("John.Coach"));
        when(gymFacade.updateTraineeTrainersList("Oleksii.Hryshko", "secret", "Oleksii.Hryshko", request))
                .thenReturn(List.of(new TrainerSummaryResponse("John.Coach", "John", "Coach", "Fitness")));

        mockMvc.perform(put("/api/trainees/Oleksii.Hryshko/trainers")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("John.Coach"));
    }

    @Test
    void getTraineeTrainings_ValidRequest_Returns200() throws Exception {
        when(gymFacade.getTraineeTrainings("Oleksii.Hryshko", "secret", "Oleksii.Hryshko",
                null, null, null, null))
                .thenReturn(List.of(new TrainingResponse("HIIT", LocalDate.now(), "Cardio", 45, "John Coach")));

        mockMvc.perform(get("/api/trainees/Oleksii.Hryshko/trainings")
                        .header("X-Auth-Username", "Oleksii.Hryshko")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("HIIT"));
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

        verify(gymFacade).toggleTraineeActivation("Oleksii.Hryshko", "secret", "Oleksii.Hryshko", request);
    }
}