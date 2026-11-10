package org.example.dao;

import org.example.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDao {

    private final Map<Long, Trainer> storage;
    private Long idCounter = 1L;

    @Autowired
    public TrainerDao(Map<Long, Trainer> trainerStorage) {
        this.storage = trainerStorage;
    }
    public Trainer save(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        if (trainer.getUserId() == null) {
            while (storage.containsKey(idCounter)) {
                idCounter++;
            }
            trainer.setUserId(idCounter++);
        } else if (storage.containsKey(trainer.getUserId())) {
            throw new IllegalArgumentException("Trainer with ID " + trainer.getUserId() + " already exists");
        }
        storage.put(trainer.getUserId(), trainer);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Trainer> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Trainer update(Trainer trainer) {
        if (trainer == null || trainer.getUserId() == null) {
            throw new IllegalArgumentException("Trainer and trainer ID must not be null");
        }
        if (!storage.containsKey(trainer.getUserId())) {
            throw new IllegalArgumentException("Trainer with ID " + trainer.getUserId() + " does not exist");
        }
        storage.put(trainer.getUserId(), trainer);
        return trainer;
    }
}
