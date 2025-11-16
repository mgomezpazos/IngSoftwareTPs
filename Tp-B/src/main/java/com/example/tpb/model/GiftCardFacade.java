package com.example.tpb.model;

import com.example.tpb.persistence.entities.*;
import com.example.tpb.persistence.repositories.*;
import com.example.tpb.persistence.entities.GiftCardEntity;
import com.example.tpb.persistence.entities.SessionEntity;
import com.example.tpb.persistence.entities.UserEntity;
import com.example.tpb.persistence.entities.MerchantEntity;
import com.example.tpb.persistence.repositories.GiftCardRepository;
import com.example.tpb.persistence.repositories.MerchantRepository;
import com.example.tpb.persistence.repositories.SessionRepository;
import com.example.tpb.persistence.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GiftCardFacade {
    public static final String InvalidUser = "InvalidUser";
    public static final String InvalidMerchant = "InvalidMerchant";
    public static final String InvalidToken = "InvalidToken";

    private final GiftCardRepository cardRepo;
    private final UserRepository userRepo;
    private final MerchantRepository merchantRepo;
    private final SessionRepository sessionRepo;
    private final Clock clock;

    public GiftCardFacade(GiftCardRepository cardRepo,
                         UserRepository userRepo,
                         MerchantRepository merchantRepo,
                         SessionRepository sessionRepo,
                         Clock clock) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.merchantRepo = merchantRepo;
        this.sessionRepo = sessionRepo;
        this.clock = clock;
    }

    public UUID login(String userKey, String pass) {
        UserEntity user = userRepo.findById(userKey).orElseThrow(() -> new RuntimeException(InvalidUser));
        if (!user.getPassword().equals(pass)) throw new RuntimeException(InvalidUser);

        UUID token = UUID.randomUUID();
        sessionRepo.save(new SessionEntity(token, userKey, clock.now()));
        return token;
    }

    public void redeem(UUID token, String cardId) {
        String user = findUser(token);
        GiftCardEntity card = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException("InvalidCard"));

        if (card.getOwner() != null) {
            if (card.getOwner().equals(user)) {
                throw new RuntimeException("CardAlreadyRedeemed");
            } else {
                throw new RuntimeException("InvalidCard");
            }
        }

        card.setOwner(user);
        cardRepo.save(card);
    }

    public int balance(UUID token, String cardId) {
        GiftCardEntity card = ownedCard(token, cardId);
        return card.getBalance();
    }

    public void charge(String merchantKey, String cardId, int amount, String description) {
        if (!merchantRepo.existsById(merchantKey)) throw new RuntimeException(InvalidMerchant);

        GiftCardEntity card = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException(GiftCard.CargoImposible));
        // Reuse rules from domain: must be owned AND have enough balance
        if (card.getOwner() == null || (card.getBalance() - amount < 0)) throw new RuntimeException(GiftCard.CargoImposible);

        card.setBalance(card.getBalance() - amount);
        List<String> charges = card.getCharges();
        charges.add(description);
        card.setCharges(charges);
        cardRepo.save(card);
    }

    public List<String> details(UUID token, String cardId) {
        GiftCardEntity card = ownedCard(token, cardId);
        return card.getCharges();
    }

    private GiftCardEntity ownedCard(UUID token, String cardId) {
        GiftCardEntity card = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException(GiftCard.InvalidCard));
        String owner = findUser(token);
        if (card.getOwner() == null || !card.getOwner().equals(owner)) throw new RuntimeException(InvalidToken);
        return card;
    }

    private String findUser(UUID token) {
        SessionEntity session = sessionRepo.findById(token).orElseThrow(() -> new RuntimeException(InvalidToken));
        if (clock.now().isAfter(session.getStamp().plusMinutes(15))) throw new RuntimeException(InvalidToken);
        return session.getUsername();
    }
}

