package com.example.giftcardsystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserSession {
    private String user;
    private String token;
    private LocalDateTime lastAccess;
    private Clock clock;

    public UserSession(String user, String token, Clock clock) {
        this.user = user;
        this.token = token;
        this.clock = clock;
        this.lastAccess = clock.now();
    }

    public void updateLastAccess() {
        this.lastAccess = clock.now();
    }

    public boolean isActive() {
        return lastAccess.plusMinutes(5).isAfter(clock.now());
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public void claimGiftCard(String cardNumber, Map<String, GiftCard> availableCards) {
        updateLastAccess();
        GiftCard card = availableCards.get(cardNumber);
        if (card != null) {
            card.claimBy(user);
        }
    }

    public int getGiftCardBalance(String cardNumber, Map<String, GiftCard> availableCards) {
        updateLastAccess();
        GiftCard card = availableCards.get(cardNumber);
        if (card != null && card.isClaimedBy(user)) {
            return card.getBalance();
        }
        throw new RuntimeException("Gift card not found or not claimed by user");
    }

    public List<Charge> getGiftCardCharges(String cardNumber, Map<String, GiftCard> availableCards) {
        updateLastAccess();
        GiftCard card = availableCards.get(cardNumber);
        if (card != null && card.isClaimedBy(user)) {
            return card.getCharges();
        }
        throw new RuntimeException("Gift card not found or not claimed by user");
    }
}