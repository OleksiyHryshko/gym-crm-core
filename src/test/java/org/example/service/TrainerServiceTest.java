package org.example.service;

import org.example.TrainerService;
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

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        testTrainer = new Trainer("John", "Doe", true, "Fitness");
    }

    @Test
    void createTrainer_ShouldGenerateUsernameAndPassword() {
        when(trainerDao.findAll()).thenReturn(List.of());
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer saved = invocation.getArgument(0);
            saved.setUserId(1L);
            return saved;
        });

        Trainer result = trainerService.createTrainer(testTrainer);

        assertNotNull(result.getUserId());
        assertEquals("John.Doe", result.getUserName());
        assertNotNull(result.getPassword());
        assertEquals(10, result.getPassword().length());

        verify(trainerDao, times(1)).save(any(Trainer.class));
    }

    @Test
    void createTrainer_WhenUsernameExists_ShouldAddSuffix() {
        Trainer existingTrainer = new Trainer("John", "Doe", true, "Yoga");
        existingTrainer.setUserName("John.Doe");

        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(testTrainer);

        Trainer result = trainerService.createTrainer(testTrainer);

        assertEquals("John.Doe1", result.getUserName());
    }

    @Test
    void getTrainerById_ShouldReturnTrainer() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.getTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
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