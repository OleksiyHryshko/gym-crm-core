package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.entity.TrainingEntity;
import org.example.entity.TrainingTypeEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TrainingEntity save(TrainingEntity training) {
        if (training == null) {
            throw new IllegalArgumentException("Training must not be null");
        }
        entityManager.persist(training);
        return training;
    }

    public Optional<TrainingEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingEntity.class, id));
    }

    public List<TrainingEntity> findAll() {
        return entityManager.createQuery("FROM TrainingEntity", TrainingEntity.class)
                .getResultList();
    }

    public List<TrainingTypeEntity> findAllTrainingTypes() {
        return entityManager.createQuery("FROM TrainingTypeEntity", TrainingTypeEntity.class)
                .getResultList();
    }

    public Optional<TrainingTypeEntity> findTrainingTypeByName(String name) {
        return entityManager.createQuery(
                        "SELECT tt FROM TrainingTypeEntity tt WHERE tt.trainingTypeName = :name", TrainingTypeEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public List<TrainingEntity> findTrainingsByTraineeUsernameAndCriteria(
            String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {

        StringBuilder hql = new StringBuilder("SELECT t FROM TrainingEntity t WHERE t.trainee.user.userName = :username ");

        if (fromDate != null) hql.append("AND t.trainingDate >= :fromDate ");
        if (toDate != null) hql.append("AND t.trainingDate <= :toDate ");
        if (trainerName != null) hql.append("AND t.trainer.user.firstName = :trainerName ");
        if (trainingTypeName != null) hql.append("AND t.trainingType.trainingTypeName = :trainingTypeName ");

        TypedQuery<TrainingEntity> query = entityManager.createQuery(hql.toString(), TrainingEntity.class);
        query.setParameter("username", username);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null) query.setParameter("trainerName", trainerName);
        if (trainingTypeName != null) query.setParameter("trainingTypeName", trainingTypeName);

        return query.getResultList();
    }

    public List<TrainingEntity> findTrainingsByTrainerUsernameAndCriteria(
            String username, LocalDate fromDate, LocalDate toDate, String traineeName) {

        StringBuilder hql = new StringBuilder("SELECT t FROM TrainingEntity t WHERE t.trainer.user.userName = :username ");

        if (fromDate != null) hql.append("AND t.trainingDate >= :fromDate ");
        if (toDate != null) hql.append("AND t.trainingDate <= :toDate ");
        if (traineeName != null) hql.append("AND t.trainee.user.firstName = :traineeName ");

        TypedQuery<TrainingEntity> query = entityManager.createQuery(hql.toString(), TrainingEntity.class);
        query.setParameter("username", username);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null) query.setParameter("traineeName", traineeName);

        return query.getResultList();
    }
}