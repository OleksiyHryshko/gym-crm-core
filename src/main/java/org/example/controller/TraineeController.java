package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.request.UpdateTraineeTrainersRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeProfileResponse;
import org.example.dto.response.TrainerSummaryResponse;
import org.example.dto.response.TrainingResponse;
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
@RequestMapping("/api/trainees")
@Validated
@Tag(name = "Trainee Management", description = "Endpoints for trainee registration, profile, trainers, and trainings")
public class TraineeController {

    private final GymFacade gymFacade;

    @Autowired
    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainee")
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gymFacade.createTrainee(request));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getTraineeByUsername(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {
        return ResponseEntity.ok(gymFacade.getTraineeByUserName(authUsername, authPassword, username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTrainee(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        return ResponseEntity.ok(gymFacade.updateTrainee(authUsername, authPassword, username, request));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile and cascade delete trainings")
    public ResponseEntity<Void> deleteTrainee(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {
        gymFacade.deleteTraineeByUserName(authUsername, authPassword, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get active trainers not assigned to the trainee")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {
        return ResponseEntity.ok(gymFacade.getUnassignedTrainers(authUsername, authPassword, username));
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's assigned trainers list")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTraineeTrainers(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        return ResponseEntity.ok(gymFacade.updateTraineeTrainersList(authUsername, authPassword, username, request));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee trainings list with optional filters")
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(gymFacade.getTraineeTrainings(
                authUsername, authPassword, username, periodFrom, periodTo, trainerName, trainingType));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainee profile")
    public ResponseEntity<Void> toggleTraineeActivation(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody ActivationRequest request) {
        gymFacade.toggleTraineeActivation(authUsername, authPassword, username, request);
        return ResponseEntity.ok().build();
    }
}