package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.request.ChangePasswordRequest;
import org.example.facade.GymFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Login and password management")
public class AuthController {

    private final GymFacade gymFacade;

    @Autowired
    public AuthController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }


    @GetMapping("/login")
    @Operation(summary = "Authenticate user (Trainee or Trainer)")
    public ResponseEntity<Void> login(
            @RequestParam("username") @NotBlank(message = "Username is required") String username,
            @RequestParam("password") @NotBlank(message = "Password is required") String password) {

        boolean authenticated = gymFacade.authenticateTrainee(username, password)
                || gymFacade.authenticateTrainer(username, password);

        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }


    @PutMapping("/login")
    @Operation(summary = "Change user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (gymFacade.authenticateTrainee(request.username(), request.oldPassword())) {
            gymFacade.changeTraineePassword(request.username(), request.oldPassword(), request.newPassword());
            return ResponseEntity.ok().build();
        }
        if (gymFacade.authenticateTrainer(request.username(), request.oldPassword())) {
            gymFacade.changeTrainerPassword(request.username(), request.oldPassword(), request.newPassword());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}