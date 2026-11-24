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
public class TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TrainerEntity save(TrainerEntity trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        entityManager.persist(trainer);
        return trainer;
    }

    public Optional<TrainerEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainerEntity.class, id));
    }

    public Optional<TrainerEntity> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT t FROM TrainerEntity t WHERE t.user.userName = :username", TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream().findFirst();
    }

    public List<TrainerEntity> findAll() {
        return entityManager.createQuery("FROM TrainerEntity", TrainerEntity.class)
                .getResultList();
    }

    public TrainerEntity update(TrainerEntity trainer) {
        if (trainer == null || trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer and its ID must not be null");
        }
        return entityManager.merge(trainer);
    }

    public void delete(Long id) {
        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }


}