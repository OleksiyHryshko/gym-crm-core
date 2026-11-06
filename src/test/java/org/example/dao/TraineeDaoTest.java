package org.example.dao;

import org.example.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoTest {

    private Map<Long, Trainee> storage;
    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        traineeDao = new TraineeDao(storage);
    }

    @Test
    void save_ShouldAssignIdAndStoreTrainee() {
        Trainee trainee = trainee("John");

        Trainee saved = traineeDao.save(trainee);

        assertEquals(1L, saved.getUserId());
        assertSame(saved, storage.get(1L));
    }

    @Test
    void findById_WhenPresent_ShouldReturnTrainee() {
        Trainee trainee = trainee("John");
        storage.put(5L, trainee);

        assertEquals(java.util.Optional.of(trainee), traineeDao.findById(5L));
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        storage.put(1L, trainee("John"));
        storage.put(2L, trainee("Jane"));

        assertEquals(2, traineeDao.findAll().size());
    }

    @Test
    void update_ShouldReplaceTraineeWithSameId() {
        Trainee original = trainee("John");
        original.setUserId(1L);
        storage.put(1L, original);
        Trainee updated = trainee("Jane");
        updated.setUserId(1L);

        assertSame(updated, traineeDao.update(updated));
        assertSame(updated, storage.get(1L));
    }

    @Test
    void delete_ShouldRemoveTrainee() {
        storage.put(1L, trainee("John"));

        traineeDao.delete(1L);

        assertFalse(storage.containsKey(1L));
    }

    private Trainee trainee(String firstName) {
        return new Trainee(firstName, "Doe", true, LocalDate.of(1990, 1, 1), "Address");
    }
}
