package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
import org.example.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TraineeEntity save(TraineeEntity trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        entityManager.persist(trainee);
        return trainee;
    }

    public Optional<TraineeEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TraineeEntity.class, id));
    }

    public Optional<TraineeEntity> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT t FROM TraineeEntity t WHERE t.user.userName = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
    public List<TraineeEntity> findAll() {
        return entityManager.createQuery("FROM TraineeEntity", TraineeEntity.class)
                .getResultList();
    }

    public TraineeEntity update(TraineeEntity trainee) {
        if (trainee == null || trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee and its ID must not be null");
        }
        return entityManager.merge(trainee);
    }

    public void delete(Long id) {
        TraineeEntity entity = entityManager.find(TraineeEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    public void deleteByUsername(String username) {
        List<TraineeEntity> result = entityManager.createQuery(
                        "SELECT t FROM TraineeEntity t WHERE t.user.userName = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultList();
        result.forEach(entityManager::remove);
    }



}