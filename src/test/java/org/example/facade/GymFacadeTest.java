package org.example.facade;

import org.example.dto.request.CreateTrainingRequest;
import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeProfileResponse;
import org.example.dto.response.TrainerProfileResponse;
import org.example.dto.response.TrainingResponse;
import org.example.mapper.TraineeMapper;
import org.example.mapper.TrainerMapper;
import org.example.mapper.TrainingMapper;
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

    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(
                traineeService,
                trainerService,
                trainingService,
                Mappers.getMapper(TraineeMapper.class),
                Mappers.getMapper(TrainerMapper.class),
                Mappers.getMapper(TrainingMapper.class)
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
}