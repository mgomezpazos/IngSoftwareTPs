package com.example.giftcardsystem;

import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String GiftCardAlreadyClaimedErrorDescription = "Gift card has been already claimed";
    public static final String GiftCardNotClaimedErrorDescription = "Gift card has not been claimed yet";
    public static final String InvalidCardNumberError = "Card number cannot be null or empty";
    public static final String InvalidOwnerError = "Invalid owner";
    public static final String InvalidMerchantKeyError = "Invalid merchant key";
    public static final String InvalidChargeDescriptionError = "Invalid description";
    public static final String InvalidInitialBalanceError = "Initial balance cannot be negative";
    public static final String InvalidChargeAmountError = "Charge amount must be positive";
    public static final String InsufficientBalanceError = "Insufficient balance for this charge";
    public static final String InvalidMerchantErrorDescription = "Invalid merchant";
    public static final String InvalidChargeAmount = "Invalid charge amount";

    private final String cardNumber;
    private final int initialBalance;
    private int currentBalance;
    private String ownerUser;
    private boolean claimed;
    private final List<Charge> charges;

    public GiftCard(String cardNumber, int initialBalance) {
        validateInitialBalance(initialBalance);
        validateStringNotNullOrEmpty(cardNumber, InvalidCardNumberError);

        this.cardNumber = cardNumber;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance;
        this.claimed = false;
        this.charges = new ArrayList<>();
    }

    public static GiftCard numberedWithBalance(String cardNumber, int balance) {
        return new GiftCard(cardNumber, balance);
    }

    public void claimBy(String user) {
        validateNotClaimed();
        validateStringNotNullOrEmpty(user, InvalidOwnerError);

        this.claimed = true;
        this.ownerUser = user;
    }

    public void chargeAmount(int amount, String merchantKey, String description) {
        validateClaimed();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        validateStringNotNullOrEmpty(merchantKey, InvalidMerchantErrorDescription);
        validateStringNotNullOrEmpty(description, InvalidChargeDescriptionError);

        Charge charge = new Charge(amount, merchantKey, description);
        charges.add(charge);
        currentBalance -= amount;
    }

    private static void validateInitialBalance(int balance) {
        if (balance < 0) {
            throw new RuntimeException(InvalidInitialBalanceError);
        }
    }

    private static void validateStringNotNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(errorMessage);
        }
    }

    private void validateNotClaimed() {
        if (claimed) {
            throw new RuntimeException(GiftCardAlreadyClaimedErrorDescription);
        }
    }

    private void validateClaimed() {
        if (!claimed) {
            throw new RuntimeException(GiftCardNotClaimedErrorDescription);
        }
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new RuntimeException(InvalidChargeAmountError);
        }
    }

    private void validateSufficientBalance(int amount) {
        if (currentBalance < amount) {
            throw new RuntimeException(InsufficientBalanceError);
        }
    }

    public boolean isClaimed() {return claimed;}
    public boolean isClaimedBy(String user) {return claimed && ownerUser.equals(user);}
    public int getBalance() {return currentBalance;}
    public List<Charge> getCharges() {return new ArrayList<>(charges);}
    public String getCardNumber() {return cardNumber;}
    public String getOwner() {return ownerUser;}
    public int getInitialBalance() {return initialBalance;}
}