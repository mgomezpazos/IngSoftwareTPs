package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    private GiftCard giftCard;

    @BeforeEach
    public void setUp() {
        giftCard = GiftCard.numberedWithBalance("1234567890123456", 1000);
    }

    @Test
    public void testCreateGiftCard() {
        assertEquals("1234567890123456", giftCard.getCardNumber());
        assertEquals(1000, giftCard.getInitialBalance());
        assertEquals(1000, giftCard.getBalance());
        assertFalse(giftCard.isClaimed());
        assertTrue(giftCard.getCharges().isEmpty());
    }

    @Test
    public void testClaimGiftCard() {
        giftCard.claimBy("Manuela");

        assertTrue(giftCard.isClaimed());
        assertTrue(giftCard.isClaimedBy("Manuela"));
        assertFalse(giftCard.isClaimedBy("Zöe"));
        assertEquals("Manuela", giftCard.getOwner());
    }

    @Test
    public void testCannotClaimAlreadyClaimedCard() {
        giftCard.claimBy("Manuela");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            giftCard.claimBy("Zöe");
        });

        assertEquals(GiftCard.GiftCardAlreadyClaimedErrorDescription, exception.getMessage());
    }

    @Test
    public void testChargeAmountOnClaimedCard() {
        giftCard.claimBy("Manuela");

        giftCard.chargeAmount(100, "merchant1", "Test purchase");

        assertEquals(900, giftCard.getBalance());
        assertEquals(1, giftCard.getCharges().size());

        Charge charge = giftCard.getCharges().get(0);
        assertEquals(100, charge.getAmount());
        assertEquals("merchant1", charge.getMerchantKey());
        assertEquals("Test purchase", charge.getDescription());
    }

    @Test
    public void testCannotChargeUnclaimedCard() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            giftCard.chargeAmount(100, "merchant1", "Test purchase");
        });

        assertEquals(GiftCard.GiftCardNotClaimedErrorDescription, exception.getMessage());
    }

    @Test
    public void testCannotChargeInvalidAmount() {
        giftCard.claimBy("Manuela");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            giftCard.chargeAmount(0, "merchant1", "Test purchase");
        });

        assertEquals(GiftCard.InvalidChargeAmountErrorDescription, exception.getMessage());

        exception = assertThrows(RuntimeException.class, () -> {
            giftCard.chargeAmount(-50, "merchant1", "Test purchase");
        });

        assertEquals(GiftCard.InvalidChargeAmountErrorDescription, exception.getMessage());
    }

    @Test
    public void testCannotChargeMoreThanBalance() {
        giftCard.claimBy("Manuela");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            giftCard.chargeAmount(1500, "merchant1", "Expensive purchase");
        });

        assertEquals(GiftCard.InsufficientBalanceErrorDescription, exception.getMessage());
        assertEquals(1000, giftCard.getBalance()); // Balance unchanged
    }

    @Test
    public void testMultipleCharges() {
        giftCard.claimBy("Manuela");

        giftCard.chargeAmount(200, "merchant1", "First purchase");
        giftCard.chargeAmount(300, "merchant2", "Second purchase");

        assertEquals(500, giftCard.getBalance());
        assertEquals(2, giftCard.getCharges().size());

        assertEquals(200, giftCard.getCharges().get(0).getAmount());
        assertEquals(300, giftCard.getCharges().get(1).getAmount());
    }

    @Test
    public void testExactBalanceCharge() {
        giftCard.claimBy("Manuela");

        giftCard.chargeAmount(1000, "merchant1", "Full balance purchase");

        assertEquals(0, giftCard.getBalance());
        assertEquals(1, giftCard.getCharges().size());
    }
}
