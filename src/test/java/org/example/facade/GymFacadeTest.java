package org.example.facade;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
import org.example.metrics.GymMetrics;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.example.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private SimpleMeterRegistry meterRegistry;
    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymFacade = new GymFacade(
                traineeService,
                trainerService,
                trainingService,
                Mappers.getMapper(TraineeMapper.class),
                Mappers.getMapper(TrainerMapper.class),
                Mappers.getMapper(TrainingMapper.class),
                new GymMetrics(meterRegistry)
        );
    }

    @Test
    void createTrainee_MapsRequestAndRegistrationResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(1998, 5, 10), "Krakow");

        Trainee saved = new Trainee();
        saved.setUserName("John.Doe");
        saved.setPassword("secret1234");

        when(traineeService.createTrainee(any(Trainee.class))).thenReturn(saved);

        RegistrationResponse response = gymFacade.createTrainee(request);

        assertEquals("John.Doe", response.getUserName());
        assertEquals("secret1234", response.getPassword());
    }

    @Test
    void createTrainer_MapsRequestAndRegistrationResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Jane", "Coach", "Yoga");

        Trainer saved = new Trainer();
        saved.setUserName("Jane.Coach");
        saved.setPassword("pass123456");

        when(trainerService.createTrainer(any(Trainer.class))).thenReturn(saved);

        RegistrationResponse response = gymFacade.createTrainer(request);

        assertEquals("Jane.Coach", response.getUserName());
        assertEquals("pass123456", response.getPassword());
    }

    @Test
    void login_ValidCredentials_IncrementsSuccessMetric() {
        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);

        gymFacade.login("John.Doe", "pass");

        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "success").count());
        assertEquals(0.0, meterRegistry.counter("gym.auth.attempts", "status", "failure").count());
    }

    @Test
    void login_InvalidCredentials_IncrementsFailureMetricAndThrows() {
        when(traineeService.authenticate("John.Doe", "wrong")).thenReturn(false);
        when(trainerService.authenticate("John.Doe", "wrong")).thenReturn(false);

        assertThrows(SecurityException.class, () -> gymFacade.login("John.Doe", "wrong"));

        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "failure").count());
    }

    @Test
    void changePassword_ValidTrainee_ChangesPasswordAndIncrementsSuccessMetric() {
        ChangePasswordRequest request = new ChangePasswordRequest("John.Doe", "oldPass123", "newPass12345");
        when(traineeService.authenticate("John.Doe", "oldPass123")).thenReturn(true);

        gymFacade.changePassword(request);

        verify(traineeService).changePassword("John.Doe", "newPass12345");
        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "success").count());
    }

    @Test
    void changePassword_ValidTrainer_ChangesPasswordAndIncrementsSuccessMetric() {
        ChangePasswordRequest request = new ChangePasswordRequest("Jane.Coach", "oldPass123", "newPass12345");
        when(traineeService.authenticate("Jane.Coach", "oldPass123")).thenReturn(false);
        when(trainerService.authenticate("Jane.Coach", "oldPass123")).thenReturn(true);

        gymFacade.changePassword(request);

        verify(trainerService).changePassword("Jane.Coach", "newPass12345");
        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "success").count());
    }

    @Test
    void changePassword_InvalidCredentials_IncrementsFailureAndThrows() {
        ChangePasswordRequest request = new ChangePasswordRequest("Unknown.User", "wrongPass1", "newPass12345");
        when(traineeService.authenticate("Unknown.User", "wrongPass1")).thenReturn(false);
        when(trainerService.authenticate("Unknown.User", "wrongPass1")).thenReturn(false);

        assertThrows(SecurityException.class, () -> gymFacade.changePassword(request));

        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "failure").count());
    }

    @Test
    void getTraineeByUsername_IncludesUserNameAndMapsTrainerSummaryCorrectly() {
        Trainer trainer = new Trainer();
        trainer.setUserName("Jane.Coach");
        trainer.setFirstName("Jane");
        trainer.setLastName("Coach");
        trainer.setSpecialization("Yoga");

        Trainee trainee = new Trainee();
        trainee.setUserName("John.Doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        trainee.setTrainers(List.of(trainer));

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(traineeService.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        TraineeProfileResponse profile = gymFacade.getTraineeByUserName("John.Doe", "pass", "John.Doe");

        assertEquals("John.Doe", profile.userName());
        assertTrue(profile.isActive());
        assertEquals(1, profile.trainers().size());
        assertEquals("Jane.Coach", profile.trainers().get(0).userName());
        assertEquals("Jane", profile.trainers().get(0).firstName());
        assertEquals("Coach", profile.trainers().get(0).lastName());
        assertEquals("Yoga", profile.trainers().get(0).specialization());
    }

    @Test
    void getTraineeByUsername_InvalidAuth_ThrowsSecurityException() {
        when(traineeService.authenticate("John.Doe", "bad")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.getTraineeByUserName("John.Doe", "bad", "John.Doe"));

        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "failure").count());
    }

    @Test
    void getTraineeByUsername_NotFound_ThrowsNoSuchElementException() {
        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(traineeService.findByUsername("Missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> gymFacade.getTraineeByUserName("John.Doe", "pass", "Missing"));
    }

    @Test
    void updateTrainee_MapsUserNameFromPathAndFields() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "John", "Updated", false, LocalDate.of(1998, 5, 10), "Gdansk");

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(traineeService.updateTrainee(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        TraineeProfileResponse response = gymFacade.updateTrainee("John.Doe", "pass", "John.Doe", request);

        assertEquals("John.Doe", response.userName());
        assertEquals("Updated", response.lastName());
        assertEquals("Gdansk", response.address());
        assertFalse(response.isActive());
    }

    @Test
    void deleteTraineeByUserName_DelegatesToService() {
        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);

        gymFacade.deleteTraineeByUserName("John.Doe", "pass", "John.Doe");

        verify(traineeService).deleteTraineeByUsername("John.Doe");
    }

    @Test
    void toggleTraineeActivation_DelegatesToService() {
        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);

        gymFacade.toggleTraineeActivation("John.Doe", "pass", "John.Doe", new ActivationRequest(false));

        verify(traineeService).toggleActivation("John.Doe", false);
    }

    @Test
    void getUnassignedTrainers_ReturnsMappedTrainerSummaries() {
        Trainer trainer = new Trainer();
        trainer.setUserName("Jane.Coach");
        trainer.setFirstName("Jane");
        trainer.setLastName("Coach");
        trainer.setSpecialization("Yoga");

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(traineeService.getUnassignedTrainers("John.Doe")).thenReturn(List.of(trainer));

        List<TrainerSummaryResponse> result = gymFacade.getUnassignedTrainers("John.Doe", "pass", "John.Doe");

        assertEquals(1, result.size());
        assertEquals("Jane.Coach", result.get(0).userName());
        assertEquals("Yoga", result.get(0).specialization());
    }

    @Test
    void updateTraineeTrainersList_ReturnsUpdatedTrainerSummaries() {
        Trainer trainer = new Trainer();
        trainer.setUserName("Jane.Coach");
        trainer.setFirstName("Jane");
        trainer.setLastName("Coach");
        trainer.setSpecialization("Yoga");

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of("Jane.Coach"));

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(traineeService.updateTrainersList("John.Doe", List.of("Jane.Coach"))).thenReturn(List.of(trainer));

        List<TrainerSummaryResponse> result = gymFacade.updateTraineeTrainersList(
                "John.Doe", "pass", "John.Doe", request);

        assertEquals(1, result.size());
        assertEquals("Jane.Coach", result.get(0).userName());
    }

    @Test
    void getTrainerByUsername_IncludesUserNameAndTraineesList() {
        Trainee trainee = new Trainee();
        trainee.setUserName("John.Doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        Trainer trainer = new Trainer();
        trainer.setUserName("Jane.Coach");
        trainer.setFirstName("Jane");
        trainer.setLastName("Coach");
        trainer.setSpecialization("Yoga");
        trainer.setActive(true);
        trainer.setTrainees(List.of(trainee));

        when(trainerService.authenticate("Jane.Coach", "pass")).thenReturn(true);
        when(trainerService.findByUsername("Jane.Coach")).thenReturn(Optional.of(trainer));

        TrainerProfileResponse profile = gymFacade.getTrainerByUserName("Jane.Coach", "pass", "Jane.Coach");

        assertEquals("Jane.Coach", profile.userName());
        assertTrue(profile.isActive());
        assertEquals(1, profile.trainees().size());
        assertEquals("John.Doe", profile.trainees().get(0).userName());
    }

    @Test
    void getTrainerByUsername_InvalidAuth_ThrowsSecurityException() {
        when(trainerService.authenticate("Jane.Coach", "bad")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.getTrainerByUserName("Jane.Coach", "bad", "Jane.Coach"));

        assertEquals(1.0, meterRegistry.counter("gym.auth.attempts", "status", "failure").count());
    }

    @Test
    void getTrainerByUsername_NotFound_ThrowsNoSuchElementException() {
        when(trainerService.authenticate("Jane.Coach", "pass")).thenReturn(true);
        when(trainerService.findByUsername("Missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> gymFacade.getTrainerByUserName("Jane.Coach", "pass", "Missing"));
    }

    @Test
    void updateTrainer_MapsUserNameAndFields() {
        UpdateTrainerRequest request = new UpdateTrainerRequest("Jane", "Updated", false);

        when(trainerService.authenticate("Jane.Coach", "pass")).thenReturn(true);
        when(trainerService.updateTrainer(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setSpecialization("Yoga");
            return t;
        });

        TrainerProfileResponse response = gymFacade.updateTrainer("Jane.Coach", "pass", "Jane.Coach", request);

        assertEquals("Jane.Coach", response.userName());
        assertEquals("Updated", response.lastName());
        assertEquals("Yoga", response.specialization());
        assertFalse(response.isActive());
    }

    @Test
    void toggleTrainerActivation_DelegatesToService() {
        when(trainerService.authenticate("Jane.Coach", "pass")).thenReturn(true);

        gymFacade.toggleTrainerActivation("Jane.Coach", "pass", "Jane.Coach", new ActivationRequest(true));

        verify(trainerService).toggleActivation("Jane.Coach", true);
    }

    @Test
    void createTraining_MapsTraineeAndTrainerUserNamesToModel() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                "John.Doe", "Jane.Coach", "HIIT", LocalDate.now(), 45);

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);

        gymFacade.createTraining("John.Doe", "pass", request);

        verify(trainingService).createTraining(argThat(t ->
                "John.Doe".equals(t.getTraineeUserName())
                        && "Jane.Coach".equals(t.getTrainerUserName())
                        && "HIIT".equals(t.getTrainingName())
                        && Integer.valueOf(45).equals(t.getTrainingDuration())));
    }

    @Test
    void getTraineeTrainings_MapsTrainingTypeName() {
        Training training = new Training();
        training.setTrainingName("Yoga Flow");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingType(new TrainingType(1L, "Yoga"));
        training.setTrainingDuration(60);
        training.setTrainerName("Jane Coach");

        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(trainingService.getTraineeTrainings("John.Doe", null, null, null, null))
                .thenReturn(List.of(training));

        List<TrainingResponse> result = gymFacade.getTraineeTrainings(
                "John.Doe", "pass", "John.Doe", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Yoga", result.get(0).trainingType());
    }

    @Test
    void getTrainerTrainings_MapsTraineeNameAndTrainingType() {
        Training training = new Training();
        training.setTrainingName("Yoga Flow");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingType(new TrainingType(1L, "Yoga"));
        training.setTrainingDuration(60);
        training.setTraineeName("John Doe");

        when(trainerService.authenticate("Jane.Coach", "pass")).thenReturn(true);
        when(trainingService.getTrainerTrainings("Jane.Coach", null, null, null))
                .thenReturn(List.of(training));

        List<TrainerTrainingResponse> result = gymFacade.getTrainerTrainings(
                "Jane.Coach", "pass", "Jane.Coach", null, null, null);

        assertEquals(1, result.size());
        assertEquals("Yoga", result.get(0).trainingType());
        assertEquals("John Doe", result.get(0).traineeName());
    }

    @Test
    void getAllTrainingTypes_MapsTrainingTypeResponses() {
        when(traineeService.authenticate("John.Doe", "pass")).thenReturn(true);
        when(trainingService.getAllTrainingTypes()).thenReturn(List.of(new TrainingType(1L, "Yoga")));

        List<TrainingTypeResponse> result = gymFacade.getAllTrainingTypes("John.Doe", "pass");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Yoga", result.get(0).trainingType());
    }
}