package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
import org.example.model.Trainer;
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
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;
    private TrainerEntity trainerEntity;

    @BeforeEach
    void setUp() {
        testTrainer = new Trainer();
        testTrainer.setFirstName("John");
        testTrainer.setLastName("Doe");
        testTrainer.setActive(true);
        testTrainer.setSpecialization("Fitness");

        UserEntity userEntity = new UserEntity("John", "Doe", true);
        userEntity.setUserName("John.Doe");
        userEntity.setPassword("pass123456");

        trainerEntity = new TrainerEntity();
        trainerEntity.setId(1L);
        trainerEntity.setUser(userEntity);
        trainerEntity.setSpecialization("Fitness");
    }

    @Test
    void createTrainer_ShouldGenerateUsernameAndPassword() {
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any(TrainerEntity.class))).thenAnswer(invocation -> {
            TrainerEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Trainer result = trainerService.createTrainer(testTrainer);

        assertNotNull(result.getId());
        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        verify(trainerDao, times(1)).save(any(TrainerEntity.class));
    }

    @Test
    void createTrainer_WhenUsernameExists_ShouldAddSuffix() {
        when(trainerDao.findAll()).thenReturn(List.of(trainerEntity));
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any(TrainerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = trainerService.createTrainer(testTrainer);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void updateTrainer_WhenExists_ShouldUpdateProfileAndPreserveSpecialization() {
        testTrainer.setUserName("John.Doe");
        testTrainer.setFirstName("Updated");
        testTrainer.setSpecialization("Yoga"); // Should be ignored

        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainerEntity));

        Trainer result = trainerService.updateTrainer(testTrainer);

        assertEquals("Updated", result.getFirstName());
        assertEquals("Fitness", result.getSpecialization());
        assertEquals("Fitness", trainerEntity.getSpecialization());
    }

    @Test
    void updateTrainer_WhenNotFound_ShouldThrowException() {
        testTrainer.setUserName("Missing.Trainer");
        when(trainerDao.findByUsername("Missing.Trainer")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainerService.updateTrainer(testTrainer));
    }

    @Test
    void getTrainerById_ShouldReturnMappedDomain() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainerEntity));

        Optional<Trainer> result = trainerService.getTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void authenticate_WhenCredentialsMatchAndActive_ShouldReturnTrue() {
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainerEntity));

        assertTrue(trainerService.authenticate("John.Doe", "pass123456"));
    }

    @Test
    void authenticate_WhenTrainerIsInactive_ShouldReturnFalse() {
        trainerEntity.getUser().setActive(false);
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainerEntity));

        assertFalse(trainerService.authenticate("John.Doe", "pass123456"));
    }

    @Test
    void changePassword_WhenTrainerExists_ShouldUpdatePassword() {
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainerEntity));

        trainerService.changePassword("John.Doe", "newPass123");

        assertEquals("newPass123", trainerEntity.getUser().getPassword());
    }

    @Test
    void toggleActivation_ShouldUpdateStatus() {
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainerEntity));

        trainerService.toggleActivation("John.Doe", false);

        assertFalse(trainerEntity.getUser().isActive());
    }

    @Test
    void createTrainer_WhenSpecializationIsNull_ShouldThrowException() {
        testTrainer.setSpecialization(null);

        assertThrows(IllegalArgumentException.class, () -> trainerService.createTrainer(testTrainer));
    }

    @Test
    void createTrainer_WhenTrainerIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.createTrainer(null));
    }
}