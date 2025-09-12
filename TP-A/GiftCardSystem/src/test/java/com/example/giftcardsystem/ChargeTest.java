package com.example.giftcardsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class ChargeTest {

    private Charge charge;
    private LocalDateTime beforeCreation;

    @BeforeEach
    public void setUp() {
        beforeCreation = LocalDateTime.now();
        charge = new Charge(100, "merchant123", "Coffee purchase");
    }

    @Test
    public void testChargeCreation() {
        assertEquals(100, charge.getAmount());
        assertEquals("merchant123", charge.getMerchantKey());
        assertEquals("Coffee purchase", charge.getDescription());

        // Verify timestamp is recent (within last few seconds)
        LocalDateTime afterCreation = LocalDateTime.now();
        assertTrue(charge.getTimestamp().isAfter(beforeCreation) ||
                charge.getTimestamp().isEqual(beforeCreation));
        assertTrue(charge.getTimestamp().isBefore(afterCreation) ||
                charge.getTimestamp().isEqual(afterCreation));
    }

    @Test
    public void testChargeWithDifferentValues() {
        Charge expensiveCharge = new Charge(999, "premium-merchant", "Luxury item");

        assertEquals(999, expensiveCharge.getAmount());
        assertEquals("premium-merchant", expensiveCharge.getMerchantKey());
        assertEquals("Luxury item", expensiveCharge.getDescription());
    }

    @Test
    public void testChargeWithEmptyDescription() {
        Charge emptyDescCharge = new Charge(50, "merchant456", "");

        assertEquals(50, emptyDescCharge.getAmount());
        assertEquals("merchant456", emptyDescCharge.getMerchantKey());
        assertEquals("", emptyDescCharge.getDescription());
    }

    @Test
    public void testChargeTimestampIsImmutable() {
        LocalDateTime originalTimestamp = charge.getTimestamp();

        // Wait a bit and get timestamp again
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LocalDateTime secondRead = charge.getTimestamp();
        assertEquals(originalTimestamp, secondRead);
    }

    @Test
    public void testMultipleChargesHaveDifferentTimestamps() {
        Charge firstCharge = new Charge(100, "merchant1", "First");

        try {
            Thread.sleep(1); // Ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Charge secondCharge = new Charge(200, "merchant2", "Second");

        assertTrue(secondCharge.getTimestamp().isAfter(firstCharge.getTimestamp()) ||
                secondCharge.getTimestamp().isEqual(firstCharge.getTimestamp()));
    }
}
