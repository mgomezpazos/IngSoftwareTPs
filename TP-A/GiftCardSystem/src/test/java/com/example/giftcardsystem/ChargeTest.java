package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class ChargeTest {
    private Charge standardCharge;
    private static final int STANDARD_AMOUNT = 100;
    private static final String STANDARD_MERCHANT_KEY = "merchant123";
    private static final String STANDARD_DESCRIPTION = "Coffee purchase";

    @BeforeEach
    void setUp() {
        standardCharge = createCharge(STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, STANDARD_DESCRIPTION);
    }

    @Test
    public void test01ChargeCreation() {
        Charge charge = createCharge(STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, STANDARD_DESCRIPTION);
        assertChargeHasExpectedValues(charge, STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, STANDARD_DESCRIPTION);
    }

    @Test
    public void test02ChargeWithInvalidDescription() {
        assertThrowsWithMessage(() -> createCharge(STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, ""), GiftCard.InvalidChargeDescriptionError);
        assertThrowsWithMessage(() -> createCharge(STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, null), GiftCard.InvalidChargeDescriptionError);
        assertThrowsWithMessage(() -> createCharge(STANDARD_AMOUNT, STANDARD_MERCHANT_KEY, "   "), GiftCard.InvalidChargeDescriptionError);
    }

    @Test
    public void test03InvalidChargeAmount() {
        assertThrowsWithMessage(() -> createCharge(-500, STANDARD_MERCHANT_KEY, STANDARD_DESCRIPTION), GiftCard.InvalidChargeAmount);
        assertThrowsWithMessage(() -> createCharge(0, STANDARD_MERCHANT_KEY, STANDARD_DESCRIPTION), GiftCard.InvalidChargeAmount);
    }

    @Test
    public void test04ChargeWithInvalidMerchantKey() {
        assertThrowsWithMessage(() -> createCharge(STANDARD_AMOUNT, "", STANDARD_DESCRIPTION), GiftCard.InvalidMerchantKeyError);

        assertThrowsWithMessage(
                () -> createCharge(STANDARD_AMOUNT, null, STANDARD_DESCRIPTION),
                GiftCard.InvalidMerchantKeyError
        );

        assertThrowsWithMessage(
                () -> createCharge(STANDARD_AMOUNT, "   ", STANDARD_DESCRIPTION),
                GiftCard.InvalidMerchantKeyError
        );
    }


    private Charge createCharge(int amount, String merchantKey, String description) {
        return new Charge(amount, merchantKey, description);
    }

    private void assertChargeHasExpectedValues(Charge charge, int expectedAmount,
                                               String expectedMerchantKey, String expectedDescription) {
        assertAll("charge properties",
                () -> assertEquals(expectedAmount, charge.getAmount(), "Amount should match"),
                () -> assertEquals(expectedMerchantKey, charge.getMerchantKey(), "Merchant key should match"),
                () -> assertEquals(expectedDescription, charge.getDescription(), "Description should match")
        );
    }

    private void assertThrowsWithMessage(Runnable action, String expectedMessage) {
        RuntimeException exception = assertThrows(RuntimeException.class, action::run);
        assertEquals(expectedMessage, exception.getMessage());
    }
}