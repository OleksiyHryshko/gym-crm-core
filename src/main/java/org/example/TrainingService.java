package org.example;

import org.example.dao.TrainingDao;
import org.example.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private TrainingDao trainingDao;


    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public Training createTraining(Training training) {
        logger.info("Creating new Training: {}", training.getTrainingName());
        Training savedTraining = trainingDao.save(training);
        logger.info("Training created successfully");
        return savedTraining;
    }

    public Optional<Training> getTrainingById(Long id) {
        logger.info("Fetching Training with ID: {}", id);
        return trainingDao.findById(id);
    }
}