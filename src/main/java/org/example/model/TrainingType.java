package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "training_types")
public class TrainingType {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String trainingTypeName;

    public TrainingType() {
    }

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

    public Long getId() { return id; }

}