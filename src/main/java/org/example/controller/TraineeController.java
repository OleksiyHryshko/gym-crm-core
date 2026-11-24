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
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(gymFacade.createTrainee(request));
    }


    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getTraineeByUsername(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {

        Trainee trainee = gymFacade.getTraineeByUsername(authUsername, authPassword, username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + username));

        TraineeProfileResponse profile = new TraineeProfileResponse(
                null,
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                mapTrainers(trainee.getTrainers())
        );

        return ResponseEntity.ok(profile);
    }


    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTrainee(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTraineeRequest request) {

        Trainee traineeToUpdate = new Trainee();
        traineeToUpdate.setUserName(username);
        traineeToUpdate.setFirstName(request.firstName());
        traineeToUpdate.setLastName(request.lastName());
        traineeToUpdate.setDateOfBirth(request.dateOfBirth());
        traineeToUpdate.setAddress(request.address());
        traineeToUpdate.setActive(request.isActive());

        Trainee updatedTrainee = gymFacade.updateTrainee(authUsername, authPassword, traineeToUpdate);

        TraineeProfileResponse profile = new TraineeProfileResponse(
                updatedTrainee.getUserName(),
                updatedTrainee.getFirstName(),
                updatedTrainee.getLastName(),
                updatedTrainee.getDateOfBirth(),
                updatedTrainee.getAddress(),
                updatedTrainee.isActive(),
                mapTrainers(updatedTrainee.getTrainers())
        );

        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile and cascade delete trainings")
    public ResponseEntity<Void> deleteTrainee(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {

        gymFacade.deleteTraineeByUsername(authUsername, authPassword, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get active trainers not assigned to the trainee")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {

        List<Trainer> unassigned = gymFacade.getUnassignedTrainers(authUsername, authPassword, username);
        return ResponseEntity.ok(mapTrainers(unassigned));
    }


    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's assigned trainers list")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTraineeTrainers(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        List<Trainer> updatedTrainers = gymFacade.updateTraineeTrainersList(
                authUsername, authPassword, username, request.trainerUsernames());
        return ResponseEntity.ok(mapTrainers(updatedTrainers));
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

        List<Training> trainings = gymFacade.getTraineeTrainings(
                authUsername, authPassword, username, periodFrom, periodTo, trainerName, trainingType);

        List<TrainingResponse> response = trainings.stream()
                .map(t -> new TrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType() != null ? t.getTrainingType().trainingTypeName() : null,
                        t.getTrainingDuration(),
                        t.getTrainerName()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainee profile")
    public ResponseEntity<Void> toggleTraineeActivation(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody ActivationRequest request) {

        gymFacade.toggleTraineeActivation(authUsername, authPassword, username, request.isActive());
        return ResponseEntity.ok().build();
    }

    private List<TrainerSummaryResponse> mapTrainers(List<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }
        return trainers.stream()
                .map(t -> new TrainerSummaryResponse(
                        t.getUserName(),
                        t.getFirstName(),
                        t.getLastName(),
                        t.getSpecialization()))
                .toList();
    }
}