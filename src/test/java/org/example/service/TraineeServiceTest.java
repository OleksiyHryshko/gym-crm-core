package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Address");
    }

    @Test
    void createTrainee_ShouldGenerateUsernameAndTenCharacterPassword() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertEquals(1L, result.getId());
        verify(traineeDao).save(trainee);
    }

    @Test
    void createTrainee_WhenUsernameExists_ShouldAddNumericSuffix() {
        Trainee existing = new Trainee();
        existing.setUserName("John.Doe");
        when(traineeDao.findAll()).thenReturn(List.of(existing));
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void updateTrainee_WhenExists_ShouldUpdateAndDelegateToDao() {
        trainee.setUserName("John.Doe");
        Trainee existing = new Trainee();
        existing.setId(1L);
        existing.setUserName("John.Doe");

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(traineeDao.update(existing)).thenReturn(existing);

        Trainee result = traineeService.updateTrainee(trainee);

        assertSame(existing, result);
        assertEquals("John", existing.getFirstName());
        assertEquals("Doe", existing.getLastName());
        verify(traineeDao).update(existing);
    }

    @Test
    void updateTrainee_WhenNotFound_ShouldThrowException() {
        trainee.setUserName("Missing.User");
        when(traineeDao.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> traineeService.updateTrainee(trainee));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void deleteTraineeByUsername_ShouldFindAndDeleteById() {
        trainee.setId(1L);
        trainee.setUserName("John.Doe");

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername("John.Doe");

        verify(traineeDao).delete(1L);
    }

    @Test
    void deleteTraineeByUsername_WhenNotFound_ShouldThrowException() {
        when(traineeDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> traineeService.deleteTraineeByUsername("Unknown"));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void getTraineeById_ShouldReturnDaoResult() {
        when(traineeDao.findById(7L)).thenReturn(Optional.of(trainee));

        assertEquals(Optional.of(trainee), traineeService.getTraineeById(7L));

        verify(traineeDao).findById(7L);
    }

    @Test
    void authenticate_WhenCredentialsMatch_ShouldReturnTrue() {
        trainee.setUserName("John.Doe");
        trainee.setPassword("secret123");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        boolean isAuthenticated = traineeService.authenticate("John.Doe", "secret123");

        assertTrue(isAuthenticated);
    }

    @Test
    void changePassword_WhenTraineeExists_ShouldUpdatePassword() {
        trainee.setUserName("John.Doe");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("John.Doe", "newPassword");

        assertEquals("newPassword", trainee.getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void toggleActivation_ShouldUpdateStatus() {
        trainee.setUserName("John.Doe");
        trainee.setActive(true);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        traineeService.toggleActivation("John.Doe", false);

        assertFalse(trainee.isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void getUnassignedTrainers_ShouldReturnActiveTrainersNotAssignedToTrainee() {
        Trainer assigned = new Trainer();
        assigned.setUserName("Trainer.One");
        assigned.setActive(true);

        Trainer unassigned = new Trainer();
        unassigned.setUserName("Trainer.Two");
        unassigned.setActive(true);

        trainee.setUserName("John.Doe");
        trainee.setTrainers(List.of(assigned));

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findAll()).thenReturn(List.of(assigned, unassigned));

        List<Trainer> result = traineeService.getUnassignedTrainers("John.Doe");

        assertEquals(1, result.size());
        assertEquals("Trainer.Two", result.get(0).getUserName());
    }

    @Test
    void updateTrainersList_ShouldAssignMatchingTrainers() {
        Trainer t1 = new Trainer();
        t1.setUserName("Trainer.One");
        Trainer t2 = new Trainer();
        t2.setUserName("Trainer.Two");

        trainee.setUserName("John.Doe");
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainer> result = traineeService.updateTrainersList("John.Doe", List.of("Trainer.Two"));

        assertEquals(1, result.size());
        assertEquals("Trainer.Two", result.get(0).getUserName());
        verify(traineeDao).update(trainee);
    }
}