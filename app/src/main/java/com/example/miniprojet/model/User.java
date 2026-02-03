package com.example.miniprojet.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String id;
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private String phone;
    private String university;
    private String profileImageUrl;
    private Date createdAt;
    private Date updatedAt;
    private int advertisementCount = 0;

    // Constructeurs
    public User() {}

    public User(String lastName, String firstName, String email, String password, String phone, String university) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.university = university;
        this.profileImageUrl = "";
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.advertisementCount = 0;
    }

    public User(String lastName, String firstName, String email, String phone, String university) {
        this(lastName, firstName, email, "", phone, university);
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getAdvertisementCount() { return advertisementCount; }
    public void setAdvertisementCount(int advertisementCount) {
        this.advertisementCount = advertisementCount;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}