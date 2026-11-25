package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.dao.TrainingDao;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.TrainingEntity;
import org.example.entity.TrainingTypeEntity;
import org.example.metrics.GymMetrics;
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
    private GymMetrics gymMetrics;

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

    @Autowired
    public void setGymMetrics(GymMetrics gymMetrics) {
        this.gymMetrics = gymMetrics;
    }

    public Training createTraining(Training training) {
        validateTraining(training);

        TraineeEntity trainee = traineeDao.findByUsername(training.getTraineeUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + training.getTraineeUserName()));

        TrainerEntity trainer = trainerDao.findByUsername(training.getTrainerUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + training.getTrainerUserName()));

        String typeName = (training.getTrainingType() != null && training.getTrainingType().trainingTypeName() != null)
                ? training.getTrainingType().trainingTypeName()
                : trainer.getSpecialization();

        TrainingTypeEntity trainingType = trainingDao.findTrainingTypeByName(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + typeName));

        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingType(trainingType);
        entity.setTrainingName(training.getTrainingName());
        entity.setTrainingDate(training.getTrainingDate());
        entity.setTrainingDuration(training.getTrainingDuration());

        logger.info("Creating new Training: {}", training.getTrainingName());
        TrainingEntity savedEntity = trainingDao.save(entity);
        gymMetrics.incrementTrainingsCreated();
        logger.info("Training created successfully");
        return toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Training> getTrainingById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Training ID must be positive");
        }
        logger.info("Fetching Training with ID: {}", id);
        return trainingDao.findById(id).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        logger.info("Fetching trainings for Trainee: {} with criteria", username);
        return trainingDao.findTrainingsByTraineeUsernameAndCriteria(username, fromDate, toDate, trainerName, trainingTypeName)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        logger.info("Fetching trainings for Trainer: {} with criteria", username);
        return trainingDao.findTrainingsByTrainerUsernameAndCriteria(username, fromDate, toDate, traineeName)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        logger.info("Fetching all Training Types");
        return trainingDao.findAllTrainingTypes()
                .stream()
                .map(tt -> new TrainingType(tt.getId(), tt.getTrainingTypeName()))
                .toList();
    }

    private Training toDomain(TrainingEntity entity) {
        Training domain = new Training();
        domain.setId(entity.getId());
        if (entity.getTrainee() != null && entity.getTrainee().getUser() != null) {
            domain.setTraineeUserName(entity.getTrainee().getUser().getUserName());
            domain.setTraineeName(entity.getTrainee().getUser().getFirstName() + " " + entity.getTrainee().getUser().getLastName());
        }
        if (entity.getTrainer() != null && entity.getTrainer().getUser() != null) {
            domain.setTrainerUserName(entity.getTrainer().getUser().getUserName());
            domain.setTrainerName(entity.getTrainer().getUser().getFirstName() + " " + entity.getTrainer().getUser().getLastName());
        }
        if (entity.getTrainingType() != null) {
            domain.setTrainingType(new TrainingType(
                    entity.getTrainingType().getId(),
                    entity.getTrainingType().getTrainingTypeName()
            ));
        }
        domain.setTrainingName(entity.getTrainingName());
        domain.setTrainingDate(entity.getTrainingDate());
        domain.setTrainingDuration(entity.getTrainingDuration());
        return domain;
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
        if (training.getTraineeUserName() == null || training.getTraineeUserName().isBlank()) {
            throw new IllegalArgumentException("Trainee username is required");
        }
        if (training.getTrainerUserName() == null || training.getTrainerUserName().isBlank()) {
            throw new IllegalArgumentException("Trainer username is required");
        }
    }
}