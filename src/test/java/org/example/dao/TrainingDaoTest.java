package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.TrainingEntity;
import org.example.entity.TrainingTypeEntity;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrainingDao trainingDao;

    @Test
    void save_ShouldResolveEntitiesAndCallPersist() {
        @SuppressWarnings("unchecked")
        TypedQuery<TraineeEntity> traineeQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<TrainerEntity> trainerQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingTypeEntity> typeQuery = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Doe")).thenReturn(traineeQuery);
        when(traineeQuery.getResultStream()).thenReturn(Stream.of(new TraineeEntity()));

        when(entityManager.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Jane.Smith")).thenReturn(trainerQuery);
        when(trainerQuery.getResultStream()).thenReturn(Stream.of(new TrainerEntity()));

        when(entityManager.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(typeQuery);
        when(typeQuery.setParameter("name", "Cardio")).thenReturn(typeQuery);
        when(typeQuery.getResultStream()).thenReturn(Stream.of(new TrainingTypeEntity("Cardio")));

        Training training = new Training();
        training.setTraineeUsername("John.Doe");
        training.setTrainerUsername("Jane.Smith");
        training.setTrainingName("Morning Run");
        training.setTrainingType(new TrainingType(1L, "Cardio"));
        training.setTrainingDate(LocalDate.of(2026, 1, 1));
        training.setTrainingDuration(45);

        Training result = trainingDao.save(training);

        verify(entityManager, times(1)).persist(any(TrainingEntity.class));
        assertNotNull(result);
        assertEquals("Morning Run", result.getTrainingName());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTraining() {
        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(1L);
        trainingEntity.setTrainingName("HIIT");

        when(entityManager.find(TrainingEntity.class, 1L)).thenReturn(trainingEntity);

        Optional<Training> found = trainingDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("HIIT", found.get().getTrainingName());
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TrainingEntity", TrainingEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainingEntity(), new TrainingEntity()));

        List<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findTrainingsByTraineeUsernameAndCriteria_ShouldExecuteQuery() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainingEntity()));

        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        List<Training> result = trainingDao.findTrainingsByTraineeUsernameAndCriteria(
                "John.Doe", fromDate, null, "Jane", null);

        assertEquals(1, result.size());
        verify(query).setParameter("username", "John.Doe");
        verify(query).setParameter("fromDate", fromDate);
        verify(query).setParameter("trainerName", "Jane");
    }

    @Test
    void findTrainingsByTrainerUsernameAndCriteria_ShouldExecuteQuery() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainingEntity(), new TrainingEntity()));

        List<Training> result = trainingDao.findTrainingsByTrainerUsernameAndCriteria(
                "Jane.Doe", null, LocalDate.now(), "John");

        assertEquals(2, result.size());
        verify(query).setParameter("username", "Jane.Doe");
        verify(query).setParameter("traineeName", "John");
    }
}