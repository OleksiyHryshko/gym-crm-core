package org.example.facade;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.metrics.GymMetrics;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.example.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final GymMetrics gymMetrics;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TraineeMapper traineeMapper,
                     TrainerMapper trainerMapper,
                     TrainingMapper trainingMapper,
                     GymMetrics gymMetrics) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
        this.gymMetrics = gymMetrics;
    }

    // --- Public Registration (No Authentication Required) ---

    public RegistrationResponse createTrainee(TraineeRegistrationRequest request) {
        Trainee created = traineeService.createTrainee(traineeMapper.toModel(request));
        return traineeMapper.toRegistrationResponse(created);
    }

    public RegistrationResponse createTrainer(TrainerRegistrationRequest request) {
        Trainer created = trainerService.createTrainer(trainerMapper.toModel(request));
        return trainerMapper.toRegistrationResponse(created);
    }

    // --- Authentication ---

    public void login(String username, String password) {
        requireAnyUserAuth(username, password);
    }

    public void changePassword(ChangePasswordRequest request) {
        if (traineeService.authenticate(request.userName(), request.oldPassword())) {
            gymMetrics.incrementAuthSuccess();
            traineeService.changePassword(request.userName(), request.newPassword());
            return;
        }
        if (trainerService.authenticate(request.userName(), request.oldPassword())) {
            gymMetrics.incrementAuthSuccess();
            trainerService.changePassword(request.userName(), request.newPassword());
            return;
        }
        gymMetrics.incrementAuthFailure();
        log.warn("Password change failed authentication for user: {}", request.userName());
        throw new SecurityException("Authentication failed for user: " + request.userName());
    }

    private void requireTraineeAuth(String username, String password) {
        if (!traineeService.authenticate(username, password)) {
            gymMetrics.incrementAuthFailure();
            log.warn("Authentication failed for Trainee: {}", username);
            throw new SecurityException("Authentication failed for Trainee: " + username);
        }
        gymMetrics.incrementAuthSuccess();
    }

    private void requireTrainerAuth(String username, String password) {
        if (!trainerService.authenticate(username, password)) {
            gymMetrics.incrementAuthFailure();
            log.warn("Authentication failed for Trainer: {}", username);
            throw new SecurityException("Authentication failed for Trainer: " + username);
        }
        gymMetrics.incrementAuthSuccess();
    }

    private void requireAnyUserAuth(String username, String password) {
        if (!traineeService.authenticate(username, password) && !trainerService.authenticate(username, password)) {
            gymMetrics.incrementAuthFailure();
            log.warn("Authentication failed for user: {}", username);
            throw new SecurityException("Authentication failed for user: " + username);
        }
        gymMetrics.incrementAuthSuccess();
    }

    // --- Trainee Operations ---

    public TraineeProfileResponse getTraineeByUserName(String authUsername, String authPassword, String targetUsername) {
        requireTraineeAuth(authUsername, authPassword);
        Trainee trainee = traineeService.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found: " + targetUsername));
        return traineeMapper.toProfileResponse(trainee);
    }

    public TraineeProfileResponse updateTrainee(String authUsername, String authPassword, String username, UpdateTraineeRequest request) {
        requireTraineeAuth(authUsername, authPassword);
        Trainee updated = traineeService.updateTrainee(traineeMapper.toModel(username, request));
        return traineeMapper.toProfileResponse(updated);
    }

    public void deleteTraineeByUserName(String authUsername, String authPassword, String targetUsername) {
        requireTraineeAuth(authUsername, authPassword);
        traineeService.deleteTraineeByUsername(targetUsername);
    }

    public void toggleTraineeActivation(String authUsername, String authPassword, String targetUsername, ActivationRequest request) {
        requireTraineeAuth(authUsername, authPassword);
        traineeService.toggleActivation(targetUsername, request.isActive());
    }

    public List<TrainerSummaryResponse> getUnassignedTrainers(String authUsername, String authPassword, String traineeUsername) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeMapper.toTrainerSummaryResponseList(
                traineeService.getUnassignedTrainers(traineeUsername)
        );
    }

    public List<TrainerSummaryResponse> updateTraineeTrainersList(String authUsername,
                                                                  String authPassword,
                                                                  String traineeUsername,
                                                                  UpdateTraineeTrainersRequest request) {
        requireTraineeAuth(authUsername, authPassword);
        return traineeMapper.toTrainerSummaryResponseList(
                traineeService.updateTrainersList(traineeUsername, request.trainerUserNames())
        );
    }

    // --- Trainer Operations ---

    public TrainerProfileResponse getTrainerByUserName(String authUsername, String authPassword, String targetUsername) {
        requireTrainerAuth(authUsername, authPassword);
        Trainer trainer = trainerService.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found: " + targetUsername));
        return trainerMapper.toProfileResponse(trainer);
    }

    public TrainerProfileResponse updateTrainer(String authUsername, String authPassword, String username, UpdateTrainerRequest request) {
        requireTrainerAuth(authUsername, authPassword);
        Trainer updated = trainerService.updateTrainer(trainerMapper.toModel(username, request));
        return trainerMapper.toProfileResponse(updated);
    }

    public void toggleTrainerActivation(String authUsername, String authPassword, String targetUsername, ActivationRequest request) {
        requireTrainerAuth(authUsername, authPassword);
        trainerService.toggleActivation(targetUsername, request.isActive());
    }

    // --- Training & TrainingType Operations ---

    public void createTraining(String authUsername, String authPassword, CreateTrainingRequest request) {
        requireAnyUserAuth(authUsername, authPassword);
        trainingService.createTraining(trainingMapper.toModel(request));
    }

    public List<TrainingResponse> getTraineeTrainings(String authUsername, String authPassword, String traineeUsername,
                                                      LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        requireTraineeAuth(authUsername, authPassword);
        return trainingMapper.toTrainingResponseList(
                trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName)
        );
    }

    public List<TrainerTrainingResponse> getTrainerTrainings(String authUsername, String authPassword, String trainerUsername,
                                                             LocalDate fromDate, LocalDate toDate, String traineeName) {
        requireTrainerAuth(authUsername, authPassword);
        return trainingMapper.toTrainerTrainingResponseList(
                trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName)
        );
    }

    public List<TrainingTypeResponse> getAllTrainingTypes(String authUsername, String authPassword) {
        requireAnyUserAuth(authUsername, authPassword);
        return trainingMapper.toTrainingTypeResponseList(
                trainingService.getAllTrainingTypes()
        );
    }
}