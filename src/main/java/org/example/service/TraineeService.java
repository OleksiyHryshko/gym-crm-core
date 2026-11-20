package org.example.service;

import org.example.UserCredentialsGenerator;
import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
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

        String username = UserCredentialsGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), allUsernames());
        String password = UserCredentialsGenerator.generatePassword();

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(trainee.getFirstName());
        userEntity.setLastName(trainee.getLastName());
        userEntity.setUserName(username);
        userEntity.setPassword(password);
        userEntity.setActive(true);

        TraineeEntity entity = new TraineeEntity();
        entity.setUser(userEntity);
        entity.setDateOfBirth(trainee.getDateOfBirth());
        entity.setAddress(trainee.getAddress());

        TraineeEntity savedEntity = traineeDao.save(entity);
        logger.info("Trainee profile created successfully with username: {}", username);
        return toDomain(savedEntity);
    }

    public Trainee updateTrainee(Trainee trainee) {
        validateTrainee(trainee, true);

        TraineeEntity managed = traineeDao.findByUsername(trainee.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + trainee.getUserName()));

        managed.getUser().setFirstName(trainee.getFirstName());
        managed.getUser().setLastName(trainee.getLastName());
        managed.getUser().setActive(trainee.isActive());
        managed.setDateOfBirth(trainee.getDateOfBirth());
        managed.setAddress(trainee.getAddress());

        logger.info("Updating Trainee profile: {}", managed.getUser().getUserName());
        // 'managed' is attached to Persistence Context — dirty checking updates DB automatically
        return toDomain(managed);
    }

    public void deleteTraineeByUsername(String username) {
        logger.info("Deleting Trainee profile with username: {}", username);
        TraineeEntity entity = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        traineeDao.delete(entity.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeById(Long id) {
        validateId(id, "Trainee ID");
        logger.info("Fetching Trainee profile with ID: {}", id);
        return traineeDao.findById(id).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        logger.info("Fetching Trainee by username: {}", username);
        return traineeDao.findByUsername(username).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating Trainee: {}", username);
        if (isBlank(username) || isBlank(password)) {
            return false;
        }
        return traineeDao.findByUsername(username)
                .map(entity -> entity.getUser() != null
                        && entity.getUser().isActive()
                        && entity.getUser().getPassword().equals(password))
                .orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        logger.info("Changing password for Trainee: {}", username);
        TraineeEntity managed = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        managed.getUser().setPassword(newPassword);
    }

    public void toggleActivation(String username, boolean isActive) {
        logger.info("Setting activation status to {} for Trainee: {}", isActive, username);
        TraineeEntity managed = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        if (managed.getUser().isActive() == isActive) {
            throw new IllegalStateException("Trainee is already " + (isActive ? "active" : "inactive"));
        }
        managed.getUser().setActive(isActive);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        logger.info("Fetching unassigned trainers for Trainee: {}", traineeUsername);
        TraineeEntity trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        List<String> assignedUsernames = trainee.getTrainers() == null
                ? List.of()
                : trainee.getTrainers().stream()
                .map(t -> t.getUser().getUserName())
                .toList();

        return trainerDao.findAll().stream()
                .filter(t -> t.getUser().isActive())
                .filter(t -> !assignedUsernames.contains(t.getUser().getUserName()))
                .map(this::toTrainerDomainWithoutTrainees)
                .toList();
    }

    public List<Trainer> updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for Trainee: {}", traineeUsername);
        TraineeEntity managed = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        List<TrainerEntity> newTrainerEntities = trainerDao.findAll().stream()
                .filter(t -> trainerUsernames.contains(t.getUser().getUserName()))
                .toList();

        managed.setTrainers(new ArrayList<>(newTrainerEntities));

        return newTrainerEntities.stream()
                .map(this::toTrainerDomainWithoutTrainees)
                .toList();
    }

    private List<String> allUsernames() {
        List<String> usernames = new ArrayList<>();
        traineeDao.findAll().forEach(t -> usernames.add(t.getUser().getUserName()));
        if (trainerDao != null) {
            trainerDao.findAll().forEach(t -> usernames.add(t.getUser().getUserName()));
        }
        return usernames;
    }


    private Trainee toDomain(TraineeEntity entity) {
        Trainee domain = new Trainee();
        domain.setId(entity.getId());
        if (entity.getUser() != null) {
            domain.setFirstName(entity.getUser().getFirstName());
            domain.setLastName(entity.getUser().getLastName());
            domain.setUserName(entity.getUser().getUserName());
            domain.setPassword(entity.getUser().getPassword());
            domain.setActive(entity.getUser().isActive());
        }
        domain.setDateOfBirth(entity.getDateOfBirth());
        domain.setAddress(entity.getAddress());

        if (entity.getTrainers() != null) {
            List<Trainer> trainers = entity.getTrainers().stream()
                    .map(this::toTrainerDomainWithoutTrainees)
                    .toList();
            domain.setTrainers(trainers);
        }
        return domain;
    }

    private Trainer toTrainerDomainWithoutTrainees(TrainerEntity entity) {
        Trainer trainer = new Trainer();
        trainer.setId(entity.getId());
        trainer.setSpecialization(entity.getSpecialization());
        if (entity.getUser() != null) {
            trainer.setFirstName(entity.getUser().getFirstName());
            trainer.setLastName(entity.getUser().getLastName());
            trainer.setUserName(entity.getUser().getUserName());
            trainer.setPassword(entity.getUser().getPassword());
            trainer.setActive(entity.getUser().isActive());
        }
        return trainer;
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