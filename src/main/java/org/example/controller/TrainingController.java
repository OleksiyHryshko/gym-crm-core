package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.request.CreateTrainingRequest;
import org.example.dto.response.TrainingTypeResponse;
import org.example.facade.GymFacade;
import org.example.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training Management", description = "Endpoints for adding trainings and retrieving training types")
public class TrainingController {

    private final GymFacade gymFacade;

    @Autowired
    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }


    @PostMapping
    @Operation(summary = "Add a new training session")
    public ResponseEntity<Void> addTraining(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword,
            @Valid @RequestBody CreateTrainingRequest request) {

        Training training = new Training();
        training.setTraineeUsername(request.traineeUsername());
        training.setTrainerUsername(request.trainerUsername());
        training.setTrainingName(request.trainingName());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());

        gymFacade.createTraining(authUsername, authPassword, training);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/types")
    @Operation(summary = "Get all available training types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes(
            @RequestHeader("X-Auth-Username") String authUsername,
            @RequestHeader("X-Auth-Password") String authPassword) {

        List<TrainingTypeResponse> types = gymFacade.getAllTrainingTypes(authUsername, authPassword)
                .stream()
                .map(tt -> new TrainingTypeResponse(tt.id(), tt.trainingTypeName()))
                .toList();

        return ResponseEntity.ok(types);
    }
}