package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.TrainingEntity;
import org.example.entity.TrainingTypeEntity;
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
    void save_ShouldCallPersistAndReturnEntity() {
        TrainingEntity training = new TrainingEntity();
        training.setTrainee(new TraineeEntity());
        training.setTrainer(new TrainerEntity());
        training.setTrainingType(new TrainingTypeEntity("Cardio"));
        training.setTrainingName("Morning Run");
        training.setTrainingDate(LocalDate.of(2026, 1, 1));
        training.setTrainingDuration(45);

        TrainingEntity result = trainingDao.save(training);

        verify(entityManager, times(1)).persist(training);
        assertNotNull(result);
        assertEquals("Morning Run", result.getTrainingName());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainingEntity() {
        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(1L);
        trainingEntity.setTrainingName("HIIT");

        when(entityManager.find(TrainingEntity.class, 1L)).thenReturn(trainingEntity);

        Optional<TrainingEntity> found = trainingDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("HIIT", found.get().getTrainingName());
    }

    @Test
    void findAll_ShouldReturnAllTrainingEntities() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TrainingEntity", TrainingEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainingEntity(), new TrainingEntity()));

        List<TrainingEntity> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findTrainingTypeByName_WhenExists_ShouldReturnOptionalEntity() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingTypeEntity> query = mock(TypedQuery.class);
        TrainingTypeEntity typeEntity = new TrainingTypeEntity("Cardio");

        when(entityManager.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.setParameter("name", "Cardio")).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(typeEntity));

        Optional<TrainingTypeEntity> result = trainingDao.findTrainingTypeByName("Cardio");

        assertTrue(result.isPresent());
        assertEquals("Cardio", result.get().getTrainingTypeName());
    }

    @Test
    void findTrainingsByTraineeUsernameAndCriteria_ShouldExecuteQuery() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainingEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainingEntity()));

        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        List<TrainingEntity> result = trainingDao.findTrainingsByTraineeUsernameAndCriteria(
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

        List<TrainingEntity> result = trainingDao.findTrainingsByTrainerUsernameAndCriteria(
                "Jane.Doe", null, LocalDate.now(), "John");

        assertEquals(2, result.size());
        verify(query).setParameter("username", "Jane.Doe");
        verify(query).setParameter("traineeName", "John");
    }
}