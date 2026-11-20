package org.example.config;


import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.TrainingEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StorageConfig {

    @Bean
    public Map<Long, TrainerEntity> trainerStorage() {
        return new HashMap<>();
    }

    @Bean
    public Map<Long, TraineeEntity> traineeStorage() {
        return new HashMap<>();
    }

    @Bean
    public Map<Long, TrainingEntity> trainingStorage() {
        return new HashMap<>();
    }
}
