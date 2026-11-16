package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.model.Trainee;
import org.example.model.Trainer;
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

    public Trainee save(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee must not be null");
        }
        TraineeEntity entity = toNewEntity(trainee);
        entityManager.persist(entity);
        return toDomain(entity);
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TraineeEntity.class, id))
                .map(this::toDomain);
    }

    public Optional<Trainee> findByUsername(String username) {
        List<TraineeEntity> result = entityManager.createQuery(
                        "SELECT t FROM TraineeEntity t WHERE t.user.userName = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst().map(this::toDomain);
    }

    public List<Trainee> findAll() {
        return entityManager.createQuery("FROM TraineeEntity", TraineeEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Trainee update(Trainee trainee) {
        if (trainee == null || trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee and its ID must not be null");
        }
        TraineeEntity managed = entityManager.find(TraineeEntity.class, trainee.getId());
        if (managed == null) {
            throw new IllegalArgumentException("Trainee not found with ID: " + trainee.getId());
        }

        managed.getUser().setFirstName(trainee.getFirstName());
        managed.getUser().setLastName(trainee.getLastName());
        managed.getUser().setUserName(trainee.getUserName());
        managed.getUser().setPassword(trainee.getPassword());
        managed.getUser().setActive(trainee.isActive());
        managed.setDateOfBirth(trainee.getDateOfBirth());
        managed.setAddress(trainee.getAddress());

        if (trainee.getTrainers() != null) {
            List<TrainerEntity> trainerEntities = trainee.getTrainers().stream()
                    .map(t -> entityManager.getReference(TrainerEntity.class, t.getId()))
                    .toList();
            managed.setTrainers(new java.util.ArrayList<>(trainerEntities));
        }

        return toDomain(entityManager.merge(managed));
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



    private TraineeEntity toNewEntity(Trainee domain) {
        UserEntity user = new UserEntity();
        user.setFirstName(domain.getFirstName());
        user.setLastName(domain.getLastName());
        user.setUserName(domain.getUserName());
        user.setPassword(domain.getPassword());
        user.setActive(domain.isActive());

        TraineeEntity entity = new TraineeEntity();
        entity.setUser(user);
        entity.setDateOfBirth(domain.getDateOfBirth());
        entity.setAddress(domain.getAddress());
        return entity;
    }

    private Trainee toDomain(TraineeEntity entity) {
        Trainee domain = new Trainee();
        domain.setId(entity.getId());
        if (entity.getUser() != null) {
            domain.setFirstName(entity.getUser().getFirstName());
            domain.setLastName(entity.getUser().getLastName());
            domain.setUserName(entity.getUser().getUserName());
            domain.setPassword(entity.getUser().getPassword());
            domain.setActive(entity.getUser().isActive());
        }
        domain.setDateOfBirth(entity.getDateOfBirth());
        domain.setAddress(entity.getAddress());

        if (entity.getTrainers() != null) {
            List<Trainer> trainers = entity.getTrainers().stream()
                    .map(this::toTrainerDomainWithoutTrainees)
                    .toList();
            domain.setTrainers(trainers);
        }
        return domain;
    }

    private Trainer toTrainerDomainWithoutTrainees(TrainerEntity entity) {
        Trainer trainer = new Trainer();
        trainer.setId(entity.getId());
        trainer.setSpecialization(entity.getSpecialization());
        if (entity.getUser() != null) {
            trainer.setFirstName(entity.getUser().getFirstName());
            trainer.setLastName(entity.getUser().getLastName());
            trainer.setUserName(entity.getUser().getUserName());
            trainer.setPassword(entity.getUser().getPassword());
            trainer.setActive(entity.getUser().isActive());
        }
        return trainer;
    }
}