package org.example.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUserName,
        @NotBlank(message = "Trainer username is required") String trainerUserName,
        @NotBlank(message = "Training name is required") String trainingName,
        @NotNull(message = "Training date is required")
        @FutureOrPresent(message = "Training date must be today or in the future")
        LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive") Integer trainingDuration
) {}