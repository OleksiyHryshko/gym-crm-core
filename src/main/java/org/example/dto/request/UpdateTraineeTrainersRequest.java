package org.example.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotEmpty(message = "Trainers list cannot be empty")
        List<String> trainerUsernames
) {}