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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    void login_ValidUser_Returns200Ok() throws Exception {
        doNothing().when(gymFacade).login("John.Doe", "pass123456");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "pass123456"))
                .andExpect(status().isOk());

        verify(gymFacade).login("John.Doe", "pass123456");
    }

    @Test
    void login_InvalidCredentials_Returns401Unauthorized() throws Exception {
        doThrow(new SecurityException("Authentication failed"))
                .when(gymFacade).login("John.Doe", "bad");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Doe")
                        .param("password", "bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void changePassword_ValidRequest_Returns200Ok() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Coach.Mike", "oldPass123", "newPass12345");

        mockMvc.perform(put("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changePassword(request);
    }

    @Test
    void changePassword_InvalidCredentials_Returns401Unauthorized() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Coach.Mike", "wrongPass", "newPass12345");
        doThrow(new SecurityException("Authentication failed"))
                .when(gymFacade).changePassword(request);

        mockMvc.perform(put("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void changePassword_BlankOrShortPassword_Returns400BadRequest() throws Exception {
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest("", "oldPass", "short");

        mockMvc.perform(put("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(gymFacade);
    }
}