package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
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
    private TraineeEntity traineeEntity;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("Address");

        UserEntity userEntity = new UserEntity("John", "Doe", true);
        userEntity.setUserName("John.Doe");
        userEntity.setPassword("secret1234");

        traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(userEntity);
        traineeEntity.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeEntity.setAddress("Address");
    }

    @Test
    void createTrainee_ShouldGenerateUsernameAndTenCharacterPassword() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.save(any(TraineeEntity.class))).thenAnswer(invocation -> {
            TraineeEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        assertEquals(1L, result.getId());
        verify(traineeDao).save(any(TraineeEntity.class));
    }

    @Test
    void createTrainee_WhenUsernameExists_ShouldAddNumericSuffix() {
        when(traineeDao.findAll()).thenReturn(List.of(traineeEntity));
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.save(any(TraineeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.createTrainee(trainee);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void updateTrainee_WhenExists_ShouldUpdateManagedEntityAndReturnDomain() {
        trainee.setUserName("John.Doe");
        trainee.setFirstName("Updated");
        trainee.setAddress("New Address");

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

        Trainee result = traineeService.updateTrainee(trainee);

        assertEquals("Updated", result.getFirstName());
        assertEquals("New Address", result.getAddress());
        assertEquals("Updated", traineeEntity.getUser().getFirstName());
    }

    @Test
    void updateTrainee_WhenNotFound_ShouldThrowException() {
        trainee.setUserName("Missing.User");
        when(traineeDao.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> traineeService.updateTrainee(trainee));
    }

    @Test
    void deleteTraineeByUsername_ShouldFindAndDeleteById() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

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
    void getTraineeById_ShouldReturnMappedDomain() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(traineeEntity));

        Optional<Trainee> result = traineeService.getTraineeById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("John.Doe", result.get().getUserName());
    }

    @Test
    void authenticate_WhenCredentialsMatchAndActive_ShouldReturnTrue() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

        assertTrue(traineeService.authenticate("John.Doe", "secret1234"));
    }

    @Test
    void authenticate_WhenTraineeIsInactive_ShouldReturnFalse() {
        traineeEntity.getUser().setActive(false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

        assertFalse(traineeService.authenticate("John.Doe", "secret1234"));
    }

    @Test
    void changePassword_WhenTraineeExists_ShouldUpdatePassword() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

        traineeService.changePassword("John.Doe", "newPassword");

        assertEquals("newPassword", traineeEntity.getUser().getPassword());
    }

    @Test
    void toggleActivation_ShouldUpdateStatus() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));

        traineeService.toggleActivation("John.Doe", false);

        assertFalse(traineeEntity.getUser().isActive());
    }

    @Test
    void getUnassignedTrainers_ShouldReturnActiveTrainersNotAssignedToTrainee() {
        UserEntity u1 = new UserEntity("T", "One", true);
        u1.setUserName("Trainer.One");
        TrainerEntity assigned = new TrainerEntity();
        assigned.setUser(u1);

        UserEntity u2 = new UserEntity("T", "Two", true);
        u2.setUserName("Trainer.Two");
        TrainerEntity unassigned = new TrainerEntity();
        unassigned.setUser(u2);

        traineeEntity.setTrainers(List.of(assigned));

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));
        when(trainerDao.findAll()).thenReturn(List.of(assigned, unassigned));

        List<Trainer> result = traineeService.getUnassignedTrainers("John.Doe");

        assertEquals(1, result.size());
        assertEquals("Trainer.Two", result.get(0).getUserName());
    }

    @Test
    void updateTrainersList_ShouldAssignMatchingTrainers() {
        UserEntity u1 = new UserEntity("T", "One", true);
        u1.setUserName("Trainer.One");
        TrainerEntity t1 = new TrainerEntity();
        t1.setUser(u1);

        UserEntity u2 = new UserEntity("T", "Two", true);
        u2.setUserName("Trainer.Two");
        TrainerEntity t2 = new TrainerEntity();
        t2.setUser(u2);

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(traineeEntity));
        when(trainerDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainer> result = traineeService.updateTrainersList("John.Doe", List.of("Trainer.Two"));

        assertEquals(1, result.size());
        assertEquals("Trainer.Two", result.get(0).getUserName());
        assertEquals(1, traineeEntity.getTrainers().size());
    }
}