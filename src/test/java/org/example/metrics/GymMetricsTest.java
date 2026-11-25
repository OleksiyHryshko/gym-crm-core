package org.example.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {

    @Test
    void counters_ShouldIncrementProperly() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        GymMetrics metrics = new GymMetrics(registry);

        metrics.incrementTrainingsCreated();
        metrics.incrementTrainingsCreated();
        metrics.incrementAuthSuccess();
        metrics.incrementAuthFailure();

        assertEquals(2.0, registry.counter("gym.trainings.created").count());
        assertEquals(1.0, registry.counter("gym.auth.attempts", "status", "success").count());
        assertEquals(1.0, registry.counter("gym.auth.attempts", "status", "failure").count());
    }
}