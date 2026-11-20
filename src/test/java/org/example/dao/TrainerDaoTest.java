package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
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
    void save_ShouldCallPersistAndReturnEntity() {
        UserEntity user = new UserEntity("Alice", "Smith", true);
        user.setUserName("Alice.Smith");
        user.setPassword("secret1234");

        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(user);
        trainer.setSpecialization("Cardio");

        TrainerEntity result = trainerDao.save(trainer);

        verify(entityManager, times(1)).persist(trainer);
        assertNotNull(result);
        assertEquals("Alice", result.getUser().getFirstName());
        assertEquals("Cardio", result.getSpecialization());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainerEntity() {
        UserEntity user = new UserEntity("Bob", "Jones", true);
        TrainerEntity entity = new TrainerEntity();
        entity.setId(1L);
        entity.setUser(user);
        entity.setSpecialization("Weights");

        when(entityManager.find(TrainerEntity.class, 1L)).thenReturn(entity);

        Optional<TrainerEntity> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getUser().getFirstName());
        assertEquals("Weights", found.get().getSpecialization());
    }

    @Test
    void findAll_ShouldReturnAllTrainerEntities() {
        @SuppressWarnings("unchecked")
        TypedQuery<TrainerEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TrainerEntity", TrainerEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TrainerEntity(), new TrainerEntity()));

        List<TrainerEntity> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void update_ShouldCallMergeAndReturnEntity() {
        UserEntity user = new UserEntity("Updated", "Trainer", true);
        user.setUserName("Updated.Trainer");
        user.setPassword("pass123456");

        TrainerEntity entity = new TrainerEntity();
        entity.setId(1L);
        entity.setUser(user);
        entity.setSpecialization("Cardio");

        when(entityManager.merge(entity)).thenReturn(entity);

        TrainerEntity result = trainerDao.update(entity);

        verify(entityManager, times(1)).merge(entity);
        assertEquals("Updated", result.getUser().getFirstName());
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        TrainerEntity trainerEntity = new TrainerEntity();
        when(entityManager.find(TrainerEntity.class, 1L)).thenReturn(trainerEntity);

        trainerDao.delete(1L);

        verify(entityManager, times(1)).remove(trainerEntity);
    }
}