package com.tss.FoodApp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tss.FoodApp.repository.Identifiable;

public abstract class User implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String password;
    private String name;
    private Role role;
    private boolean isActive;
    private String createdAt;

    public User(String id, String username, String password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s", id, name, username, role, isActive ? "Active" : "Inactive");
    }
}
