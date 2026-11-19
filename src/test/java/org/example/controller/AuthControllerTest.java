package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.ChangePasswordRequest;
import org.example.exception.GlobalExceptionHandler;
import org.example.facade.GymFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_ValidTrainee_Returns200Ok() throws Exception {
        when(gymFacade.authenticateTrainee("John.Doe", "pass123")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "pass123"))
                .andExpect(status().isOk());
    }

    @Test
    void login_InvalidCredentials_Returns401Unauthorized() throws Exception {
        when(gymFacade.authenticateTrainee("John.Doe", "bad")).thenReturn(false);
        when(gymFacade.authenticateTrainer("John.Doe", "bad")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_ValidTrainer_Returns200Ok() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Coach.Mike", "oldPass", "newPass");
        when(gymFacade.authenticateTrainee("Coach.Mike", "oldPass")).thenReturn(false);
        when(gymFacade.authenticateTrainer("Coach.Mike", "oldPass")).thenReturn(true);

        mockMvc.perform(put("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changeTrainerPassword("Coach.Mike", "oldPass", "newPass");
    }
}