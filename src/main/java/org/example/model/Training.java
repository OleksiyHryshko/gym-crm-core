package org.example.model;

import java.time.LocalDate;

public class Training {
    private Long id;
    private String traineeUserName;
    private String trainerUserName;
    private String traineeName;
    private String trainerName;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Integer trainingDuration;

    public Training() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTraineeUserName() { return traineeUserName; }
    public void setTraineeUserName(String traineeUserName) { this.traineeUserName = traineeUserName; }

    public String getTrainerUserName() { return trainerUserName; }
    public void setTrainerUserName(String trainerUserName) { this.trainerUserName = trainerUserName; }

    public String getTraineeName() { return traineeName; }
    public void setTraineeName(String traineeName) { this.traineeName = traineeName; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public Integer getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(Integer trainingDuration) { this.trainingDuration = trainingDuration; }
}