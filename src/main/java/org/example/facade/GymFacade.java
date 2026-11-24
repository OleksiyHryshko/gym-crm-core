package org.example.facade;

import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.example.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;

    @Autowired
    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, TraineeMapper traineeMapper, TrainerMapper trainerMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper= trainerMapper;
    }

    // --- Public Registration (No Authentication Required) ---

    public RegistrationResponse createTrainee(TraineeRegistrationRequest request) {
        Trainee trainee = traineeService.createTrainee(traineeMapper.toModel(request));
        return new RegistrationResponse(trainee.getUserName(), trainee.getPassword());

    }

    public RegistrationResponse createTrainer(TrainerRegistrationRequest request) {
        Trainer trainer = trainerService.createTrainer(trainerMapper.toModel(request));
        return new RegistrationResponse(trainer.getUserName(), trainer.getPassword());

    }

    // --- Authentication ---

    public boolean authenticateTrainee(String username, String password) {
        return traineeService.authenticate(username, password);
    }

    public boolean authenticateTrainer(String username, String password) {
        return trainerService.authenticate(username, password);
    }

    private void requireTraineeAuth(String username, String password) {
        if (!traineeService.authenticate(username, password)) {
            throw new SecurityException("Authentication failed for Trainee: " + username);
        }
    }

    private void requireTrainerAuth(String username, String password) {
        if (!trainerService.authenticate(username, password)) {
            throw new SecurityException("Authentication failed for Trainer: " + username);
        }
    }

    private void requireAnyUserAuth(String username, String password) {
        if (!traineeService.authenticate(username, password) && !trainerService.authenticate(username, password)) {
            throw new SecurityException("Authentication failed for user: " + username);
        }
    }

    // --- Trainee Operations ---

    public Optional<Trainee> getTraineeById(String authUsername, String authPassword, Long id) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeService.getTraineeById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String authUsername, String authPassword, String targetUsername) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeService.findByUsername(targetUsername);
    }

    public Trainee updateTrainee(String authUsername, String authPassword, Trainee trainee) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeService.updateTrainee(trainee);
    }

    public void deleteTraineeByUsername(String authUsername, String authPassword, String targetUsername) {
        requireTraineeAuth(authUsername, authPassword);
        traineeService.deleteTraineeByUsername(targetUsername);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        requireTraineeAuth(username, oldPassword);
        traineeService.changePassword(username, newPassword);
    }

    public void toggleTraineeActivation(String authUsername, String authPassword, String targetUsername, boolean isActive) {
        requireTraineeAuth(authUsername, authPassword);
        traineeService.toggleActivation(targetUsername, isActive);
    }

    public List<Trainer> getUnassignedTrainers(String authUsername, String authPassword, String traineeUsername) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeService.getUnassignedTrainers(traineeUsername);
    }

    public List<Trainer> updateTraineeTrainersList(String authUsername, String authPassword, String traineeUsername, List<String> trainerUsernames) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }

    // --- Trainer Operations ---

    public Optional<Trainer> getTrainerById(String authUsername, String authPassword, Long id) {
        requireTrainerAuth(authUsername, authPassword);
        return trainerService.getTrainerById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String authUsername, String authPassword, String targetUsername) {
        requireTrainerAuth(authUsername, authPassword);
        return trainerService.findByUsername(targetUsername);
    }

    public Trainer updateTrainer(String authUsername, String authPassword, Trainer trainer) {
        requireTrainerAuth(authUsername, authPassword);
        return trainerService.updateTrainer(trainer);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        requireTrainerAuth(username, oldPassword);
        trainerService.changePassword(username, newPassword);
    }

    public void toggleTrainerActivation(String authUsername, String authPassword, String targetUsername, boolean isActive) {
        requireTrainerAuth(authUsername, authPassword);
        trainerService.toggleActivation(targetUsername, isActive);
    }

    // --- Training & TrainingType Operations ---

    public Training createTraining(String authUsername, String authPassword, Training training) {
        requireAnyUserAuth(authUsername, authPassword);
        return trainingService.createTraining(training);
    }

    public Optional<Training> getTrainingById(String authUsername, String authPassword, Long id) {
        requireAnyUserAuth(authUsername, authPassword);
        return trainingService.getTrainingById(id);
    }

    public List<Training> getTraineeTrainings(String authUsername, String authPassword, String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        requireTraineeAuth(authUsername, authPassword);
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(String authUsername, String authPassword, String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate, String traineeName) {
        requireTrainerAuth(authUsername, authPassword);
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    public List<TrainingType> getAllTrainingTypes(String authUsername, String authPassword) {
        requireAnyUserAuth(authUsername, authPassword);
        return trainingService.getAllTrainingTypes();
    }
}