package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.model.Trainer;
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
class TrainerDaoTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrainerDao trainerDao;

    @Test
    void save_ShouldCallPersist() {
        Trainer trainer = new Trainer("Alice", "Smith", true, "Cardio");

        Trainer result = trainerDao.save(trainer);

        verify(entityManager, times(1)).persist(trainer);
        assertSame(trainer, result);
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainer() {
        Trainer trainer = new Trainer("Bob", "Jones", true, "Weights");
        when(entityManager.find(Trainer.class, 1L)).thenReturn(trainer);

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getFirstName());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        @SuppressWarnings("unchecked")
        TypedQuery<Trainer> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM Trainer", Trainer.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Trainer(), new Trainer()));

        List<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void update_ShouldCallMerge() {
        Trainer trainer = new Trainer("A", "A", true, "Cardio");
        when(entityManager.merge(trainer)).thenReturn(trainer);

        Trainer result = trainerDao.update(trainer);

        verify(entityManager, times(1)).merge(trainer);
        assertSame(trainer, result);
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        Trainer trainer = new Trainer("A", "A", true, "Cardio");
        when(entityManager.find(Trainer.class, 1L)).thenReturn(trainer);

        trainerDao.delete(1L);

        verify(entityManager, times(1)).remove(trainer);
    }
}