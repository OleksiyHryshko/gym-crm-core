package org.example.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class InitialDataFileHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(InitialDataFileHealthIndicator.class);
    private final Path dataFilePath;

    public InitialDataFileHealthIndicator(@Value("${data.file.path}") String dataFilePath) {
        this.dataFilePath = Path.of(dataFilePath);
    }

    @Override
    public Health health() {
        try {
            if (Files.isRegularFile(dataFilePath) && Files.isReadable(dataFilePath)) {
                log.debug("Health check UP: data file '{}' is accessible", dataFilePath);
                return Health.up()
                        .withDetail("filePath", dataFilePath.toString())
                        .build();
            }
            log.warn("Health check DOWN: data file '{}' does not exist or is not readable", dataFilePath);
            return Health.down()
                    .withDetail("filePath", dataFilePath.toString())
                    .withDetail("reason", "File does not exist or is not readable")
                    .build();
        } catch (Exception ex) {
            log.error("Health check DOWN: error checking data file '{}'", dataFilePath, ex);
            return Health.down(ex)
                    .withDetail("filePath", dataFilePath.toString())
                    .build();
        }
    }
}