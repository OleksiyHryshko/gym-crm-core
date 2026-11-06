package org.example.dao;

import org.example.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDao {

    private final Map<Long, Training> storage;
    private Long idCounter = 1L;

    @Autowired
    public TrainingDao(Map<Long, Training> trainingStorage) {
        this.storage = trainingStorage;
    }

    public Training save(Training training) {
        while (storage.containsKey(idCounter)) {
            idCounter++;
        }
        Long trainingId = idCounter++;
        storage.put(trainingId, training);
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }
}