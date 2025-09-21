package com.example.giftcardsystem;

import java.time.LocalDateTime;

import static com.example.giftcardsystem.GiftCard.InvalidChargeDescriptionError;
import static com.example.giftcardsystem.GiftCard.InvalidMerchantKeyError;

public class Charge {
    private final int amount;
    private final String merchantKey;
    private final String description;

    public Charge(int amount, String merchantKey, String description) {
        validateAmount(amount);
        validateStringNotNullOrEmpty(merchantKey, InvalidMerchantKeyError);
        validateStringNotNullOrEmpty(description, InvalidChargeDescriptionError);

        this.amount = amount;
        this.merchantKey = merchantKey;
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public String getDescription() {
        return description;
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new RuntimeException(GiftCard.InvalidChargeAmount);
        }
    }

    private void validateStringNotNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(errorMessage);
        }
    }
}