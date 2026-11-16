package org.example.service;

import org.example.UserCredentialsGenerator;
import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
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
                trainee.getFirstName(), trainee.getLastName(), allUsernames()));
        trainee.setPassword(UserCredentialsGenerator.generatePassword());
        trainee.setActive(true);

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee profile created successfully with username: {}", savedTrainee.getUserName());
        return savedTrainee;
    }

    public Trainee updateTrainee(Trainee trainee) {
        validateTrainee(trainee, true);

        Trainee existing = findByUsername(trainee.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + trainee.getUserName()));

        existing.setFirstName(trainee.getFirstName());
        existing.setLastName(trainee.getLastName());
        existing.setDateOfBirth(trainee.getDateOfBirth());
        existing.setAddress(trainee.getAddress());
        existing.setActive(trainee.isActive());

        logger.info("Updating Trainee profile: {}", existing.getUserName());
        return traineeDao.update(existing);
    }

    public void deleteTraineeByUsername(String username) {
        logger.info("Deleting Trainee profile with username: {}", username);
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        traineeDao.delete(trainee.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeById(Long id) {
        validateId(id, "Trainee ID");
        logger.info("Fetching Trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        logger.info("Fetching Trainee by username: {}", username);
        return traineeDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating Trainee: {}", username);
        return findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        logger.info("Changing password for Trainee: {}", username);
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.setPassword(newPassword);
        traineeDao.update(trainee);
    }

    public void toggleActivation(String username, boolean isActive) {
        logger.info("Setting activation status to {} for Trainee: {}", isActive, username);
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        if (trainee.isActive() == isActive) {
            throw new IllegalStateException("Trainee is already " + (isActive ? "active" : "inactive"));
        }
        trainee.setActive(isActive);
        traineeDao.update(trainee);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        logger.info("Fetching unassigned trainers for Trainee: {}", traineeUsername);
        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        List<String> assignedUsernames = trainee.getTrainers() == null
                ? List.of()
                : trainee.getTrainers().stream().map(Trainer::getUserName).toList();

        return trainerDao.findAll().stream()
                .filter(Trainer::isActive)
                .filter(trainer -> !assignedUsernames.contains(trainer.getUserName()))
                .collect(Collectors.toList());
    }

    public List<Trainer> updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for Trainee: {}", traineeUsername);
        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        List<Trainer> newTrainers = trainerDao.findAll().stream()
                .filter(t -> trainerUsernames.contains(t.getUserName()))
                .collect(Collectors.toList());

        trainee.setTrainers(newTrainers);
        traineeDao.update(trainee);
        return newTrainers;
    }

    private List<String> allUsernames() {
        List<String> usernames = new ArrayList<>();
        traineeDao.findAll().forEach(trainee -> usernames.add(trainee.getUserName()));
        if (trainerDao != null) {
            trainerDao.findAll().forEach(trainer -> usernames.add(trainer.getUserName()));
        }
        return usernames;
    }

    private void validateTrainee(Trainee trainee, boolean update) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        if (!update && trainee.getId() != null) {
            throw new IllegalArgumentException("New trainee must not have an ID");
        }
        if (update && isBlank(trainee.getUserName())) {
            throw new IllegalArgumentException("Trainee username is required for update");
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