package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static com.example.giftcardsystem.GiftCard.GiftCardAlreadyClaimedErrorDescription;
import static com.example.giftcardsystem.GiftCardSystemFacade.GiftCardNotFoundErrorDescription;
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

    private static final String USER_MANUELITA = "Manuelita";
    private static final String PASSWORD_MANUELITA = "marcoreus11";
    private static final String USER_ZOE = "Zoe";
    private static final String PASSWORD_ZOE = "RayoMcqueen95";
    private static final String INVALID_USER = "Emilio";
    private static final String INVALID_PASSWORD = "motos1234";

    private static final String CARD_1 = "2204303355";
    private static final String CARD_2 = "1234567891";
    private static final String INVALID_CARD = "9876543219";
    private static final int CARD_1_BALANCE = 1000;
    private static final int CARD_2_BALANCE = 500;

    private static final String MERCHANT_1 = "merchant1";
    private static final String MERCHANT_2 = "merchant2";
    private static final String INVALID_MERCHANT = "MerchantInvalido";
    private static final String INVALID_TOKEN = "tokenInvalido";

    @BeforeEach
    public void setUp() {
        setupValidUsers();
        setupGiftCards();
        setupValidMerchants();
        setupTestDependencies();

        facade = new GiftCardSystemFacade(validUsers, giftCards, validMerchantKeys, testMerchantApi, testClock);
    }

    @Test
    public void test01SuccessfulLogin() {
        String token = facade.loginUser(USER_MANUELITA, PASSWORD_MANUELITA);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void test02InvalidLogin() {
        assertThrowsWithMessage(() -> facade.loginUser(INVALID_USER, INVALID_PASSWORD), GiftCardSystemFacade.InvalidUserAndOrPasswordErrorDescription);
        assertThrowsWithMessage(() -> facade.loginUser(USER_ZOE, "zoe5"), GiftCardSystemFacade.InvalidUserAndOrPasswordErrorDescription);
    }

    @Test
    public void test03ClaimGiftCard() {
        String token = loginUser(USER_ZOE);

        facade.claimGiftCard(token, CARD_1);

        GiftCard card = giftCards.get(CARD_1);
        assertTrue(card.isClaimed());
        assertTrue(card.isClaimedBy(USER_ZOE));
    }

    @Test
    public void test04ClaimWithInvalidToken() {
        assertThrowsWithMessage(() -> facade.claimGiftCard(INVALID_TOKEN, CARD_1), GiftCardSystemFacade.InvalidTokenErrorDescription);
    }

    @Test
    public void test05ClaimNonExistentCard() {
        String token = loginUser(USER_MANUELITA);
        assertThrowsWithMessage(() -> facade.claimGiftCard(token, INVALID_CARD), GiftCardNotFoundErrorDescription);
    }

    @Test
    public void test06MultipleUsersCannotClaimEachOthersCards() {
        String token1 = loginUser(USER_MANUELITA);
        String token2 = loginUser(USER_ZOE);

        facade.claimGiftCard(token1, CARD_1);

        assertThrowsWithMessage(() -> facade.claimGiftCard(token2, CARD_1), GiftCardAlreadyClaimedErrorDescription);
    }

    @Test
    public void test07UserCantClaimSameGiftCardTwice() {
        String token = loginUser(USER_MANUELITA);
        facade.claimGiftCard(token, CARD_1);
        assertThrowsWithMessage(() -> facade.claimGiftCard(token, CARD_1), GiftCardAlreadyClaimedErrorDescription);
    }

    @Test
    public void test08GetGiftCardBalance() {
        String token = loginUser(USER_ZOE);
        facade.claimGiftCard(token, CARD_1);
        int balance = facade.getGiftCardBalance(token, CARD_1);
        assertEquals(CARD_1_BALANCE, balance);
    }

    @Test
    public void test09GetBalanceWithoutClaimedGiftcard() {
        String token = loginUser(USER_MANUELITA);
        assertThrowsWithMessage(() -> facade.getGiftCardBalance(token, CARD_2), GiftCardNotFoundErrorDescription);
    }

    @Test
    public void test10GetGiftCardCharges() {
        String token = loginUser(USER_ZOE);
        facade.claimGiftCard(token, CARD_1);

        List<Charge> charges = facade.getGiftCardCharges(token, CARD_1);
        assertTrue(charges.isEmpty());

        int chargeAmount = 100;
        String description = "Remera de Lando Norris";
        facade.merchantChargeToCard(MERCHANT_1, CARD_1, chargeAmount, description);

        charges = facade.getGiftCardCharges(token, CARD_1);
        assertEquals(1, charges.size());
        assertChargeHasExpectedValues(charges.get(0), chargeAmount, MERCHANT_1, description);
    }

    @Test
    public void test11MerchantDoesChargeToCard() {
        String token = loginUser(USER_MANUELITA);
        facade.claimGiftCard(token, CARD_2);

        int chargeAmount = 200;
        String description = "Jaylen Brown jersey";
        facade.merchantChargeToCard(MERCHANT_1, CARD_2, chargeAmount, description);

        GiftCard card = giftCards.get(CARD_2);
        assertEquals(CARD_2_BALANCE - chargeAmount, card.getBalance());
        assertEquals(1, card.getCharges().size());

        assertMerchantApiWasCalledWith(CARD_2, chargeAmount);
    }

    @Test
    public void test12MerchantChargeFailsWithInvalidMerchantKey() {
        String token = loginUser(USER_MANUELITA);
        facade.claimGiftCard(token, CARD_2);
        assertThrowsWithMessage(() -> facade.merchantChargeToCard(INVALID_MERCHANT, CARD_2, 100, "Gorra Fernando Alonso"), GiftCardSystemFacade.InvalidMerchantKeyErrorDescription);
    }

    @Test
    public void test13ChargeToNonExistentGiftcard() {
        assertThrowsWithMessage(() -> facade.merchantChargeToCard(MERCHANT_1, INVALID_CARD, 100, "Giftcard Invalida"), GiftCardNotFoundErrorDescription);
    }

    @Test
    public void test14ChargeMoreThanCurrentBalance() {
        String token = loginUser(USER_ZOE);
        facade.claimGiftCard(token, CARD_1);
        assertThrows(RuntimeException.class, () -> {facade.merchantChargeToCard(MERCHANT_1, CARD_1, CARD_1_BALANCE + 500, "Otra remera de Lando Norris");});
    }

    @Test
    public void test15MultipleChargesReduceGiftcardBalance() {
        String token = loginUser(USER_ZOE);
        facade.claimGiftCard(token, CARD_1);

        int charge1 = 200;
        int charge2 = 300;
        facade.merchantChargeToCard(MERCHANT_1, CARD_1, charge1, "Gorra de Franco Colapinto");
        facade.merchantChargeToCard(MERCHANT_2, CARD_1, charge2, "Jimmy Butler Jersey");

        int balance = facade.getGiftCardBalance(token, CARD_1);
        assertEquals(CARD_1_BALANCE - charge1 - charge2, balance);

        List<Charge> charges = facade.getGiftCardCharges(token, CARD_1);
        assertEquals(2, charges.size());
    }

    @Test
    public void test16GetUserClaimedGiftcards() {
        String token = loginUser(USER_ZOE);

        List<String> claimedCards = facade.getUserClaimedCards(token);
        assertTrue(claimedCards.isEmpty());

        facade.claimGiftCard(token, CARD_1);
        claimedCards = facade.getUserClaimedCards(token);
        assertEquals(1, claimedCards.size());
        assertTrue(claimedCards.contains(CARD_1));

        facade.claimGiftCard(token, CARD_2);
        claimedCards = facade.getUserClaimedCards(token);
        assertEquals(2, claimedCards.size());
        assertTrue(claimedCards.contains(CARD_1));
        assertTrue(claimedCards.contains(CARD_2));
    }

    @Test
    public void test17TokenExpiration() {
        String token = loginUser(USER_MANUELITA);
        facade.claimGiftCard(token, CARD_2);
        testClock.advanceMinutes(6);
        assertThrowsWithMessage(() -> facade.getGiftCardBalance(token, CARD_2), GiftCardSystemFacade.TokenExpiredErrorDescription);
    }

    @Test
    public void test18SameUserWithMultipleSessions() {
        String token1 = loginUser(USER_ZOE);
        String token2 = loginUser(USER_ZOE);

        facade.claimGiftCard(token1, CARD_1);
        facade.claimGiftCard(token2, CARD_2);

        List<String> cards1 = facade.getUserClaimedCards(token1);
        List<String> cards2 = facade.getUserClaimedCards(token2);

        assertEquals(2, cards1.size());
        assertEquals(2, cards2.size());
        assertCardsContainBoth(cards1);
        assertCardsContainBoth(cards2);
    }

    @Test
    public void test19RegisterAlreadyExistingUser() {
        assertThrowsWithMessage(() -> facade.registerUser(USER_ZOE, "differentPassword"), "User already exists");
    }

    private void setupValidUsers() {
        validUsers = new HashMap<>();
        validUsers.put(USER_MANUELITA, PASSWORD_MANUELITA);
        validUsers.put(USER_ZOE, PASSWORD_ZOE);
    }

    private void setupGiftCards() {
        giftCards = new HashMap<>();
        GiftCard card1 = GiftCard.numberedWithBalance(CARD_1, CARD_1_BALANCE);
        GiftCard card2 = GiftCard.numberedWithBalance(CARD_2, CARD_2_BALANCE);
        giftCards.put(CARD_1, card1);
        giftCards.put(CARD_2, card2);
    }

    private void setupValidMerchants() {
        validMerchantKeys = new HashMap<>();
        validMerchantKeys.put(MERCHANT_1, "F1 store");
        validMerchantKeys.put(MERCHANT_2, "NBA store");
    }

    private void setupTestDependencies() {
        testMerchantApi = new TestMerchantApi();
        testClock = new TestClock();
    }

    private String loginUser(String username) {
        String password = username.equals(USER_MANUELITA) ? PASSWORD_MANUELITA : PASSWORD_ZOE;
        return facade.loginUser(username, password);
    }

    private void assertThrowsWithMessage(Runnable action, String expectedMessage) {
        RuntimeException exception = assertThrows(RuntimeException.class, action::run);
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertChargeHasExpectedValues(Charge charge, int expectedAmount, String expectedMerchant, String expectedDescription) {
        assertAll("charge properties",
                () -> assertEquals(expectedAmount, charge.getAmount()),
                () -> assertEquals(expectedMerchant, charge.getMerchantKey()),
                () -> assertEquals(expectedDescription, charge.getDescription())
        );
    }

    private void assertMerchantApiWasCalledWith(String expectedCard, int expectedAmount) {
        assertTrue(testMerchantApi.wasCalled());
        assertEquals(expectedCard, testMerchantApi.getLastCardNumber());
        assertEquals(expectedAmount, testMerchantApi.getLastAmount());
    }

    private void assertCardsContainBoth(List<String> cards) {
        assertTrue(cards.contains(CARD_1));
        assertTrue(cards.contains(CARD_2));
    }

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