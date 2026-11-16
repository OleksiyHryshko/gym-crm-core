package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
import org.example.model.Trainer;
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
class TrainerDaoTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrainerDao trainerDao;

    @Test
    void save_ShouldCallPersistAndReturnDomainModel() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Alice");
        trainer.setLastName("Smith");
        trainer.setUserName("Alice.Smith");
        trainer.setPassword("secret1234");
        trainer.setActive(true);
        trainer.setSpecialization("Cardio");

        Trainer result = trainerDao.save(trainer);

        verify(entityManager, times(1)).persist(any(TrainerEntity.class));
        assertNotNull(result);
        assertEquals("Alice", result.getFirstName());
        assertEquals("Cardio", result.getSpecialization());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainer() {
        UserEntity user = new UserEntity("Bob", "Jones", true);
        TrainerEntity entity = new TrainerEntity();
        entity.setId(1L);
        entity.setUser(user);
        entity.setSpecialization("Weights");

        when(entityManager.find(TrainerEntity.class, 1L)).thenReturn(entity);

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getFirstName());
        assertEquals("Weights", found.get().getSpecialization());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainerEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TrainerEntity", TrainerEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainerEntity(), new TrainerEntity()));

        List<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void update_ShouldUpdateManagedEntityAndCallMerge() {
        UserEntity user = new UserEntity("A", "A", true);
        TrainerEntity managed = new TrainerEntity();
        managed.setId(1L);
        managed.setUser(user);
        managed.setSpecialization("Cardio");

        Trainer updateRequest = new Trainer();
        updateRequest.setId(1L);
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Trainer");
        updateRequest.setUserName("Updated.Trainer");
        updateRequest.setPassword("pass123456");
        updateRequest.setActive(true);
        updateRequest.setSpecialization("Cardio");

        when(entityManager.find(TrainerEntity.class, 1L)).thenReturn(managed);
        when(entityManager.merge(managed)).thenReturn(managed);

        Trainer result = trainerDao.update(updateRequest);

        verify(entityManager, times(1)).merge(managed);
        assertEquals("Updated", result.getFirstName());
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        TrainerEntity trainerEntity = new TrainerEntity();
        when(entityManager.find(TrainerEntity.class, 1L)).thenReturn(trainerEntity);

        trainerDao.delete(1L);

        verify(entityManager, times(1)).remove(trainerEntity);
    }
}