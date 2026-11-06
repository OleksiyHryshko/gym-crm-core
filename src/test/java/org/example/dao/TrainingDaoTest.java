package org.example.dao;

import org.example.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private Map<Long, Training> storage;
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDao = new TrainingDao(storage);
    }

    @Test
    void save_ShouldStoreTrainingAndReturnIt() {
        Training training = new Training();

        assertSame(training, trainingDao.save(training));
        assertEquals(1, storage.size());
        assertSame(training, storage.get(1L));
    }

    @Test
    void findById_WhenPresent_ShouldReturnTraining() {
        Training training = new Training();
        storage.put(4L, training);

        assertEquals(java.util.Optional.of(training), trainingDao.findById(4L));
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        storage.put(1L, new Training());
        storage.put(2L, new Training());

        assertEquals(2, trainingDao.findAll().size());
    }
}
