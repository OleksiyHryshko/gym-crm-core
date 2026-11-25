package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainings", schema = "public")
public class TrainingEntity {

    @Id
    @SequenceGenerator(name = "trainingsSequence", sequenceName = "trainings_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainingsSequence")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity trainee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Column(name = "trainingname", nullable = false)
    private String trainingName;

    @Column(name = "trainingdate", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "trainingduration", nullable = false)
    private Integer trainingDuration;

    public TrainingEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TraineeEntity getTrainee() {
        return trainee;
    }

    public void setTrainee(TraineeEntity trainee) {
        this.trainee = trainee;
    }

    public TrainerEntity getTrainer() {
        return trainer;
    }

    public void setTrainer(TrainerEntity trainer) {
        this.trainer = trainer;
    }

    public TrainingTypeEntity getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingTypeEntity trainingType) {
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
}