package com.example.tpb.persistence.repositories;
import org.springframework.data.repository.CrudRepository;
import com.example.tpb.persistence.entities.SessionEntity;

import java.util.UUID;

public interface SessionRepository extends CrudRepository<SessionEntity, UUID> {}