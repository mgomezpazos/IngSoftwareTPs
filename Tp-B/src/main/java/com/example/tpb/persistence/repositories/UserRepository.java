package com.example.tpb.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tpb.persistence.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {}
