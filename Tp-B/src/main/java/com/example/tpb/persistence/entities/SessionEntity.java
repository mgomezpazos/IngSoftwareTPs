package com.example.tpb.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    private UUID token;
    private String username;
    private LocalDateTime stamp;

    public SessionEntity() {}

    public SessionEntity(UUID token, String username, LocalDateTime stamp) {
        this.token = token;
        this.username = username;
        this.stamp = stamp;
    }

    public UUID getToken() { return token; }
    public void setToken(UUID token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getStamp() { return stamp; }
    public void setStamp(LocalDateTime stamp) { this.stamp = stamp; }
}

