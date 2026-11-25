package org.example.dto.response;

public record TrainerSummaryResponse(
        String userName,
        String firstName,
        String lastName,
        String specialization
) {}