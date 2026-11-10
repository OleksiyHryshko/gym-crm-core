package org.example;

import org.example.dao.TraineeDao;
import org.example.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.dao.TrainerDao;
import org.example.model.User;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public Trainee createTrainee(Trainee trainee) {
        validateTrainee(trainee, false);
        logger.info("Creating new Trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());

        trainee.setUserName(UserCredentialsGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), allUsers()));
        trainee.setPassword(UserCredentialsGenerator.generatePassword());

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee profile created successfully with ID: {}", savedTrainee.getUserId());
        return savedTrainee;
    }

    public Trainee updateTrainee(Trainee trainee) {
        validateTrainee(trainee, true);
        logger.info("Updating Trainee profile with ID: {}", trainee.getUserId());
        return traineeDao.update(trainee);
    }

    public void deleteTrainee(Long id) {
        validateId(id, "Trainee ID");
        logger.info("Deleting Trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        validateId(id, "Trainee ID");
        logger.info("Fetching Trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }

    private ArrayList<User> allUsers() {
        ArrayList<User> users = new ArrayList<>(traineeDao.findAll());
        if (trainerDao != null) {
            users.addAll(trainerDao.findAll());
        }
        return users;
    }

    private void validateTrainee(Trainee trainee, boolean update) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        if (!update && trainee.getUserId() != null) {
            throw new IllegalArgumentException("New trainee must not have an ID");
        }
        if (isBlank(trainee.getFirstName()) || isBlank(trainee.getLastName())) {
            throw new IllegalArgumentException("Trainee first name and last name must not be blank");
        }
    }

    private void validateId(Long id, String label) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(label + " must be positive");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}