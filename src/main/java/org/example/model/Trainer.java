package org.example.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name=  "trainers")
public class Trainer  {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String specialization;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees;

    public Trainer() {
        super();
    }

    public Trainer(String firstName, String lastName, boolean isActive, String specialization) {
        this.user = new User(firstName, lastName, isActive);
        this.specialization = specialization;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getUserId() { return id; }

    public void setUserId(Long id) {
        this.id = id;
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

    public List<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<Trainee> trainees) {
        this.trainees = trainees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private User requireUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
}