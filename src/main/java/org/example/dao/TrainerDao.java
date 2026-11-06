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
        if (trainer.getUserId() == null) {
            while (storage.containsKey(idCounter)) {
                idCounter++;
            }
            trainer.setUserId(idCounter++);
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
        storage.put(trainer.getUserId(), trainer);
        return trainer;
    }
}
