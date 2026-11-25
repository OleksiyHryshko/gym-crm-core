package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TrainerProfileResponse;
import org.example.dto.response.TrainerTrainingResponse;
import org.example.facade.GymFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Validated
@Tag(name = "Trainer Management", description = "Endpoints for trainer registration, profile, trainings, and status")
public class TrainerController {

    private final GymFacade gymFacade;

    @Autowired
    public TrainerController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer")
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gymFacade.createTrainer(request));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerByUsername(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {
        return ResponseEntity.ok(gymFacade.getTrainerByUserName(authUsername, authPassword, username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTrainerRequest request) {
        return ResponseEntity.ok(gymFacade.updateTrainer(authUsername, authPassword, username, request));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings list with optional filters")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(gymFacade.getTrainerTrainings(
                authUsername, authPassword, username, periodFrom, periodTo, traineeName));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainer profile")
    public ResponseEntity<Void> toggleTrainerActivation(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody ActivationRequest request) {
        gymFacade.toggleTrainerActivation(authUsername, authPassword, username, request);
        return ResponseEntity.ok().build();
    }
}