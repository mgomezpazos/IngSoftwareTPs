package com.example.tpb.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter @Setter
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
}