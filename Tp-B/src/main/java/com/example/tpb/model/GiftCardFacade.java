package com.example.tpb.model;

import com.example.tpb.persistence.entities.UserEntity;
import com.example.tpb.service.GiftCardService;
import com.example.tpb.service.MerchantService;
import com.example.tpb.service.SessionService;
import com.example.tpb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GiftCardFacade {
    public static final String InvalidUser = "InvalidUser";
    public static final String InvalidMerchant = "InvalidMerchant";
    public static final String InvalidToken = "InvalidToken";

    @Autowired private GiftCardService giftCardService;
    @Autowired private UserService userService;
    @Autowired private MerchantService merchantService;
    @Autowired private SessionService sessionService;
    @Autowired private Clock clock;

    public UUID login(String userKey, String pass) {
        UserEntity user = userService.findById(userKey);
        if (!user.getPassword().equals(pass)) throw new RuntimeException(InvalidUser);
        UUID token = UUID.randomUUID();
        sessionService.save(token, userKey, clock.now());
        return token;
    }

    public void redeem(UUID token, String cardId) {
        String user = findUserFromToken(token);
        GiftCard card = giftCardService.findById(cardId);
        card.redeem(user);
        giftCardService.save(card);
    }

    public int balance(UUID token, String cardId) {
        String owner = findUserFromToken(token);
        GiftCard card = findOwnedCard(cardId, owner);
        return card.balance();
    }

    public void charge(String merchantKey, String cardId, int amount, String description) {
        merchantService.assertExists(merchantKey);
        GiftCard card = giftCardService.findById(cardId);
        card.charge(amount, description);
        giftCardService.save(card);
    }

    public List<String> details(UUID token, String cardId) {
        String owner = findUserFromToken(token);
        GiftCard card = findOwnedCard(cardId, owner);
        return card.charges();
    }

    private GiftCard findOwnedCard(String cardId, String owner) {
        GiftCard card = giftCardService.findById(cardId);
        card.assertIsOwnedBy(owner);
        return card;
    }

    private String findUserFromToken(UUID token) {
        UserSession session = sessionService.findById(token);
        return session.userAliveAt(clock.now());
    }
}