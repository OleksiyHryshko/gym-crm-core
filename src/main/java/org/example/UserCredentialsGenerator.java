package org.example;

import org.example.model.User;

import java.security.SecureRandom;
import java.util.Collection;

public final class UserCredentialsGenerator {

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserCredentialsGenerator() {
    }

    public static String generateUsername(String firstName, String lastName, Collection<? extends User> existingUsers) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;
        while (isUsernameTaken(username, existingUsers)) {
            username = baseUsername + suffix++;
        }
        return username;
    }

    private static boolean isUsernameTaken(String username, Collection<? extends User> existingUsers) {
        return existingUsers.stream().anyMatch(user -> username.equals(user.getUserName()));
    }

    public static String generatePassword() {
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(PASSWORD_CHARACTERS.charAt(RANDOM.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }
}
