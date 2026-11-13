package org.example.service;

import org.example.TrainingService;
import org.example.dao.TrainingDao;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_ShouldDelegateToDao() {
        Training training = validTraining();
        when(trainingDao.save(training)).thenReturn(training);

        assertSame(training, trainingService.createTraining(training));

        verify(trainingDao).save(training);
    }

    @Test
    void getTrainingById_ShouldReturnDaoResult() {
        Training training = new Training();
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        assertEquals(Optional.of(training), trainingService.getTrainingById(1L));

        verify(trainingDao).findById(1L);
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
    void createTraining_WhenRequiredFieldIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainingType(null);

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
    void createTraining_WhenParticipantIdIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTraineeId(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    @Test
    void createTraining_WhenTrainerIdIsMissing_ShouldThrowException() {
        Training training = validTraining();
        training.setTrainerId(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(training));
    }

    private Training validTraining() {
        return new Training(1L, 2L, "Strength training",
                new TrainingType("Strength"), LocalDate.of(2026, 1, 1), 60);
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
}
