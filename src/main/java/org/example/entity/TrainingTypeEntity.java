package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "training_types", schema = "public")
public class TrainingTypeEntity {

    @Id
    @SequenceGenerator(name = "trainingTypesSequence", sequenceName = "training_types_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainingTypesSequence")
    private Long id;

    @Column(name = "trainingtypename", nullable = false, unique = true)
    private String trainingTypeName;

    public TrainingTypeEntity() {
    }

    public TrainingTypeEntity(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}