package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.request.ActivationRequest;
import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeSummaryResponse;
import org.example.dto.response.TrainerProfileResponse;
import org.example.dto.response.TrainerTrainingResponse;
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

        Trainer trainer = new Trainer();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());

        Trainer created = gymFacade.createTrainer(trainer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(created.getUserName(), created.getPassword()));
    }


    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerByUsername(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username) {

        Trainer trainer = gymFacade.getTrainerByUsername(authUsername, authPassword, username)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + username));

        TrainerProfileResponse profile = new TrainerProfileResponse(
                null,
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization(),
                trainer.isActive(),
                mapTrainees(trainer.getTrainees())
        );

        return ResponseEntity.ok(profile);
    }


    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody UpdateTrainerRequest request) {

        Trainer trainerToUpdate = new Trainer();
        trainerToUpdate.setUserName(username);
        trainerToUpdate.setFirstName(request.firstName());
        trainerToUpdate.setLastName(request.lastName());
        trainerToUpdate.setActive(request.isActive());

        Trainer updatedTrainer = gymFacade.updateTrainer(authUsername, authPassword, trainerToUpdate);

        TrainerProfileResponse profile = new TrainerProfileResponse(
                updatedTrainer.getUserName(),
                updatedTrainer.getFirstName(),
                updatedTrainer.getLastName(),
                updatedTrainer.getSpecialization(),
                updatedTrainer.isActive(),
                mapTrainees(updatedTrainer.getTrainees())
        );

        return ResponseEntity.ok(profile);
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

        List<Training> trainings = gymFacade.getTrainerTrainings(
                authUsername, authPassword, username, periodFrom, periodTo, traineeName);

        List<TrainerTrainingResponse> response = trainings.stream()
                .map(t -> new TrainerTrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType() != null ? t.getTrainingType().trainingTypeName() : null,
                        t.getTrainingDuration(),
                        t.getTraineeName()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainer profile")
    public ResponseEntity<Void> toggleTrainerActivation(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @PathVariable @NotBlank String username,
            @Valid @RequestBody ActivationRequest request) {

        gymFacade.toggleTrainerActivation(authUsername, authPassword, username, request.isActive());
        return ResponseEntity.ok().build();
    }

    private List<TraineeSummaryResponse> mapTrainees(List<Trainee> trainees) {
        if (trainees == null) {
            return List.of();
        }
        return trainees.stream()
                .map(t -> new TraineeSummaryResponse(
                        t.getUserName(),
                        t.getFirstName(),
                        t.getLastName()))
                .toList();
    }
}