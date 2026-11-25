package org.example.health;

import org.example.model.TrainingType;
import org.example.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypesHealthIndicator.class);
    private final TrainingService trainingService;

    public TrainingTypesHealthIndicator(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Override
    public Health health() {
        try {
            List<TrainingType> types = trainingService.getAllTrainingTypes();
            if (types.isEmpty()) {
                log.warn("Health check DOWN: training_types table is empty");
                return Health.down()
                        .withDetail("reason", "No training types available in database")
                        .build();
            }
            log.debug("Health check UP: found {} training types", types.size());
            return Health.up()
                    .withDetail("trainingTypesCount", types.size())
                    .build();
        } catch (Exception ex) {
            log.error("Health check DOWN: failed to query training types", ex);
            return Health.down(ex).build();
        }
    }
}