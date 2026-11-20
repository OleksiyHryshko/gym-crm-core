package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.dao.TrainingDao;
import org.example.entity.*;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_WhenTraineeAndTrainerExist_ShouldAssembleEntityAndSave() {
        Training training = validTraining();

        UserEntity traineeUser = new UserEntity("John", "Doe", true);
        traineeUser.setUserName("John.Doe");
        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setUser(traineeUser);

        UserEntity trainerUser = new UserEntity("Jane", "Smith", true);
        trainerUser.setUserName("Jane.Smith");
        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(trainerUser);
        trainerEntity.setSpecialization("Strength");

        TrainingTypeEntity typeEntity = new TrainingTypeEntity("Strength");
        typeEntity.setId(1L);

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.of(trainerEntity));
        when(trainingDao.findTrainingTypeByName("Strength")).thenReturn(Optional.of(typeEntity));
        when(trainingDao.save(any(TrainingEntity.class))).thenAnswer(invocation -> {
            TrainingEntity saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Training result = trainingService.createTraining(training);

        assertEquals(10L, result.getId());
        assertEquals("John.Doe", result.getTraineeUsername());
        assertEquals("Jane.Smith", result.getTrainerUsername());
        assertEquals("Strength training", result.getTrainingName());
        verify(trainingDao).save(any(TrainingEntity.class));
    }

    @Test
    void createTraining_WhenTraineeDoesNotExist_ShouldThrowException() {
        Training training = validTraining();
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void createTraining_WhenTrainerDoesNotExist_ShouldThrowException() {
        Training training = validTraining();
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(new TraineeEntity()));
        when(trainerDao.findByUsername("Jane.Smith")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void getTrainingById_ShouldReturnMappedDomain() {
        TrainingEntity entity = new TrainingEntity();
        entity.setId(1L);
        entity.setTrainingName("Strength training");
        when(trainingDao.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Training> result = trainingService.getTrainingById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Strength training", result.get().getTrainingName());
    }

    @Test
    void createTraining_WhenTrainingIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(null));
    }

    @Test
    void createTraining_WhenTrainingNameIsBlank_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainingName(" ");

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenTrainingDateIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainingDate(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenDurationIsNotPositive_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainingDuration(0);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenDurationIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainingDuration(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenTraineeUsernameIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTraineeUsername(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenTrainerUsernameIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainerUsername(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void getTraineeTrainings_ShouldCallDaoMethod() {
        LocalDate date = LocalDate.now();
        trainingService.getTraineeTrainings("John.Doe", date, date, "TrainerName", "Type");
        verify(trainingDao).findTrainingsByTraineeUsernameAndCriteria("John.Doe", date, date, "TrainerName", "Type");
    }

    @Test
    void getTrainerTrainings_ShouldCallDaoMethod() {
        LocalDate date = LocalDate.now();
        trainingService.getTrainerTrainings("Jane.Doe", date, date, "TraineeName");
        verify(trainingDao).findTrainingsByTrainerUsernameAndCriteria("Jane.Doe", date, date, "TraineeName");
    }

    private Training validTraining() {
        Training training = new Training();
        training.setTraineeUsername("John.Doe");
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingName("Strength training");
        training.setTrainingType(new TrainingType(1L, "Strength"));
        training.setTrainingDate(LocalDate.of(2026, 1, 1));
        training.setTrainingDuration(60);
        return training;
    }
}