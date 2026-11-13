package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.model.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    void save_ShouldCallPersist() {
        Training training = new Training();

        Training result = trainingDao.save(training);

        verify(entityManager, times(1)).persist(training);
        assertSame(training, result);
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTraining() {
        Training training = new Training();
        when(entityManager.find(Training.class, 1L)).thenReturn(training);

        Optional<Training> found = trainingDao.findById(1L);

        assertTrue(found.isPresent());
        assertSame(training, found.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        @SuppressWarnings("unchecked")
        TypedQuery<Training> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM Training", Training.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Training(), new Training()));

        List<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findTrainingsByTraineeUsernameAndCriteria_ShouldExecuteQuery() {
        @SuppressWarnings("unchecked")
        TypedQuery<Training> query = mock(TypedQuery.class);
        // Мокаємо створення будь-якого HQL запиту для Training
        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Training()));

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
        TypedQuery<Training> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Training(), new Training()));

        List<Training> result = trainingDao.findTrainingsByTrainerUsernameAndCriteria(
                "Jane.Doe", null, LocalDate.now(), "John");

        assertEquals(2, result.size());
        verify(query).setParameter("username", "Jane.Doe");
        verify(query).setParameter("traineeName", "John");
    }
}