package org.example.dto.request;

public record ChangePasswordRequest(
        String username,
        String oldPassword,
        String newPassword
) {
}
