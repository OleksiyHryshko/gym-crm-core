package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeSummaryResponse;
import org.example.dto.response.TrainerProfileResponse;
import org.example.dto.response.TrainerTrainingResponse;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerTrainer_ValidRequest_Returns201Created() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Jane", "Smith", "Yoga");
        when(gymFacade.createTrainer(request)).thenReturn(new RegistrationResponse("Jane.Smith", "pass123456"));

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("Jane.Smith"))
                .andExpect(jsonPath("$.password").value("pass123456"));
    }

    @Test
    void registerTrainer_BlankSpecialization_Returns400BadRequest() throws Exception {
        TrainerRegistrationRequest invalid = new TrainerRegistrationRequest("Jane", "Smith", " ");

        mockMvc.perform(post("/api/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getTrainerByUsername_ValidAuth_Returns200WithUserName() throws Exception {
        TrainerProfileResponse profile = new TrainerProfileResponse(
                "Jane.Smith", "Jane", "Smith", "Yoga", true,
                List.of(new TraineeSummaryResponse("John.Doe", "John", "Doe"))
        );

        when(gymFacade.getTrainerByUserName("Jane.Smith", "secret", "Jane.Smith")).thenReturn(profile);

        mockMvc.perform(get("/api/trainers/Jane.Smith")
                        .header("X-Auth-Username", "Jane.Smith")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Jane.Smith"))
                .andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.trainees[0].userName").value("John.Doe"));
    }

    @Test
    void updateTrainer_ValidRequest_Returns200() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest("Jane", "Updated", true);
        TrainerProfileResponse response = new TrainerProfileResponse(
                "Jane.Smith", "Jane", "Updated", "Yoga", true, List.of());

        when(gymFacade.updateTrainer("Jane.Smith", "secret", "Jane.Smith", request)).thenReturn(response);

        mockMvc.perform(put("/api/trainers/Jane.Smith")
                        .header("X-Auth-Username", "Jane.Smith")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Jane.Smith"))
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void getTrainerTrainings_ValidRequest_Returns200() throws Exception {
        when(gymFacade.getTrainerTrainings("Jane.Smith", "secret", "Jane.Smith", null, null, null))
                .thenReturn(List.of(new TrainerTrainingResponse("Yoga Flow", LocalDate.now(), "Yoga", 60, "John Doe")));

        mockMvc.perform(get("/api/trainers/Jane.Smith/trainings")
                        .header("X-Auth-Username", "Jane.Smith")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Yoga Flow"))
                .andExpect(jsonPath("$[0].traineeName").value("John Doe"));
    }

    @Test
    void toggleTrainerActivation_ValidRequest_Returns200Ok() throws Exception {
        ActivationRequest request = new ActivationRequest(false);

        mockMvc.perform(patch("/api/trainers/Jane.Smith/status")
                        .header("X-Auth-Username", "Jane.Smith")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).toggleTrainerActivation("Jane.Smith", "secret", "Jane.Smith", request);
    }
}
