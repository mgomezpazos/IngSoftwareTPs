package com.example.tpb.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tpb.persistence.entities.GiftCardEntity;

public interface GiftCardRepository extends JpaRepository<GiftCardEntity, String> {}

