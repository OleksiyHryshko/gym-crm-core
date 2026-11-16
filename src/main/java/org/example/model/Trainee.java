package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Trainee {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private boolean isActive;
    private LocalDate dateOfBirth;
    private String address;
    private List<Trainer> trainers = new ArrayList<>();

    public Trainee() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Trainer> getTrainers() { return trainers; }
    public void setTrainers(List<Trainer> trainers) { this.trainers = trainers; }
}