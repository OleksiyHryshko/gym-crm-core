package org.example.service;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
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

    @BeforeEach
    void setUp() {
        testTrainer = new Trainer();
        testTrainer.setFirstName("John");
        testTrainer.setLastName("Doe");
        testTrainer.setActive(true);
        testTrainer.setSpecialization("Fitness");
    }

    @Test
    void createTrainer_ShouldGenerateUsernameAndPassword() {
        when(trainerDao.findAll()).thenReturn(List.of());
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Trainer result = trainerService.createTrainer(testTrainer);

        assertNotNull(result.getId());
        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());
        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void createTrainer_WhenUsernameExists_ShouldAddSuffix() {
        Trainer existingTrainer = new Trainer();
        existingTrainer.setUserName("John.Doe");

        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer result = trainerService.createTrainer(testTrainer);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void updateTrainer_WhenExists_ShouldUpdateAndDelegateToDao() {
        testTrainer.setUserName("John.Doe");
        Trainer existing = new Trainer();
        existing.setId(1L);
        existing.setUserName("John.Doe");

        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(existing));
        when(trainerDao.update(existing)).thenReturn(existing);

        Trainer result = trainerService.updateTrainer(testTrainer);

        assertSame(existing, result);
        assertEquals("John", existing.getFirstName());
        verify(trainerDao).update(existing);
    }

    @Test
    void updateTrainer_WhenNotFound_ShouldThrowException() {
        testTrainer.setUserName("Missing.Trainer");
        when(trainerDao.findByUsername("Missing.Trainer")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainerService.updateTrainer(testTrainer));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void getTrainerById_ShouldReturnTrainer() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.getTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void authenticate_WhenCredentialsMatch_ShouldReturnTrue() {
        testTrainer.setUserName("John.Doe");
        testTrainer.setPassword("pass123456");
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainer));

        assertTrue(trainerService.authenticate("John.Doe", "pass123456"));
    }

    @Test
    void changePassword_WhenTrainerExists_ShouldUpdatePassword() {
        testTrainer.setUserName("John.Doe");
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainer));

        trainerService.changePassword("John.Doe", "newPass123");

        assertEquals("newPass123", testTrainer.getPassword());
        verify(trainerDao).update(testTrainer);
    }

    @Test
    void toggleActivation_ShouldUpdateStatus() {
        testTrainer.setUserName("John.Doe");
        testTrainer.setActive(true);
        when(trainerDao.findByUsername("John.Doe")).thenReturn(Optional.of(testTrainer));

        trainerService.toggleActivation("John.Doe", false);

        assertFalse(testTrainer.isActive());
        verify(trainerDao).update(testTrainer);
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