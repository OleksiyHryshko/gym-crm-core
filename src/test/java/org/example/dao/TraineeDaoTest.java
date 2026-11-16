package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.entity.TraineeEntity;
import org.example.entity.UserEntity;
import org.example.model.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TraineeDao traineeDao;

    @Test
    void save_ShouldCallPersistAndReturnDomainModel() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUserName("John.Doe");
        trainee.setPassword("secret1234");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1995, 5, 10));
        trainee.setAddress("Main St");

        Trainee result = traineeDao.save(trainee);

        verify(entityManager, times(1)).persist(any(TraineeEntity.class));
        assertNotNull(result);
        assertEquals("John.Doe", result.getUserName());
        assertEquals("Main St", result.getAddress());
    }

    @Test
    void save_WhenNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null));
    }

    @Test
    void findById_WhenExists_ShouldReturnOptionalTrainee() {
        UserEntity user = new UserEntity("John", "Doe", true);
        user.setUserName("John.Doe");
        TraineeEntity entity = new TraineeEntity();
        entity.setId(1L);
        entity.setUser(user);

        when(entityManager.find(TraineeEntity.class, 1L)).thenReturn(entity);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("John.Doe", result.get().getUserName());
    }

    @Test
    void findAll_ShouldReturnMappedList() {
        @SuppressWarnings("unchecked")
        TypedQuery<TraineeEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("FROM TraineeEntity", TraineeEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TraineeEntity(), new TraineeEntity()));

        List<Trainee> result = traineeDao.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void update_ShouldUpdateManagedEntityAndCallMerge() {
        UserEntity existingUser = new UserEntity("Old", "Name", true);
        TraineeEntity managed = new TraineeEntity();
        managed.setId(1L);
        managed.setUser(existingUser);

        Trainee updateRequest = new Trainee();
        updateRequest.setId(1L);
        updateRequest.setFirstName("New");
        updateRequest.setLastName("Name");
        updateRequest.setUserName("New.Name");
        updateRequest.setPassword("pass123456");
        updateRequest.setActive(false);

        when(entityManager.find(TraineeEntity.class, 1L)).thenReturn(managed);
        when(entityManager.merge(managed)).thenReturn(managed);

        Trainee result = traineeDao.update(updateRequest);

        verify(entityManager, times(1)).merge(managed);
        assertEquals("New", result.getFirstName());
        assertFalse(result.isActive());
    }

    @Test
    void delete_WhenExists_ShouldCallRemove() {
        TraineeEntity traineeEntity = new TraineeEntity();
        when(entityManager.find(TraineeEntity.class, 1L)).thenReturn(traineeEntity);

        traineeDao.delete(1L);

        verify(entityManager, times(1)).remove(traineeEntity);
    }
}