package com.example.tpb.service;

import com.example.tpb.persistence.repositories.GiftCardRepository;
import com.example.tpb.persistence.repositories.MerchantRepository;
import com.example.tpb.persistence.repositories.SessionRepository;
import com.example.tpb.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.tpb.model.Clock;
import com.example.tpb.model.GiftCardFacade; // adaptaremos/crearemos esta versión Spring
import com.example.tpb.persistence.entities.*;
import com.example.tpb.persistence.repositories.*;

import java.util.UUID;

@Service
public class GiftCardAppService {

    private final GiftCardFacade facade;

    @Autowired
    public GiftCardAppService(GiftCardRepository cardRepo,
                              UserRepository userRepo,
                              MerchantRepository merchantRepo,
                              SessionRepository sessionRepo) {
        // Creamos el facade que funciona con repos (ver clase en package persistence)
        this.facade = new GiftCardFacade(cardRepo, userRepo, merchantRepo, sessionRepo, new Clock());
    }

    public UUID login(String user, String pass) {
        return facade.login(user, pass);
    }

    public void redeem(UUID token, String cardId) {
        facade.redeem(token, cardId);
    }

    public int balance(UUID token, String cardId) {
        return facade.balance(token, cardId);
    }

    public void charge(String merchant, String cardId, int amount, String description) {
        facade.charge(merchant, cardId, amount, description);
    }

    public java.util.List<String> details(UUID token, String cardId) {
        return facade.details(token, cardId);
    }

    public UUID extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException(GiftCardFacade.InvalidToken);
        }
        String tokenStr = header.substring("Bearer ".length()).trim();
        try {
            return UUID.fromString(tokenStr);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(GiftCardFacade.InvalidToken);
        }
    }
}

