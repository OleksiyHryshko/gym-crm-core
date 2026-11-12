package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional
public class TrainerDao {
    @PersistenceContext
    EntityManager entityManager;

    public Trainer save(Trainer trainer) {
        if(trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        entityManager.persist(trainer);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    public List<Trainer> findAll() {
        return entityManager.createQuery("FROM Trainer", Trainer.class).getResultList();
    }

    public Trainer update(Trainer trainer) {
        if (trainer == null ) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        return entityManager.merge(trainer);
    }
    public void delete(Long id) {
        Trainer trainer = entityManager.find(Trainer.class, id);
        if (trainer != null) {
            entityManager.remove(trainer);
        }
    }
}
