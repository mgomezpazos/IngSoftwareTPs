package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserSessionTest {

    private UserSession userSession;
    private TestClock testClock;
    private Map<String, GiftCard> giftCards;
    private GiftCard testCard;

    @BeforeEach
    public void setUp() {
        testClock = new TestClock();
        userSession = new UserSession("testUser", "token123", testClock);

        // Setup test gift cards
        giftCards = new HashMap<>();
        testCard = GiftCard.numberedWithBalance("1111222233334444", 500);
        giftCards.put("1111222233334444", testCard);
    }

    @Test
    public void testUserSessionCreation() {
        assertEquals("testUser", userSession.getUser());
        assertEquals("token123", userSession.getToken());
        assertTrue(userSession.isActive());
    }

    @Test
    public void testSessionIsActiveWithinFiveMinutes() {
        // Advance time by 4 minutes
        testClock.advanceMinutes(4);

        assertTrue(userSession.isActive());
    }

    @Test
    public void testSessionExpiresAfterFiveMinutes() {
        // Advance time by 6 minutes
        testClock.advanceMinutes(6);

        assertFalse(userSession.isActive());
    }

    @Test
    public void testSessionExpiresExactlyAtFiveMinutes() {
        // Advance time by exactly 5 minutes
        testClock.advanceMinutes(5);

        assertFalse(userSession.isActive());
    }

    @Test
    public void testUpdateLastAccessExtendsSession() {
        // Advance time by 3 minutes
        testClock.advanceMinutes(3);
        assertTrue(userSession.isActive());

        // Update access time
        userSession.updateLastAccess();

        // Advance another 3 minutes (6 total, but 3 since last access)
        testClock.advanceMinutes(3);
        assertTrue(userSession.isActive());

        // Now advance 3 more minutes (should expire)
        testClock.advanceMinutes(3);
        assertFalse(userSession.isActive());
    }

    @Test
    public void testClaimGiftCardUpdatesAccess() {
        // Advance time by 3 minutes
        testClock.advanceMinutes(3);

        // Claim gift card - this should update last access
        userSession.claimGiftCard("1111222233334444", giftCards);

        assertTrue(testCard.isClaimed());
        assertTrue(testCard.isClaimedBy("testUser"));

        // Advance another 4 minutes (7 total, but 4 since claim)
        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void testGetGiftCardBalanceUpdatesAccess() {
        // First claim the card
        testCard.claimBy("testUser");

        // Advance time by 3 minutes
        testClock.advanceMinutes(3);

        // Get balance - this should update last access
        int balance = userSession.getGiftCardBalance("1111222233334444", giftCards);
        assertEquals(500, balance);

        // Advance another 4 minutes (7 total, but 4 since balance check)
        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void testGetGiftCardChargesUpdatesAccess() {
        // First claim the card and add a charge
        testCard.claimBy("testUser");
        testCard.chargeAmount(100, "merchant1", "Test purchase");

        // Advance time by 3 minutes
        testClock.advanceMinutes(3);

        // Get charges - this should update last access
        var charges = userSession.getGiftCardCharges("1111222233334444", giftCards);
        assertEquals(1, charges.size());

        // Advance another 4 minutes (7 total, but 4 since charges check)
        testClock.advanceMinutes(4);
        assertTrue(userSession.isActive());
    }

    @Test
    public void testCannotAccessCardNotClaimedByUser() {
        // Don't claim the card, try to access balance
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userSession.getGiftCardBalance("1111222233334444", giftCards);
        });

        assertEquals("Gift card not found or not claimed by user", exception.getMessage());
    }

    @Test
    public void testCannotAccessNonExistentCard() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userSession.getGiftCardBalance("9999888877776666", giftCards);
        });

        assertEquals("Gift card not found or not claimed by user", exception.getMessage());
    }

    // Helper class for testing time-dependent functionality
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