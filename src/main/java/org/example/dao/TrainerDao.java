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
public class TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainer save(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer must not be null");
        }
        TrainerEntity entity = toNewEntity(trainer);
        entityManager.persist(entity);
        return toDomain(entity);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainerEntity.class, id))
                .map(this::toDomain);
    }

    public Optional<Trainer> findByUsername(String username) {
        List<TrainerEntity> result = entityManager.createQuery(
                        "SELECT t FROM TrainerEntity t WHERE t.user.userName = :username", TrainerEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result.stream().findFirst().map(this::toDomain);
    }

    public List<Trainer> findAll() {
        return entityManager.createQuery("FROM TrainerEntity", TrainerEntity.class)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public Trainer update(Trainer trainer) {
        if (trainer == null || trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer and its ID must not be null");
        }
        TrainerEntity managed = entityManager.find(TrainerEntity.class, trainer.getId());
        if (managed == null) {
            throw new IllegalArgumentException("Trainer not found with ID: " + trainer.getId());
        }

        managed.getUser().setFirstName(trainer.getFirstName());
        managed.getUser().setLastName(trainer.getLastName());
        managed.getUser().setUserName(trainer.getUserName());
        managed.getUser().setPassword(trainer.getPassword());
        managed.getUser().setActive(trainer.isActive());
        managed.setSpecialization(trainer.getSpecialization());

        return toDomain(entityManager.merge(managed));
    }

    public void delete(Long id) {
        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    private TrainerEntity toNewEntity(Trainer domain) {
        UserEntity user = new UserEntity();
        user.setFirstName(domain.getFirstName());
        user.setLastName(domain.getLastName());
        user.setUserName(domain.getUserName());
        user.setPassword(domain.getPassword());
        user.setActive(domain.isActive());

        TrainerEntity entity = new TrainerEntity();
        entity.setUser(user);
        entity.setSpecialization(domain.getSpecialization());
        return entity;
    }

    private Trainer toDomain(TrainerEntity entity) {
        Trainer domain = new Trainer();
        domain.setId(entity.getId());
        domain.setSpecialization(entity.getSpecialization());
        if (entity.getUser() != null) {
            domain.setFirstName(entity.getUser().getFirstName());
            domain.setLastName(entity.getUser().getLastName());
            domain.setUserName(entity.getUser().getUserName());
            domain.setPassword(entity.getUser().getPassword());
            domain.setActive(entity.getUser().isActive());
        }
        if (entity.getTrainees() != null) {
            List<Trainee> trainees = entity.getTrainees().stream()
                    .map(this::toTraineeDomainWithoutTrainers)
                    .toList();
            domain.setTrainees(trainees);
        }
        return domain;
    }

    private Trainee toTraineeDomainWithoutTrainers(TraineeEntity entity) {
        Trainee trainee = new Trainee();
        trainee.setId(entity.getId());
        trainee.setDateOfBirth(entity.getDateOfBirth());
        trainee.setAddress(entity.getAddress());
        if (entity.getUser() != null) {
            trainee.setFirstName(entity.getUser().getFirstName());
            trainee.setLastName(entity.getUser().getLastName());
            trainee.setUserName(entity.getUser().getUserName());
            trainee.setPassword(entity.getUser().getPassword());
            trainee.setActive(entity.getUser().isActive());
        }
        return trainee;
    }
}