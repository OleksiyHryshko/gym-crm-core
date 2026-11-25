package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TraineeProfileResponse(
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        LocalDate dateOfBirth,
        String address,
        List<TrainerSummaryResponse> trainers
) {}