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

        String username = UserCredentialsGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), allUsernames());
        String password = UserCredentialsGenerator.generatePassword();

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(trainer.getFirstName());
        userEntity.setLastName(trainer.getLastName());
        userEntity.setUserName(username);
        userEntity.setPassword(password);
        userEntity.setActive(true);

        TrainerEntity entity = new TrainerEntity();
        entity.setUser(userEntity);
        entity.setSpecialization(trainer.getSpecialization());

        TrainerEntity savedEntity = trainerDao.save(entity);
        logger.info("Trainer profile created successfully with username: {}", username);
        return toDomain(savedEntity);
    }

    public Trainer updateTrainer(Trainer trainer) {
        validateTrainer(trainer, true);

        TrainerEntity managed = trainerDao.findByUsername(trainer.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainer.getUserName()));

        managed.getUser().setFirstName(trainer.getFirstName());
        managed.getUser().setLastName(trainer.getLastName());
        managed.getUser().setActive(trainer.isActive());
        // Specialization is intentionally NOT modified

        logger.info("Updating Trainer profile: {}", managed.getUser().getUserName());
        return toDomain(managed);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerById(Long id) {
        validateId(id, "Trainer ID");
        logger.info("Fetching Trainer profile with ID: {}", id);
        return trainerDao.findById(id).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        logger.info("Fetching Trainer by username: {}", username);
        return trainerDao.findByUsername(username).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.info("Authenticating Trainer: {}", username);
        if (isBlank(username) || isBlank(password)) {
            return false;
        }
        return trainerDao.findByUsername(username)
                .map(entity -> entity.getUser() != null
                        && entity.getUser().isActive()
                        && entity.getUser().getPassword().equals(password))
                .orElse(false);
    }

    public void changePassword(String username, String newPassword) {
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        logger.info("Changing password for Trainer: {}", username);
        TrainerEntity managed = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        managed.getUser().setPassword(newPassword);
    }

    public void toggleActivation(String username, boolean isActive) {
        logger.info("Setting activation status to {} for Trainer: {}", isActive, username);
        TrainerEntity managed = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        if (managed.getUser().isActive() == isActive) {
            throw new IllegalStateException("Trainer is already " + (isActive ? "active" : "inactive"));
        }
        managed.getUser().setActive(isActive);
    }

    private List<String> allUsernames() {
        List<String> usernames = new ArrayList<>();
        trainerDao.findAll().forEach(t -> usernames.add(t.getUser().getUserName()));
        if (traineeDao != null) {
            traineeDao.findAll().forEach(t -> usernames.add(t.getUser().getUserName()));
        }
        return usernames;
    }

    private Trainer toDomain(TrainerEntity entity) {
        Trainer domain = new Trainer();
        domain.setId(entity.getId());
        domain.setSpecialization(entity.getSpecialization());
        if (entity.getUser() != null) {
            domain.setFirstName(entity.getUser().getFirstName());
            domain.setLastName(entity.getUser().getLastName());
            domain.setUserName(entity.getUser().getUserName());
            domain.setPassword(entity.getUser().getPassword());
            domain.setActive(entity.getUser().isActive());
        }
        if (entity.getTrainees() != null) {
            List<Trainee> trainees = entity.getTrainees().stream()
                    .map(this::toTraineeDomainWithoutTrainers)
                    .toList();
            domain.setTrainees(trainees);
        }
        return domain;
    }

    private Trainee toTraineeDomainWithoutTrainers(TraineeEntity entity) {
        Trainee trainee = new Trainee();
        trainee.setId(entity.getId());
        trainee.setDateOfBirth(entity.getDateOfBirth());
        trainee.setAddress(entity.getAddress());
        if (entity.getUser() != null) {
            trainee.setFirstName(entity.getUser().getFirstName());
            trainee.setLastName(entity.getUser().getLastName());
            trainee.setUserName(entity.getUser().getUserName());
            trainee.setPassword(entity.getUser().getPassword());
            trainee.setActive(entity.getUser().isActive());
        }
        return trainee;
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