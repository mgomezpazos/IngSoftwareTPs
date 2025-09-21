package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    private GiftCard giftCard;

    private static final String CARD_NUMBER = "1234567891";
    private static final int INITIAL_BALANCE = 1000;
    private static final String OWNER_MANUELA = "Manuela";
    private static final String OWNER_ZOE = "Zöe";
    private static final String MERCHANT_1 = "merchant1";
    private static final String MERCHANT_2 = "merchant2";
    private static final String DESCRIPTION_REMERA = "Remera del nano";
    private static final String DESCRIPTION_GORRA = "Gorra del nano";

    @BeforeEach
    public void setUp() {
        giftCard = GiftCard.numberedWithBalance(CARD_NUMBER, INITIAL_BALANCE);
    }

    @Test
    public void test01CreateGiftCard() {
        assertEquals(CARD_NUMBER, giftCard.getCardNumber());
        assertEquals(INITIAL_BALANCE, giftCard.getInitialBalance());
        assertEquals(INITIAL_BALANCE, giftCard.getBalance());
        assertFalse(giftCard.isClaimed());
        assertTrue(giftCard.getCharges().isEmpty());
    }

    @Test
    public void test02ClaimGiftCard() {
        giftCard.claimBy(OWNER_MANUELA);

        assertTrue(giftCard.isClaimed());
        assertTrue(giftCard.isClaimedBy(OWNER_MANUELA));
        assertFalse(giftCard.isClaimedBy(OWNER_ZOE));
        assertEquals(OWNER_MANUELA, giftCard.getOwner());
    }

    @Test
    public void test03CannotClaimAlreadyClaimedCard() {
        giftCard.claimBy(OWNER_MANUELA);

        assertThrowsWithMessage(() -> giftCard.claimBy(OWNER_ZOE), GiftCard.GiftCardAlreadyClaimedErrorDescription);
    }

    @Test
    public void test04ChargeAmountOnClaimedCard() {
        giftCard.claimBy(OWNER_MANUELA);
        int chargeAmount = 100;

        giftCard.chargeAmount(chargeAmount, MERCHANT_1, DESCRIPTION_REMERA);

        assertEquals(INITIAL_BALANCE - chargeAmount, giftCard.getBalance());
        assertEquals(1, giftCard.getCharges().size());

        Charge charge = giftCard.getCharges().get(0);
        assertChargeHasExpectedValues(charge, chargeAmount, MERCHANT_1, DESCRIPTION_REMERA);
    }

    @Test
    public void test05CannotChargeUnclaimedCard() {
        assertThrowsWithMessage(() -> giftCard.chargeAmount(100, MERCHANT_1, DESCRIPTION_REMERA), GiftCard.GiftCardNotClaimedErrorDescription);
    }

    @Test
    public void test06CannotChargeInvalidAmount() {
        giftCard.claimBy(OWNER_MANUELA);
        assertThrowsWithMessage(() -> giftCard.chargeAmount(0, MERCHANT_1, DESCRIPTION_REMERA), GiftCard.InvalidChargeAmountError);
        assertThrowsWithMessage(() -> giftCard.chargeAmount(-50, MERCHANT_1, DESCRIPTION_REMERA), GiftCard.InvalidChargeAmountError);
    }

    @Test
    public void test07CannotChargeMoreThanBalance() {
        giftCard.claimBy(OWNER_MANUELA);
        int excessiveAmount = INITIAL_BALANCE + 500;

        assertThrowsWithMessage(() -> giftCard.chargeAmount(excessiveAmount, MERCHANT_1, "Casco del nano"), GiftCard.InsufficientBalanceError);
        assertEquals(INITIAL_BALANCE, giftCard.getBalance());
    }

    @Test
    public void test08MultipleCharges() {
        giftCard.claimBy(OWNER_MANUELA);
        int charge1 = 200;
        int charge2 = 300;

        giftCard.chargeAmount(charge1, MERCHANT_1, DESCRIPTION_GORRA);
        giftCard.chargeAmount(charge2, MERCHANT_2, "Kevin Durant Jersey");

        assertEquals(INITIAL_BALANCE - charge1 - charge2, giftCard.getBalance());
        assertEquals(2, giftCard.getCharges().size());

        assertChargeHasExpectedValues(giftCard.getCharges().get(0), charge1, MERCHANT_1, DESCRIPTION_GORRA);
        assertChargeHasExpectedValues(giftCard.getCharges().get(1), charge2, MERCHANT_2, "Kevin Durant Jersey");
    }

    @Test
    public void test09ExactBalanceCharge() {
        giftCard.claimBy(OWNER_MANUELA);
        giftCard.chargeAmount(INITIAL_BALANCE, MERCHANT_1, "Campera del nano");

        assertEquals(0, giftCard.getBalance());
        assertEquals(1, giftCard.getCharges().size());
    }

    @Test
    public void test10CreateGiftCardWithNegativeBalance() {
        assertThrowsWithMessage(() -> GiftCard.numberedWithBalance(CARD_NUMBER, -100), GiftCard.InvalidInitialBalanceError);
    }

    @Test
    public void test11CreateGiftCardWithInvalidCardNumber() {
        assertThrowsWithMessage(() -> GiftCard.numberedWithBalance(null, INITIAL_BALANCE), GiftCard.InvalidCardNumberError);
        assertThrowsWithMessage(() -> GiftCard.numberedWithBalance("", INITIAL_BALANCE), GiftCard.InvalidCardNumberError);
        assertThrowsWithMessage(() -> GiftCard.numberedWithBalance("   ", INITIAL_BALANCE), GiftCard.InvalidCardNumberError);
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
}