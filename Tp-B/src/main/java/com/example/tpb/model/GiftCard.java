package com.example.tpb.model;

import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";
    public static final String CardAlreadyRedeemed = "CardAlreadyRedeemed";
    public static final String InvalidBalance = "InvalidBalance";
    public static final String InvalidOwner = "InvalidOwner";
    public static final String InvalidDescription = "InvalidDescription";

    private String id;
    private int balance;
    private String owner;
    private List<String> charges = new ArrayList<>();

    public GiftCard(String id, int initialBalance) {
        validateId(id);
        validateBalance(initialBalance);
        this.id= id;
        this.balance=initialBalance;
    }

    public GiftCard(String id, int balance, String owner, List<String> charges) {
        validateId(id);
        this.id= id;
        this.balance= balance;
        this.owner= owner;
        this.charges=new ArrayList<>(charges);
    }

    public GiftCard charge(int anAmount, String description) {
        validateAmount(anAmount);
        validateDescription(description);
        assertCanCharge(anAmount);
        balance = balance - anAmount;
        charges.add(description);
        return this;
    }

    private void assertCanCharge(int anAmount) {
        if (!owned() || (balance - anAmount < 0)) throw new RuntimeException(CargoImposible);
    }

    public GiftCard redeem(String newOwner) {
        validateOwner(newOwner);
        assertCanRedeem(newOwner);
        owner = newOwner;
        return this;
    }

    private void assertCanRedeem(String newOwner) {
        if (owned()) {
            if (isOwnedBy(newOwner)) {throw new RuntimeException(CardAlreadyRedeemed);}
            else {throw new RuntimeException(InvalidCard);}
        }
    }

    public void assertIsOwnedBy(String expectedOwner) {
        if (!owned() || !owner.equals(expectedOwner)) throw new RuntimeException(InvalidCard);
    }

    private void validateId(String id) {
        if (id == null || id.isEmpty()) {throw new RuntimeException(InvalidCard);}
    }

    private void validateBalance(int balance) {
        if (balance <= 0) {throw new RuntimeException(InvalidBalance);}
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {throw new RuntimeException(CargoImposible);}
    }

    private void validateOwner(String owner) {
        if (owner == null || owner.isEmpty()) {throw new RuntimeException(InvalidOwner);}
    }

    private void validateDescription(String description) {
        if (description == null) {throw new RuntimeException(InvalidDescription);}
    }

    public boolean owned() {return owner != null;}
    public boolean isOwnedBy(String aPossibleOwner) {return owned() && owner.equals(aPossibleOwner);}
    public String id() {return id;}
    public int balance() {return balance;}
    public String owner() {return owner;}
    public List<String> charges() {return new ArrayList<>(charges);}
}