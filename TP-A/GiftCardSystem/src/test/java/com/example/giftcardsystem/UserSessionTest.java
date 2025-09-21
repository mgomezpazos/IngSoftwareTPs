package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static com.example.giftcardsystem.GiftCardSystemFacade.GiftCardNotFoundErrorDescription;
import static com.example.giftcardsystem.UserSession.SessionExpired;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserSessionTest {
    private UserSession userSession;
    private TestClock testClock;
    private Map<String, GiftCard> giftCards;
    private GiftCard testCard;

    private static final String USER_EMILIO = "Emilio";
    private static final String TOKEN_EMILIO = "tokenEmilio";
    private static final String CARD_NUMBER = "1234567891";
    private static final String INVALID_CARD = "9876543219";
    private static final int CARD_BALANCE = 500;
    private static final String MERCHANT_1 = "merchant1";
    private static final String CHARGE_DESCRIPTION = "Remera de Daniel Ricciardo";
    private static final int SESSION_TIMEOUT_MINUTES = 5;

    @BeforeEach
    public void setUp() {
        setupTestDependencies();
        setupGiftCards();
        userSession = new UserSession(USER_EMILIO, TOKEN_EMILIO, testClock);
    }

    @Test
    public void test01UserSessionCreation() {
        assertEquals(USER_EMILIO, userSession.getUser());
        assertEquals(TOKEN_EMILIO, userSession.getToken());
        assertTrue(userSession.isActive());
    }

    @Test
    public void test02SessionIsActiveWithinTimeout() {
        testClock.advanceMinutes(SESSION_TIMEOUT_MINUTES - 1);
        assertTrue(userSession.isActive());
    }

    @Test
    public void test03SessionExpiresAfterTimeout() {
        testClock.advanceMinutes(SESSION_TIMEOUT_MINUTES + 1);
        assertFalse(userSession.isActive());
    }

    @Test
    public void test04SessionExpiresExactlyAtTimeout() {
        testClock.advanceMinutes(SESSION_TIMEOUT_MINUTES);
        assertFalse(userSession.isActive());
    }

    @Test
    public void test05UpdateLastAccessExtendsSession() {
        testClock.advanceMinutes(3);
        assertTrue(userSession.isActive());

        userSession.updateLastAccess();

        testClock.advanceMinutes(3);
        assertTrue(userSession.isActive());

        testClock.advanceMinutes(3);
        assertFalse(userSession.isActive());
    }

    @Test
    public void test06ClaimGiftCardUpdatesAccess() {
        testClock.advanceMinutes(3);

        userSession.claimGiftCard(CARD_NUMBER, giftCards);

        assertTrue(testCard.isClaimed());
        assertTrue(testCard.isClaimedBy(USER_EMILIO));

        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void test07GetGiftCardBalanceUpdatesAccess() {
        testCard.claimBy(USER_EMILIO);
        testClock.advanceMinutes(3);

        int balance = userSession.getGiftCardBalance(CARD_NUMBER, giftCards);

        assertEquals(CARD_BALANCE, balance);
        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void test08GetGiftCardChargesUpdatesAccess() {
        testCard.claimBy(USER_EMILIO);
        testCard.chargeAmount(100, MERCHANT_1, CHARGE_DESCRIPTION);
        testClock.advanceMinutes(3);

        var charges = userSession.getGiftCardCharges(CARD_NUMBER, giftCards);

        assertEquals(1, charges.size());
        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void test09CannotAccessCardNotClaimedByUser() {
        assertThrowsWithMessage(() -> userSession.getGiftCardBalance(CARD_NUMBER, giftCards), GiftCardNotFoundErrorDescription);
    }

    @Test
    public void test10CannotAccessNonExistentCard() {
        assertThrowsWithMessage(() -> userSession.getGiftCardBalance(INVALID_CARD, giftCards), GiftCardNotFoundErrorDescription);
    }

    @Test
    public void test11SessionCreationWithInvalidUser() {
        assertThrowsWithMessage(() -> new UserSession(null, TOKEN_EMILIO, testClock), UserSession.InvalidUserErrorDescription);
        assertThrowsWithMessage(() -> new UserSession("", TOKEN_EMILIO, testClock), UserSession.InvalidUserErrorDescription);
        assertThrowsWithMessage(() -> new UserSession("   ", TOKEN_EMILIO, testClock), UserSession.InvalidUserErrorDescription);
    }

    @Test
    public void test12SessionCreationWithInvalidToken() {
        assertThrowsWithMessage(() -> new UserSession(USER_EMILIO, null, testClock), UserSession.InvalidTokenErrorDescription);
        assertThrowsWithMessage(() -> new UserSession(USER_EMILIO, "", testClock), UserSession.InvalidTokenErrorDescription);
        assertThrowsWithMessage(() -> new UserSession(USER_EMILIO, "   ", testClock), UserSession.InvalidTokenErrorDescription);
    }

    @Test
    public void test13SessionCreationWithNullClock() {
        assertThrowsWithMessage(() -> new UserSession(USER_EMILIO, TOKEN_EMILIO, null), UserSession.InvalidClockErrorDescription);
    }

    @Test
    public void test14MultipleUpdateLastAccessCalls() {
        testClock.advanceMinutes(2);
        userSession.updateLastAccess();

        testClock.advanceMinutes(2);
        userSession.updateLastAccess();

        testClock.advanceMinutes(2);
        userSession.updateLastAccess();

        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());

        testClock.advanceMinutes(2);
        assertFalse(userSession.isActive());
    }

    @Test
    public void test15OperationsFailAfterExpiration() {
        testCard.claimBy(USER_EMILIO);
        testClock.advanceMinutes(SESSION_TIMEOUT_MINUTES + 1);
        assertFalse(userSession.isActive());

        assertThrowsWithMessage(() -> userSession.claimGiftCard(CARD_NUMBER, giftCards), SessionExpired);
        assertThrowsWithMessage(() -> userSession.getGiftCardBalance(CARD_NUMBER, giftCards), SessionExpired);
        assertThrowsWithMessage(() -> userSession.getGiftCardCharges(CARD_NUMBER, giftCards), SessionExpired);
    }

    @Test
    public void test16UpdateLastAccessAfterSessionExpired() {
        testClock.advanceMinutes(SESSION_TIMEOUT_MINUTES + 1);
        assertFalse(userSession.isActive());

        userSession.updateLastAccess();
        assertFalse(userSession.isActive());
    }

    @Test
    public void test17ConcurrentSessionActivity() {
        testClock.advanceMinutes(2);
        userSession.claimGiftCard(CARD_NUMBER, giftCards);

        testClock.advanceMinutes(2);
        int balance = userSession.getGiftCardBalance(CARD_NUMBER, giftCards);

        testClock.advanceMinutes(2);
        var charges = userSession.getGiftCardCharges(CARD_NUMBER, giftCards);

        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    private void setupTestDependencies() {
        testClock = new TestClock();
    }

    private void setupGiftCards() {
        giftCards = new HashMap<>();
        testCard = GiftCard.numberedWithBalance(CARD_NUMBER, CARD_BALANCE);
        giftCards.put(CARD_NUMBER, testCard);
    }

    private void assertThrowsWithMessage(Runnable action, String expectedMessage) {
        RuntimeException exception = assertThrows(RuntimeException.class, action::run);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private static class TestClock extends Clock {
        private LocalDateTime currentTime = LocalDateTime.now();

        @Override
        public LocalDateTime now() {
            return currentTime;
        }

        public void advanceMinutes(int minutes) {
            currentTime = currentTime.plusMinutes(minutes);
        }

        public void setTime(LocalDateTime time) {
            currentTime = time;
        }
    }
}