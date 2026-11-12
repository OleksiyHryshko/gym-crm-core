package org.example.model;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy= jakarta.persistence.GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @Column(nullable = false)
    private String trainingName;
    @Column(nullable = false)
    private LocalDate trainingDate;
    @Column(nullable = false)
    private Integer trainingDuration;

    public Training() {
    }

    public Training(Trainee trainee, Trainer trainer, String trainingName, TrainingType trainingType, LocalDate trainingDate, Integer trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Training(Long traineeId, Long trainerId, String trainingName, TrainingType trainingType,
                    LocalDate trainingDate, Integer trainingDuration) {
        this.trainee = traineeId == null ? null : traineeWithId(traineeId);
        this.trainer = trainerId == null ? null : trainerWithId(trainerId);
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Long getId() {
        return id;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public void setTrainee(Trainee trainee) {
        this.trainee = trainee;
    }

    public Trainer getTrainer() {
        return trainer;
    }
    public Long getTrainerId() {
        return trainer == null ? null : trainer.getId();
    }
    public Long getTraineeId() {
        return trainee == null ? null : trainee.getId();
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
    public void setTrainingId(Long id) {
        this.id = id;
    }

    public Long getTrainingId() {
        return id;
    }

    public void setTraineeId(Long traineeId) {
        this.trainee = traineeId == null ? null : traineeWithId(traineeId);
    }

    public void setTrainerId(Long trainerId) {
        this.trainer = trainerId == null ? null : trainerWithId(trainerId);
    }

    private static Trainee traineeWithId(Long id) {
        Trainee trainee = new Trainee();
        trainee.setId(id);
        return trainee;
    }

    private static Trainer trainerWithId(Long id) {
        Trainer trainer = new Trainer();
        trainer.setId(id);
        return trainer;
    }
}