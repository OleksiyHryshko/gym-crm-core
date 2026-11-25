package org.example.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitialDataFileHealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void health_WhenFileExistsAndReadable_ReturnsUp() throws Exception {
        Path file = tempDir.resolve("initial_data.csv");
        Files.writeString(file, "type,id\nTRAINER,1\n");

        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator(file.toString());
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(file.toString(), health.getDetails().get("filePath"));
    }

    @Test
    void health_WhenFileDoesNotExist_ReturnsDown() {
        Path missing = tempDir.resolve("missing.csv");

        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator(missing.toString());
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void health_WhenPathIsDirectory_ReturnsDown() {
        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator(tempDir.toString());
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}