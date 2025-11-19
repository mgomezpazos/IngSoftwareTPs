package com.example.tpb.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import com.example.tpb.persistence.entities.MerchantEntity;

public interface MerchantRepository extends CrudRepository<MerchantEntity, String> {}