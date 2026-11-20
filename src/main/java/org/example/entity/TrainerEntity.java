package org.example.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
public class TrainerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "specialization", nullable = false)
    private String specialization;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @ManyToMany(mappedBy = "trainers")
    private List<TraineeEntity> trainees = new ArrayList<>();

    @OneToMany(mappedBy = "trainer")
    private List<TrainingEntity> trainings = new ArrayList<>();

    public TrainerEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<TraineeEntity> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<TraineeEntity> trainees) {
        this.trainees = trainees;
    }

    public List<TrainingEntity> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<TrainingEntity> trainings) {
        this.trainings = trainings;
    }
}