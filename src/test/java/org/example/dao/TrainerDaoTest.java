package org.example.dao;

import org.example.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private TrainerDao trainerDao;
    private Map<Long, Trainer> storage;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainerDao = new TrainerDao(storage);
    }

    @Test
    void save_ShouldAssignIdAndStoreInMap() {
        Trainer trainer = new Trainer("Alice", "Smith", true, "Cardio");

        Trainer savedTrainer = trainerDao.save(trainer);

        assertNotNull(savedTrainer.getUserId());
        assertEquals(1, storage.size());
        assertEquals(savedTrainer, storage.get(savedTrainer.getUserId()));
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainer() {
        Trainer trainer = new Trainer("Bob", "Jones", true, "Weights");
        trainer.setUserId(1L);
        storage.put(1L, trainer);

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getFirstName());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        storage.put(1L, new Trainer("A", "A", true, "1"));
        storage.put(2L, new Trainer("B", "B", true, "2"));

        List<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }
}