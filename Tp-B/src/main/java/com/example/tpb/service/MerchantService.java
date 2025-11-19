package com.example.tpb.service;

import com.example.tpb.model.GiftCardFacade;
import com.example.tpb.persistence.entities.MerchantEntity;
import com.example.tpb.persistence.repositories.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    @Autowired private MerchantRepository repository;

    @Transactional( readOnly = true )
    public void assertExists( String merchantKey ) {
        if ( !repository.existsById( merchantKey ) ) {
            throw new RuntimeException( GiftCardFacade.InvalidMerchant );
        }
    }

    public MerchantEntity save( MerchantEntity merchant ) {
        return repository.save( merchant );
    }

    @Transactional( readOnly = true )
    public long count() {
        return repository.count();
    }

    public void delete( String code ) {
        repository.deleteById( code );
    }
}