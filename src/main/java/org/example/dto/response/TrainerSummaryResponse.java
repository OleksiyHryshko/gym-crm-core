package org.example.dto.response;

public record TrainerSummaryResponse (
    String firstName,
    String lastName,
    String userName,
    String specialization
) {
}