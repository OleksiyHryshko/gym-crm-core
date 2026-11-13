package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.model.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TraineeDao traineeDao;

    @Test
    void save_ShouldCallPersist() {
        Trainee trainee = new Trainee();

        Trainee result = traineeDao.save(trainee);

        verify(entityManager, times(1)).persist(trainee);
        assertSame(trainee, result);
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnOptionalTrainee() {
        Trainee trainee = new Trainee();
        when(entityManager.find(Trainee.class, 1L)).thenReturn(trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }

    @Test
    void findAll_ShouldReturnList() {
        @SuppressWarnings("unchecked")
        TypedQuery<Trainee> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM Trainee", Trainee.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Trainee(), new Trainee()));

        List<Trainee> result = traineeDao.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void update_ShouldCallMerge() {
        Trainee trainee = new Trainee();
        when(entityManager.merge(trainee)).thenReturn(trainee);

        Trainee result = traineeDao.update(trainee);

        verify(entityManager, times(1)).merge(trainee);
        assertSame(trainee, result);
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        Trainee trainee = new Trainee();
        when(entityManager.find(Trainee.class, 1L)).thenReturn(trainee);

        traineeDao.delete(1L);

        verify(entityManager, times(1)).remove(trainee);
    }
}