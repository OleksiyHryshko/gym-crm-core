package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.dao.TrainingDao;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public Training createTraining(Training training) {
        validateTraining(training);

        if (traineeDao.findByUsername(training.getTraineeUsername()).isEmpty()) {
            throw new IllegalArgumentException("Trainee not found: " + training.getTraineeUsername());
        }
        if (trainerDao.findByUsername(training.getTrainerUsername()).isEmpty()) {
            throw new IllegalArgumentException("Trainer not found: " + training.getTrainerUsername());
        }

        logger.info("Creating new Training: {}", training.getTrainingName());
        Training savedTraining = trainingDao.save(training);
        logger.info("Training created successfully");
        return savedTraining;
    }

    @Transactional(readOnly = true)
    public Optional<Training> getTrainingById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Training ID must be positive");
        }
        logger.info("Fetching Training with ID: {}", id);
        return trainingDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        logger.info("Fetching trainings for Trainee: {} with criteria", username);
        return trainingDao.findTrainingsByTraineeUsernameAndCriteria(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        logger.info("Fetching trainings for Trainer: {} with criteria", username);
        return trainingDao.findTrainingsByTrainerUsernameAndCriteria(username, fromDate, toDate, traineeName);
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        logger.info("Fetching all Training Types");
        return trainingDao.findAllTrainingTypes();
    }

    private void validateTraining(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training must not be null");
        }
        if (training.getTrainingName() == null || training.getTrainingName().trim().isEmpty()) {
            throw new IllegalArgumentException("Training name is required");
        }
        if (training.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (training.getTrainingDuration() == null || training.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration must be greater than 0");
        }
        if (training.getTraineeUsername() == null || training.getTraineeUsername().isBlank()) {
            throw new IllegalArgumentException("Trainee username is required");
        }
        if (training.getTrainerUsername() == null || training.getTrainerUsername().isBlank()) {
            throw new IllegalArgumentException("Trainer username is required");
        }
    }
}