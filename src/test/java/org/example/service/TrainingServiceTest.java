package org.example.service;

import org.example.TrainingService;
import org.example.dao.TrainingDao;
import org.example.model.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
        Training training = new Training();
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
}
