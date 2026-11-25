package org.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private static final Logger log = LoggerFactory.getLogger(GymMetrics.class);

    private final Counter trainingsCreatedCounter;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;


    public GymMetrics(MeterRegistry meterRegistry) {
        this.trainingsCreatedCounter = Counter.builder("gym.trainings.created")
                .description("Total number of created training sessions")
                .register(meterRegistry);

        this.authSuccessCounter = Counter.builder("gym.auth.attempts")
                .description("Total authentication attempts")
                .tag("status", "success")
                .register(meterRegistry);

        this.authFailureCounter = Counter.builder("gym.auth.attempts")
                .description("Total authentication attempts")
                .tag("status", "failure")
                .register(meterRegistry);

        log.info("Initialized custom GymMetrics");
    }


    public void incrementTrainingsCreated() {
        trainingsCreatedCounter.increment();
        log.debug("Incremented gym.trainings.created metric");
    }

    public void incrementAuthSuccess() {
        authSuccessCounter.increment();
    }

    public void incrementAuthFailure() {
        authFailureCounter.increment();
    }
}