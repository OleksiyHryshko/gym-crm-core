package org.example;

import org.example.dao.TraineeDao;
import org.example.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);
    private TraineeDao traineeDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public Trainee createTrainee(Trainee trainee) {
        logger.info("Creating new Trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());

        trainee.setUserName(generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(generatePassword());

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee profile created successfully with ID: {}", savedTrainee.getUserId());
        return savedTrainee;
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Updating Trainee profile with ID: {}", trainee.getUserId());
        return traineeDao.update(trainee);
    }

    public void deleteTrainee(Long id) {
        logger.info("Deleting Trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }

    public Optional<Trainee> getTraineeById(Long id) {
        logger.info("Fetching Trainee profile with ID: {}", id);
        return traineeDao.findById(id);
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
        List<Trainee> allTrainees = traineeDao.findAll();
        return allTrainees.stream()
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