package org.example;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.User;
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
@Transactional // Важливо для управління транзакціями
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
        trainee.setActive(true); // За замовчуванням активний

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee profile created successfully with username: {}", savedTrainee.getUserName());
        return savedTrainee;
    }

    public Trainee updateTrainee(Trainee trainee) {
        validateTrainee(trainee, true);
        logger.info("Updating Trainee profile: {}", trainee.getUserName());
        return traineeDao.update(trainee);
    }

    public void deleteTraineeByUsername(String username) {
        logger.info("Deleting Trainee profile with username: {}", username);
        findByUsername(username).ifPresent(trainee -> traineeDao.delete(trainee.getId()));
    }

    public Optional<Trainee> getTraineeById(Long id) {
        validateId(id, "Trainee ID");
        logger.info("Fetching Trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }

    public Optional<Trainee> findByUsername(String username) {
        logger.info("Fetching Trainee by username: {}", username);
        return traineeDao.findAll().stream()
                .filter(t -> t.getUserName().equals(username))
                .findFirst();
    }

    public boolean authenticate(String username, String password) {
        logger.info("Authenticating Trainee: {}", username);
        return findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for Trainee: {}", username);
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
        trainee.setPassword(newPassword);
        traineeDao.update(trainee);
    }

    public void toggleActivation(String username, boolean isActive) {
        logger.info("Setting activation status to {} for Trainee: {}", isActive, username);
        Trainee trainee = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
        trainee.setActive(isActive);
        traineeDao.update(trainee);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        logger.info("Fetching unassigned trainers for Trainee: {}", traineeUsername);
        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        List<Trainer> allTrainers = trainerDao.findAll();
        List<Trainer> assignedTrainers = trainee.getTrainers();

        return allTrainers.stream()
                .filter(trainer -> assignedTrainers == null || !assignedTrainers.contains(trainer))
                .collect(Collectors.toList());
    }

    public List<Trainer> updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for Trainee: {}", traineeUsername);
        Trainee trainee = findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        List<Trainer> newTrainers = trainerDao.findAll().stream()
                .filter(t -> trainerUsernames.contains(t.getFirstName())) // або t.getUserName()
                .collect(Collectors.toList());

        trainee.setTrainers(newTrainers);
        traineeDao.update(trainee);
        return newTrainers;
    }

    private ArrayList<User> allUsers() {
        ArrayList<User> users = new ArrayList<>();
        traineeDao.findAll().forEach(trainee -> users.add(trainee.getUser()));
        if (trainerDao != null) {
            trainerDao.findAll().forEach(trainer -> users.add(trainer.getUser()));
        }
        return users;
    }

    private void validateTrainee(Trainee trainee, boolean update) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
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