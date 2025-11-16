package com.example.tpb.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tpb.persistence.entities.SessionEntity;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {}
