package org.example;

import org.example.dao.TrainerDao;
import org.example.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Creating new Trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());

        trainer.setUserName(generateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(generatePassword());

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer profile created successfully with ID: {}", savedTrainer.getUserId());
        return savedTrainer;
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Updating Trainer profile with ID: {}", trainer.getUserId());
        return trainerDao.update(trainer);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        logger.info("Fetching Trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }

    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (isUsernameTaken(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        logger.debug("Generated unique username: {}", username);
        return username;
    }

    private boolean isUsernameTaken(String username) {
        List<Trainer> allTrainers = trainerDao.findAll();
        return allTrainers.stream()
                .anyMatch(t -> username.equals(t.getUserName()));
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(10);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}