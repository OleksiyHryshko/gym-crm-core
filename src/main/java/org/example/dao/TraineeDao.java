package org.example.dao;

import org.example.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDao {

    private final Map<Long, Trainee> storage;
    private Long idCounter = 1L;

    @Autowired
    public TraineeDao(Map<Long, Trainee> traineeStorage) {
        this.storage = traineeStorage;
    }

    public Trainee save(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        if (trainee.getUserId() == null) {
            while (storage.containsKey(idCounter)) {
                idCounter++;
            }
            trainee.setUserId(idCounter++);
        } else if (storage.containsKey(trainee.getUserId())) {
            throw new IllegalArgumentException("Trainee with ID " + trainee.getUserId() + " already exists");
        }
        storage.put(trainee.getUserId(), trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Trainee update(Trainee trainee) {
        if (trainee == null || trainee.getUserId() == null) {
            throw new IllegalArgumentException("Trainee and trainee ID must not be null");
        }
        if (!storage.containsKey(trainee.getUserId())) {
            throw new IllegalArgumentException("Trainee with ID " + trainee.getUserId() + " does not exist");
        }
        storage.put(trainee.getUserId(), trainee);
        return trainee;
    }

    public void delete(Long id) {
        storage.remove(id);
    }
}