package org.example.model;

public class Trainer extends User {
    private String specialization;
    private Long userId;

    public Trainer() {
        super();
    }

    public Trainer(String firstName, String lastName, boolean isActive, String specialization) {
        super(firstName, lastName, isActive);
        this.specialization = specialization;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}