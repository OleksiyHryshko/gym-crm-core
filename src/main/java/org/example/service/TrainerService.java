package org.example.service;

import org.example.UserCredentialsGenerator;
import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
        logger.info("Creating new Trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());

        trainer.setUserName(UserCredentialsGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), allUsernames()));
        trainer.setPassword(UserCredentialsGenerator.generatePassword());
        trainer.setActive(true);

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer profile created successfully with username: {}", savedTrainer.getUserName());
        return savedTrainer;
    }

    public Trainer updateTrainer(Trainer trainer) {
        validateTrainer(trainer, true);

        Trainer existing = findByUsername(trainer.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainer.getUserName()));

        existing.setFirstName(trainer.getFirstName());
        existing.setLastName(trainer.getLastName());
        existing.setActive(trainer.isActive());

        logger.info("Updating Trainer profile: {}", existing.getUserName());
        return trainerDao.update(existing);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerById(Long id) {
        validateId(id, "Trainer ID");
        logger.info("Fetching Trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        logger.info("Fetching Trainer by username: {}", username);
        return trainerDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating Trainer: {}", username);
        return findByUsername(username)
                .map(trainer -> trainer.getPassword().equals(password))
                .orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        logger.info("Changing password for Trainer: {}", username);
        Trainer trainer = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.setPassword(newPassword);
        trainerDao.update(trainer);
    }

    public void toggleActivation(String username, boolean isActive) {
        logger.info("Setting activation status to {} for Trainer: {}", isActive, username);
        Trainer trainer = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        if (trainer.isActive() == isActive) {
            throw new IllegalStateException("Trainer is already " + (isActive ? "active" : "inactive"));
        }
        trainer.setActive(isActive);
        trainerDao.update(trainer);
    }

    private List<String> allUsernames() {
        List<String> usernames = new ArrayList<>();
        trainerDao.findAll().forEach(trainer -> usernames.add(trainer.getUserName()));
        if (traineeDao != null) {
            traineeDao.findAll().forEach(trainee -> usernames.add(trainee.getUserName()));
        }
        return usernames;
    }

    private void validateTrainer(Trainer trainer, boolean update) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        if (!update && trainer.getId() != null) {
            throw new IllegalArgumentException("New trainer must not have an ID");
        }
        if (update && isBlank(trainer.getUserName())) {
            throw new IllegalArgumentException("Trainer username is required for update");
        }
        if (isBlank(trainer.getFirstName()) || isBlank(trainer.getLastName())) {
            throw new IllegalArgumentException("Trainer first name and last name must not be blank");
        }
        if (!update && isBlank(trainer.getSpecialization())) {
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