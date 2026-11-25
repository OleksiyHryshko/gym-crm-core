package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.CreateTrainingRequest;
import org.example.dto.response.TrainingTypeResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void addTraining_PresentOrFutureDate_Returns200Ok() throws Exception {
        CreateTrainingRequest request = new CreateTrainingRequest(
                "John.Doe", "Jane.Smith", "Morning Cardio", LocalDate.now(), 45);

        mockMvc.perform(post("/api/trainings")
                        .header("X-Auth-Username", "John.Doe")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).createTraining("John.Doe", "secret", request);
    }

    @Test
    void addTraining_PastDate_Returns400BadRequest() throws Exception {
        CreateTrainingRequest pastRequest = new CreateTrainingRequest(
                "John.Doe", "Jane.Smith", "Old Session", LocalDate.now().minusDays(1), 45);

        mockMvc.perform(post("/api/trainings")
                        .header("X-Auth-Username", "John.Doe")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void addTraining_NonPositiveDuration_Returns400BadRequest() throws Exception {
        CreateTrainingRequest invalidRequest = new CreateTrainingRequest(
                "John.Doe", "Jane.Smith", "Morning Cardio", LocalDate.now().plusDays(1), 0);

        mockMvc.perform(post("/api/trainings")
                        .header("X-Auth-Username", "John.Doe")
                        .header("X-Auth-Password", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getTrainingTypes_ValidAuth_Returns200Ok() throws Exception {
        when(gymFacade.getAllTrainingTypes("John.Doe", "secret"))
                .thenReturn(List.of(new TrainingTypeResponse(1L, "Fitness")));

        mockMvc.perform(get("/api/trainings/types")
                        .header("X-Auth-Username", "John.Doe")
                        .header("X-Auth-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].trainingType").value("Fitness"));
    }
}