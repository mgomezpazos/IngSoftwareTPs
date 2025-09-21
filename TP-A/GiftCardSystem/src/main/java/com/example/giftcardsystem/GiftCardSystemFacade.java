package com.example.giftcardsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GiftCardSystemFacade {
    public static final String InvalidUserAndOrPasswordErrorDescription = "Invalid user and/or password";
    public static final String InvalidTokenErrorDescription = "Invalid or expired token";
    public static final String InvalidMerchantKeyErrorDescription = "Invalid merchant key";
    public static final String GiftCardNotFoundErrorDescription = "Gift card not found or not claimed by user";
    public static final String TokenExpiredErrorDescription = "Token has expired";
    public static final String UserAlreadyExists = "User already exists";

    private final Map<String, String> validUsers;
    private final Map<String, GiftCard> giftCards;
    private final Map<String, String> validMerchantKeys;
    private final MerchantApi merchantApi;
    private final Clock clock;
    private final Map<String, UserSession> activeSessions;

    public GiftCardSystemFacade(Map<String, String> validUsers, Map<String, GiftCard> giftCards, Map<String, String> validMerchantKeys,
                                MerchantApi merchantApi, Clock clock) {
        this.validUsers = validUsers;
        this.giftCards = giftCards;
        this.validMerchantKeys = validMerchantKeys;
        this.merchantApi = merchantApi;
        this.clock = clock;
        this.activeSessions = new HashMap<>();
    }

    public String loginUser(String user, String password) {
        validateUser(user, password);

        String token = generateToken();
        UserSession session = new UserSession(user, token, clock);
        activeSessions.put(token, session);

        return token;
    }

    public void claimGiftCard(String token, String cardNumber) {
        UserSession session = getValidatedSessionAndCard(token, cardNumber);
        session.claimGiftCard(cardNumber, giftCards);
    }

    public int getGiftCardBalance(String token, String cardNumber) {
        UserSession session = getValidatedSessionAndCard(token, cardNumber);
        return session.getGiftCardBalance(cardNumber, giftCards);
    }

    public List<Charge> getGiftCardCharges(String token, String cardNumber) {
        UserSession session = getValidatedSessionAndCard(token, cardNumber);
        return session.getGiftCardCharges(cardNumber, giftCards);
    }

    public void merchantChargeToCard(String merchantKey, String cardNumber, int amount, String description) {
        validateMerchant(merchantKey);
        validateGiftCardExists(cardNumber);

        GiftCard card = giftCards.get(cardNumber);
        card.chargeAmount(amount, merchantKey, description);

        merchantApi.processCharge(cardNumber, amount, merchantKey, description);
    }

    public List<String> getUserClaimedCards(String token) {
        UserSession session = getActiveSession(token);
        String user = session.getUser();

        return giftCards.values().stream().filter(card -> card.isClaimedBy(user)).map(GiftCard::getCardNumber).toList();
    }

    public void registerUser(String user, String password) {
        if (validUsers.containsKey(user)) {
            throw new RuntimeException(UserAlreadyExists);
        }
        validUsers.put(user, password);
    }

    private UserSession getValidatedSessionAndCard(String token, String cardNumber) {
        UserSession session = getActiveSession(token);
        validateGiftCardExists(cardNumber);
        return session;
    }

    private UserSession getActiveSession(String token) {
        UserSession session = activeSessions.get(token);

        if (session == null) {
            throw new RuntimeException(InvalidTokenErrorDescription);
        }

        if (!session.isActive()) {
            activeSessions.remove(token);
            throw new RuntimeException(TokenExpiredErrorDescription);
        }

        return session;
    }

    private void validateUser(String user, String password) {
        if (!password.equals(validUsers.get(user))) {
            throw new RuntimeException(InvalidUserAndOrPasswordErrorDescription);
        }
    }

    private void validateMerchant(String merchantKey) {
        if (!validMerchantKeys.containsKey(merchantKey)) {
            throw new RuntimeException(InvalidMerchantKeyErrorDescription);
        }
    }

    private void validateGiftCardExists(String cardNumber) {
        if (!giftCards.containsKey(cardNumber)) {
            throw new RuntimeException(GiftCardNotFoundErrorDescription);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}