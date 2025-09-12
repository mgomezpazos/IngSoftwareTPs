package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GiftCardSystemFacadeTest {

    private GiftCardSystemFacade facade;
    private Map<String, String> validUsers;
    private Map<String, GiftCard> giftCards;
    private Map<String, String> validMerchantKeys;
    private TestMerchantApi testMerchantApi;
    private TestClock testClock;

    @BeforeEach
    public void setUp() {
        // Setup valid users
        validUsers = new HashMap<>();
        validUsers.put("user1", "password1");
        validUsers.put("user2", "password2");

        // Setup gift cards
        giftCards = new HashMap<>();
        GiftCard card1 = GiftCard.numberedWithBalance("1111222233334444", 1000);
        GiftCard card2 = GiftCard.numberedWithBalance("5555666677778888", 500);
        giftCards.put("1111222233334444", card1);
        giftCards.put("5555666677778888", card2);

        // Setup valid merchants
        validMerchantKeys = new HashMap<>();
        validMerchantKeys.put("merchant1", "Coffee Shop");
        validMerchantKeys.put("merchant2", "Book Store");

        // Setup test dependencies
        testMerchantApi = new TestMerchantApi();
        testClock = new TestClock();

        facade = new GiftCardSystemFacade(validUsers, giftCards, validMerchantKeys, testMerchantApi, testClock);
    }

    @Test
    public void testSuccessfulLogin() {
        String token = facade.loginUser("user1", "password1");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testInvalidUserLogin() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.loginUser("invalidUser", "password1");
        });

        assertEquals(GiftCardSystemFacade.InvalidUserAndOrPasswordErrorDescription, exception.getMessage());
    }

    @Test
    public void testInvalidPasswordLogin() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.loginUser("user1", "wrongPassword");
        });

        assertEquals(GiftCardSystemFacade.InvalidUserAndOrPasswordErrorDescription, exception.getMessage());
    }

    @Test
    public void testClaimGiftCard() {
        String token = facade.loginUser("user1", "password1");

        facade.claimGiftCard(token, "1111222233334444");

        GiftCard card = giftCards.get("1111222233334444");
        assertTrue(card.isClaimed());
        assertTrue(card.isClaimedBy("user1"));
    }

    @Test
    public void testClaimWithInvalidToken() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.claimGiftCard("invalidToken", "1111222233334444");
        });

        assertEquals(GiftCardSystemFacade.InvalidTokenErrorDescription, exception.getMessage());
    }

    @Test
    public void testClaimNonExistentCard() {
        String token = facade.loginUser("user1", "password1");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.claimGiftCard(token, "9999888877776666");
        });

        assertEquals(GiftCardSystemFacade.GiftCardNotFoundErrorDescription, exception.getMessage());
    }

    @Test
    public void testGetGiftCardBalance() {
        String token = facade.loginUser("user1", "password1");
        facade.claimGiftCard(token, "1111222233334444");

        int balance = facade.getGiftCardBalance(token, "1111222233334444");

        assertEquals(1000, balance);
    }

    @Test
    public void testGetBalanceWithoutClaiming() {
        String token = facade.loginUser("user1", "password1");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.getGiftCardBalance(token, "1111222233334444");
        });

        assertTrue(exception.getMessage().contains("Gift card not found or not claimed by user"));
    }

    @Test
    public void testGetGiftCardCharges() {
        String token = facade.loginUser("user1", "password1");
        facade.claimGiftCard(token, "1111222233334444");

        // Initially no charges
        List<Charge> charges = facade.getGiftCardCharges(token, "1111222233334444");
        assertTrue(charges.isEmpty());

        // Add a charge and check again
        facade.merchantChargeToCard("merchant1", "1111222233334444", 100, "Coffee purchase");
        charges = facade.getGiftCardCharges(token, "1111222233334444");

        assertEquals(1, charges.size());
        assertEquals(100, charges.get(0).getAmount());
        assertEquals("merchant1", charges.get(0).getMerchantKey());
    }

    @Test
    public void testMerchantChargeToCard() {
        String token = facade.loginUser("user1", "password1");
        facade.claimGiftCard(token, "1111222233334444");

        facade.merchantChargeToCard("merchant1", "1111222233334444", 200, "Book purchase");

        GiftCard card = giftCards.get("1111222233334444");
        assertEquals(800, card.getBalance());
        assertEquals(1, card.getCharges().size());

        // Verify merchant API was called
        assertTrue(testMerchantApi.wasCalled());
        assertEquals("1111222233334444", testMerchantApi.getLastCardNumber());
        assertEquals(200, testMerchantApi.getLastAmount());
    }

    @Test
    public void testMerchantChargeWithInvalidMerchant() {
        String token = facade.loginUser("user1", "password1");
        facade.claimGiftCard(token, "1111222233334444");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.merchantChargeToCard("invalidMerchant", "1111222233334444", 100, "Purchase");
        });

        assertEquals(GiftCardSystemFacade.InvalidMerchantKeyErrorDescription, exception.getMessage());
    }

    @Test
    public void testGetUserClaimedCards() {
        String token = facade.loginUser("user1", "password1");

        // Initially no claimed cards
        List<String> claimedCards = facade.getUserClaimedCards(token);
        assertTrue(claimedCards.isEmpty());

        // Claim one card
        facade.claimGiftCard(token, "1111222233334444");
        claimedCards = facade.getUserClaimedCards(token);

        assertEquals(1, claimedCards.size());
        assertTrue(claimedCards.contains("1111222233334444"));

        // Claim another card
        facade.claimGiftCard(token, "5555666677778888");
        claimedCards = facade.getUserClaimedCards(token);

        assertEquals(2, claimedCards.size());
        assertTrue(claimedCards.contains("1111222233334444"));
        assertTrue(claimedCards.contains("5555666677778888"));
    }

    @Test
    public void testTokenExpiration() {
        String token = facade.loginUser("user1", "password1");

        // Token should work initially
        facade.claimGiftCard(token, "1111222233334444");

        // Advance time by 6 minutes
        testClock.advanceMinutes(6);

        // Token should be expired now
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.getGiftCardBalance(token, "1111222233334444");
        });

        assertEquals(GiftCardSystemFacade.TokenExpiredErrorDescription, exception.getMessage());
    }

    @Test
    public void testMultipleUsersCannotAccessEachOthersCards() {
        String token1 = facade.loginUser("user1", "password1");
        String token2 = facade.loginUser("user2", "password2");

        facade.claimGiftCard(token1, "1111222233334444");

        // user2 should not be able to access user1's card
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facade.getGiftCardBalance(token2, "1111222233334444");
        });

        assertTrue(exception.getMessage().contains("Gift card not found or not claimed by user"));
    }

    // Helper classes for testing
    private static class TestMerchantApi extends MerchantApi {
        private boolean called = false;
        private String lastCardNumber;
        private int lastAmount;
        private String lastMerchantKey;
        private String lastDescription;

        @Override
        public String processCharge(String cardNumber, int amount, String merchantKey, String description) {
            called = true;
            lastCardNumber = cardNumber;
            lastAmount = amount;
            lastMerchantKey = merchantKey;
            lastDescription = description;
            return "TEST-TRANSACTION-" + System.currentTimeMillis();
        }

        public boolean wasCalled() { return called; }
        public String getLastCardNumber() { return lastCardNumber; }
        public int getLastAmount() { return lastAmount; }
        public String getLastMerchantKey() { return lastMerchantKey; }
        public String getLastDescription() { return lastDescription; }
    }

    private static class TestClock extends Clock {
        private java.time.LocalDateTime currentTime = java.time.LocalDateTime.now();

        @Override
        public java.time.LocalDateTime now() {
            return currentTime;
        }

        public void advanceMinutes(int minutes) {
            currentTime = currentTime.plusMinutes(minutes);
        }
    }
}