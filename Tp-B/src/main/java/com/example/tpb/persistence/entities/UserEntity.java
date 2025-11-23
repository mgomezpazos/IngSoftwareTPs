package com.example.tpb.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class UserEntity {
    @Id
    private String username;
    private String password;

    public UserEntity() {}
    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }
}