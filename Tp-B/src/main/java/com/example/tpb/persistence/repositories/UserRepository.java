package com.example.tpb.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import com.example.tpb.persistence.entities.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, String> {}