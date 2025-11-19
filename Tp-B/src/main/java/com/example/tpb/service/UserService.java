package com.example.tpb.service;

import com.example.tpb.model.GiftCardFacade;
import com.example.tpb.persistence.entities.UserEntity;
import com.example.tpb.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired private UserRepository repository;

    @Transactional( readOnly = true )
    public UserEntity findById( String username ) {
        return repository.findById( username )
                .orElseThrow( () -> new RuntimeException( GiftCardFacade.InvalidUser ) );
    }

    public UserEntity save( UserEntity user ) {
        return repository.save( user );
    }

    @Transactional( readOnly = true )
    public long count() {
        return repository.count();
    }

    public void delete( String username ) {
        repository.deleteById( username );
    }
}