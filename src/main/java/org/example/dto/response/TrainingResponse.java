package org.example.dto.response;

import java.time.LocalDate;

public record TrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Number trainingDuration,
        String trainerName
) {}