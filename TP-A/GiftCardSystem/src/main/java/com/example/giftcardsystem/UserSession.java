package com.example.giftcardsystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserSession {
    public static final String InvallidGiftCard = "Gift card not found or not claimed by user";
    public static final String SessionExpired = "Session expired";
    public static final String InvalidUserErrorDescription = "User cannot be null or empty";
    public static final String InvalidTokenErrorDescription = "Token cannot be null or empty";
    public static final String InvalidClockErrorDescription = "Clock cannot be null";

    private static final int SESSION_TIMEOUT_MINUTES = 5;

    private final String user;
    private final String token;
    private final Clock clock;
    private LocalDateTime lastAccess;

    public UserSession(String user, String token, Clock clock) {
        validateStringNotNullOrEmpty(user, InvalidUserErrorDescription);
        validateStringNotNullOrEmpty(token, InvalidTokenErrorDescription);
        validateNotNull(clock, InvalidClockErrorDescription);

        this.user = user;
        this.token = token;
        this.clock = clock;
        this.lastAccess = clock.now();
    }

    public void updateLastAccess() {
        if (isActive()) {
            this.lastAccess = clock.now();
        }
    }

    public boolean isActive() {
        return lastAccess.plusMinutes(SESSION_TIMEOUT_MINUTES).isAfter(clock.now());
    }

    public void claimGiftCard(String cardNumber, Map<String, GiftCard> availableCards) {
        validateActiveSession();
        updateLastAccess();

        GiftCard card = availableCards.get(cardNumber);
        if (card != null) {
            card.claimBy(user);
        }
    }

    public int getGiftCardBalance(String cardNumber, Map<String, GiftCard> availableCards) {
        validateActiveSession();
        updateLastAccess();

        GiftCard card = getValidCardForUser(cardNumber, availableCards);
        return card.getBalance();
    }

    public List<Charge> getGiftCardCharges(String cardNumber, Map<String, GiftCard> availableCards) {
        validateActiveSession();
        updateLastAccess();

        GiftCard card = getValidCardForUser(cardNumber, availableCards);
        return card.getCharges();
    }

    public String getUser() {return user;}
    public String getToken() {return token;}

    private GiftCard getValidCardForUser(String cardNumber, Map<String, GiftCard> availableCards) {
        GiftCard card = availableCards.get(cardNumber);
        if (card != null && card.isClaimedBy(user)) {
            return card;
        }
        throw new RuntimeException(InvallidGiftCard);
    }

    private void validateActiveSession() {
        if (!isActive()) {
            throw new RuntimeException(SessionExpired);
        }
    }

    private static void validateStringNotNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(errorMessage);
        }
    }

    private static void validateNotNull(Object value, String errorMessage) {
        if (value == null) {
            throw new RuntimeException(errorMessage);
        }
    }
}