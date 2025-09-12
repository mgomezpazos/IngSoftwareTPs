package com.example.giftcardsystem;

import java.time.LocalDateTime;

public class Charge {
    private int amount;
    private String merchantKey;
    private String description;
    private LocalDateTime timestamp;

    public Charge(int amount, String merchantKey, String description) {
        this.amount = amount;
        this.merchantKey = merchantKey;
        this.description = description;
        this.timestamp = LocalDateTime.now();
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}