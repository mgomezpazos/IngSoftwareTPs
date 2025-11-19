package com.example.tpb.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import com.example.tpb.persistence.entities.GiftCardEntity;

public interface GiftCardRepository extends CrudRepository<GiftCardEntity, String> {}