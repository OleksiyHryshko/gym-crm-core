package org.example.dto.request;

public record UpdateTraineeRequest(
        String firstName,
        String lastName,
        java.time.LocalDate dateOfBirth,
        String address,
        Boolean isActive
) {
}
