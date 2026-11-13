package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
@Transactional
public class TrainingDao {
    @PersistenceContext
    private EntityManager entityManager;



    public Training save(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training must not be null");
        }
        entityManager.persist(training);
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    public List<Training> findAll() {
        return entityManager.createQuery("FROM Training", Training.class).getResultList();
    }

    public List<Training> findTrainingsByTraineeUsernameAndCriteria(
            String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {

        StringBuilder hql = new StringBuilder("SELECT t FROM Training t WHERE t.trainee.user.userName = :username ");

        if (fromDate != null) hql.append("AND t.trainingDate >= :fromDate ");
        if (toDate != null) hql.append("AND t.trainingDate <= :toDate ");
        if (trainerName != null) hql.append("AND t.trainer.user.firstName = :trainerName ");
        if (trainingTypeName != null) hql.append("AND t.trainingType.trainingTypeName = :trainingTypeName ");

        TypedQuery<Training> query = entityManager.createQuery(hql.toString(), Training.class);
        query.setParameter("username", username);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null) query.setParameter("trainerName", trainerName);
        if (trainingTypeName != null) query.setParameter("trainingTypeName", trainingTypeName);

        return query.getResultList();
    }

    public List<Training> findTrainingsByTrainerUsernameAndCriteria(
            String username, LocalDate fromDate, LocalDate toDate, String traineeName) {

        StringBuilder hql = new StringBuilder("SELECT t FROM Training t WHERE t.trainer.user.userName = :username ");

        if (fromDate != null) hql.append("AND t.trainingDate >= :fromDate ");
        if (toDate != null) hql.append("AND t.trainingDate <= :toDate ");
        if (traineeName != null) hql.append("AND t.trainee.user.firstName = :traineeName ");

        TypedQuery<Training> query = entityManager.createQuery(hql.toString(), Training.class);
        query.setParameter("username", username);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null) query.setParameter("traineeName", traineeName);

        return query.getResultList();
    }
}