package org.example;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserCredentialsGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserCredentialsGenerator() {
    }

    public static String generateUsername(String firstName, String lastName, Collection<String> existingUsernames) {
        String baseUsername = firstName.trim() + "." + lastName.trim();
        if (existingUsernames == null || existingUsernames.isEmpty()) {
            return baseUsername;
        }

        Set<String> taken = new HashSet<>(existingUsernames);
        if (!taken.contains(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        while (taken.contains(baseUsername + suffix)) {
            suffix++;
        }
        return baseUsername + suffix;
    }

    public static String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}