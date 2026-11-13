package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional
public class TraineeDao {
    @PersistenceContext
    private EntityManager entityManager;

    public Trainee save(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        entityManager.persist(trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    public List<Trainee> findAll() {
        return entityManager.createQuery("FROM Trainee", Trainee.class).getResultList();
    }

    public Trainee update(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        return entityManager.merge(trainee);
    }

    public void delete(Long id) {
        Trainee trainee = entityManager.find(Trainee.class, id);
        if (trainee != null) {
            entityManager.remove(trainee);
        }
    }
    public void deleteByUsername(String username) {
        entityManager.createQuery("DELETE FROM Trainee t WHERE t.user.userName = :username")
                .setParameter("username", username)
                .executeUpdate();
    }
}