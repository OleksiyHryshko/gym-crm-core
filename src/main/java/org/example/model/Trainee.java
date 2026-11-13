package org.example.model;

import jakarta.persistence.*;
import java.util.List;

import java.time.LocalDate;

@Entity
@Table(name=  "trainees")
public class Trainee  {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDate dateOfBirth;
    private String address;
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE)
    private List<Training> trainings;

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private List<Trainer> trainers;

    public Trainee() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return id; }
    public void setUserId(Long id) { this.id = id; }

    public Trainee(String firstName, String lastName, boolean isActive, LocalDate dateOfBirth, String address) {
        this.user = new User(firstName, lastName, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return user == null ? null : user.getFirstName();
    }

    public void setFirstName(String firstName) {
        requireUser().setFirstName(firstName);
    }

    public String getLastName() {
        return user == null ? null : user.getLastName();
    }

    public void setLastName(String lastName) {
        requireUser().setLastName(lastName);
    }

    public String getUserName() {
        return user == null ? null : user.getUserName();
    }

    public void setUserName(String userName) {
        requireUser().setUserName(userName);
    }

    public String getPassword() {
        return user == null ? null : user.getPassword();
    }

    public void setPassword(String password) {
        requireUser().setPassword(password);
    }

    public boolean isActive() {
        return user != null && user.isActive();
    }

    public void setActive(boolean active) {
        requireUser().setActive(active);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }

    private User requireUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
}