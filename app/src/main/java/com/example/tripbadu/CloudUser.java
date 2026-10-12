package com.example.tripbadu;

public class CloudUser {
    private String name;
    private String email;
    private String role;
    private String passwordHash;

    public CloudUser() {
        // Default constructor required for Firebase Realtime Database
    }

    public CloudUser(String name, String email, String role, String passwordHash) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
