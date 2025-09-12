package com.example.giftcardsystem;

import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String GiftCardAlreadyClaimedErrorDescription = "Gift card is already claimed";
    public static final String GiftCardNotClaimedErrorDescription = "Gift card is not claimed yet";
    public static final String InsufficientBalanceErrorDescription = "Insufficient balance for this charge";
    public static final String InvalidChargeAmountErrorDescription = "Charge amount must be positive";

    private final String cardNumber;
    private final int initialBalance;
    private int currentBalance;
    private String ownerUser;
    private boolean claimed;
    private List<Charge> charges;

    public GiftCard(String cardNumber, int initialBalance) {
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
        assertIsNotClaimed();
        this.claimed = true;
        this.ownerUser = user;
    }

    public void chargeAmount(int amount, String merchantKey, String description) {
        assertIsClaimed();
        assertIsValidChargeAmount(amount);
        assertHasSufficientBalance(amount);

        Charge charge = new Charge(amount, merchantKey, description);
        charges.add(charge);
        currentBalance -= amount;
    }

    private void assertIsNotClaimed() {
        if (claimed) {
            throw new RuntimeException(GiftCardAlreadyClaimedErrorDescription);
        }
    }

    private void assertIsClaimed() {
        if (!claimed) {
            throw new RuntimeException(GiftCardNotClaimedErrorDescription);
        }
    }

    private void assertIsValidChargeAmount(int amount) {
        if (amount <= 0) {
            throw new RuntimeException(InvalidChargeAmountErrorDescription);
        }
    }

    private void assertHasSufficientBalance(int amount) {
        if (currentBalance < amount) {
            throw new RuntimeException(InsufficientBalanceErrorDescription);
        }
    }

    public boolean isClaimed() {
        return claimed;
    }

    public boolean isClaimedBy(String user) {
        return claimed && ownerUser.equals(user);
    }

    public int getBalance() {
        return currentBalance;
    }

    public List<Charge> getCharges() {
        return new ArrayList<>(charges);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getOwner() {
        return ownerUser;
    }

    public int getInitialBalance() {
        return initialBalance;
    }
}
