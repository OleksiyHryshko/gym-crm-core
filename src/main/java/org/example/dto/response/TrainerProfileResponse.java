package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrainerProfileResponse(
        String userName,
        String firstName,
        String lastName,
        String specialization,
        boolean isActive,
        List<TraineeSummaryResponse> trainees
) {}