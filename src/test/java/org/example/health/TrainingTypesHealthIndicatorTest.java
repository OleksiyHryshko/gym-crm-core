package org.example.health;

import org.example.model.TrainingType;
import org.example.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingTypesHealthIndicator indicator;

    @Test
    void health_WhenTrainingTypesExist_ReturnsUp() {
        when(trainingService.getAllTrainingTypes())
                .thenReturn(List.of(new TrainingType(1L, "Fitness"), new TrainingType(2L, "Yoga")));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("trainingTypesCount"));
    }

    @Test
    void health_WhenTrainingTypesEmpty_ReturnsDown() {
        when(trainingService.getAllTrainingTypes()).thenReturn(List.of());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void health_WhenServiceThrowsException_ReturnsDown() {
        when(trainingService.getAllTrainingTypes()).thenThrow(new RuntimeException("DB unavailable"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}