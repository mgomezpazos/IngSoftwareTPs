package com.example.tpb.service;

import com.example.tpb.model.GiftCardFacade;
import com.example.tpb.model.UserSession;
import com.example.tpb.persistence.entities.SessionEntity;
import com.example.tpb.persistence.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired private SessionRepository repository;

    @Transactional( readOnly = true )
    public UserSession findById( UUID token ) {
        SessionEntity entity = repository.findById( token )
                .orElseThrow( () -> new RuntimeException( GiftCardFacade.InvalidToken ) );
        return new UserSession( entity.getUsername(), entity.getStamp() );
    }

    public void save( UUID token, String username, LocalDateTime stamp ) {
        repository.save( new SessionEntity( token, username, stamp ) );
    }

    public void delete( UUID token ) {
        repository.deleteById( token );
    }
}