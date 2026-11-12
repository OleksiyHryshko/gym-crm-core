package org.example;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainer;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public Trainer createTrainer(Trainer trainer) {
        validateTrainer(trainer, false);
        logger.info("Creating new Trainer profile for {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());

        trainer.getUser().setUserName(UserCredentialsGenerator.generateUsername(
                trainer.getUser().getFirstName(), trainer.getUser().getLastName(), allUsers()));
        trainer.getUser().setPassword(UserCredentialsGenerator.generatePassword());

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer profile created successfully with ID: {}", savedTrainer.getUserId());
        return savedTrainer;
    }

    public Trainer updateTrainer(Trainer trainer) {
        validateTrainer(trainer, true);
        logger.info("Updating Trainer profile with ID: {}", trainer.getUserId());
        return trainerDao.update(trainer);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        validateId(id, "Trainer ID");
        logger.info("Fetching Trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }

    private ArrayList<User> allUsers() {
        ArrayList<User> users = new ArrayList<>();
        trainerDao.findAll().forEach(trainer -> users.add(trainer.getUser()));
        if (traineeDao != null) {
            traineeDao.findAll().forEach(trainee -> users.add(trainee.getUser()));
        }
        return users;
    }

    private void validateTrainer(Trainer trainer, boolean update) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        if (update) {
            validateId(trainer.getUserId(), "Trainer ID");
        } else if (trainer.getUserId() != null) {
            throw new IllegalArgumentException("New trainer must not have an ID");
        }
        if (trainer.getUser() == null || isBlank(trainer.getUser().getFirstName()) || isBlank(trainer.getUser().getLastName())) {
            throw new IllegalArgumentException("Trainer first name and last name must not be blank");
        }
        if (isBlank(trainer.getSpecialization())) {
            throw new IllegalArgumentException("Trainer specialization is required");
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