package com.example.tpb.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tpb.persistence.entities.MerchantEntity;

public interface MerchantRepository extends JpaRepository<MerchantEntity, String> {}

