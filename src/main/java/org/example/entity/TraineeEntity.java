package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainees")
public class TraineeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE)
    private List<TrainingEntity> trainings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private List<TrainerEntity> trainers = new ArrayList<>();

    public TraineeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<TrainingEntity> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<TrainingEntity> trainings) {
        this.trainings = trainings;
    }

    public List<TrainerEntity> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<TrainerEntity> trainers) {
        this.trainers = trainers;
    }
}