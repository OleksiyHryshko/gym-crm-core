package org.example.service;

import org.example.TraineeService;
import org.example.dao.TraineeDao;
import org.example.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainee = new Trainee("John", "Doe", true, java.time.LocalDate.of(1990, 1, 1), "Address");
    }

    @Test
    void createTrainee_ShouldGenerateUsernameAndTenCharacterPassword() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee saved = invocation.getArgument(0);
            saved.setUserId(1L);
            return saved;
        });

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertEquals(1L, result.getUserId());
        verify(traineeDao).save(trainee);
    }

    @Test
    void createTrainee_WhenUsernameExists_ShouldAddNumericSuffix() {
        Trainee existing = new Trainee();
        existing.setUserName("John.Doe");
        when(traineeDao.findAll()).thenReturn(List.of(existing));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void updateTrainee_ShouldDelegateToDao() {
        when(traineeDao.update(trainee)).thenReturn(trainee);

        assertSame(trainee, traineeService.updateTrainee(trainee));

        verify(traineeDao).update(trainee);
    }

    @Test
    void deleteTrainee_ShouldDelegateToDao() {
        traineeService.deleteTrainee(7L);

        verify(traineeDao).delete(7L);
    }

    @Test
    void getTraineeById_ShouldReturnDaoResult() {
        when(traineeDao.findById(7L)).thenReturn(Optional.of(trainee));

        assertEquals(Optional.of(trainee), traineeService.getTraineeById(7L));

        verify(traineeDao).findById(7L);
    }
}
