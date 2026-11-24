package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TraineeEntity;
import org.example.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    void save_ShouldCallPersistAndReturnEntity() {
        UserEntity user = new UserEntity("John", "Doe", true);
        user.setUserName("John.Doe");
        user.setPassword("secret1234");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 10));
        trainee.setAddress("Main St");

        TraineeEntity result = traineeDao.save(trainee);

        verify(entityManager, times(1)).persist(trainee);
        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUserName());
        assertEquals("Main St", result.getAddress());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnOptionalTraineeEntity() {
        UserEntity user = new UserEntity("John", "Doe", true);
        user.setUserName("John.Doe");
        TraineeEntity entity = new TraineeEntity();
        entity.setId(1L);
        entity.setUser(user);

        when(entityManager.find(TraineeEntity.class, 1L)).thenReturn(entity);

        Optional<TraineeEntity> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("John.Doe", result.get().getUser().getUserName());
    }

    @Test
    void findAll_ShouldReturnEntityList() {
        @SuppressWarnings("unchecked")
        TypedQuery<TraineeEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TraineeEntity", TraineeEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TraineeEntity(), new TraineeEntity()));

        List<TraineeEntity> result = traineeDao.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void update_ShouldCallMergeAndReturnEntity() {
        UserEntity user = new UserEntity("New", "Name", false);
        user.setUserName("New.Name");
        user.setPassword("pass123456");

        TraineeEntity entity = new TraineeEntity();
        entity.setId(1L);
        entity.setUser(user);

        when(entityManager.merge(entity)).thenReturn(entity);

        TraineeEntity result = traineeDao.update(entity);

        verify(entityManager, times(1)).merge(entity);
        assertEquals("New", result.getUser().getFirstName());
        assertFalse(result.getUser().isActive());
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        TraineeEntity traineeEntity = new TraineeEntity();
        when(entityManager.find(TraineeEntity.class, 1L)).thenReturn(traineeEntity);

        traineeDao.delete(1L);

        verify(entityManager, times(1)).remove(traineeEntity);
    }
}