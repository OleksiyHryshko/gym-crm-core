package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.example.entity.TraineeEntity;
import org.example.entity.TrainerEntity;
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

    public Training save(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training must not be null");
        }
        TrainingEntity entity = toNewEntity(training);
        entityManager.persist(entity);
        return toDomain(entity);
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingEntity.class, id))
                .map(this::toDomain);
    }

    public List<Training> findAll() {
        return entityManager.createQuery("FROM TrainingEntity", TrainingEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public List<TrainingType> findAllTrainingTypes() {
        return entityManager.createQuery("FROM TrainingTypeEntity", TrainingTypeEntity.class)
                .getResultList()
                .stream()
                .map(tt -> new TrainingType(tt.getId(), tt.getTrainingTypeName()))
                .toList();
    }

    public List<Training> findTrainingsByTraineeUsernameAndCriteria(
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

        return query.getResultList().stream().map(this::toDomain).toList();
    }

    public List<Training> findTrainingsByTrainerUsernameAndCriteria(
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

        return query.getResultList().stream().map(this::toDomain).toList();
    }

    private TrainingEntity toNewEntity(Training domain) {
        TraineeEntity trainee = entityManager.createQuery(
                        "SELECT t FROM TraineeEntity t WHERE t.user.userName = :username", TraineeEntity.class)
                .setParameter("username", domain.getTraineeUsername())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + domain.getTraineeUsername()));

        TrainerEntity trainer = entityManager.createQuery(
                        "SELECT t FROM TrainerEntity t WHERE t.user.userName = :username", TrainerEntity.class)
                .setParameter("username", domain.getTrainerUsername())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + domain.getTrainerUsername()));

        String typeName = domain.getTrainingType() != null && domain.getTrainingType().trainingTypeName() != null
                ? domain.getTrainingType().trainingTypeName()
                : trainer.getSpecialization();

        TrainingTypeEntity trainingType = entityManager.createQuery(
                        "SELECT tt FROM TrainingTypeEntity tt WHERE tt.trainingTypeName = :name", TrainingTypeEntity.class)
                .setParameter("name", typeName)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + typeName));

        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingType(trainingType);
        entity.setTrainingName(domain.getTrainingName());
        entity.setTrainingDate(domain.getTrainingDate());
        entity.setTrainingDuration(domain.getTrainingDuration());
        return entity;
    }

    private Training toDomain(TrainingEntity entity) {
        Training domain = new Training();
        domain.setId(entity.getId());
        if (entity.getTrainee() != null && entity.getTrainee().getUser() != null) {
            domain.setTraineeUsername(entity.getTrainee().getUser().getUserName());
            domain.setTraineeName(entity.getTrainee().getUser().getFirstName() + " " + entity.getTrainee().getUser().getLastName());
        }
        if (entity.getTrainer() != null && entity.getTrainer().getUser() != null) {
            domain.setTrainerUsername(entity.getTrainer().getUser().getUserName());
            domain.setTrainerName(entity.getTrainer().getUser().getFirstName() + " " + entity.getTrainer().getUser().getLastName());
        }
        if (entity.getTrainingType() != null) {
            domain.setTrainingType(new TrainingType(
                    entity.getTrainingType().getId(),
                    entity.getTrainingType().getTrainingTypeName()
            ));
        }
        domain.setTrainingName(entity.getTrainingName());
        domain.setTrainingDate(entity.getTrainingDate());
        domain.setTrainingDuration(entity.getTrainingDuration());
        return domain;
    }
}