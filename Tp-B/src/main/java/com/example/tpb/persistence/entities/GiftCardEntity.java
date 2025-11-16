package com.example.tpb.persistence.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "giftcards")
public class GiftCardEntity {

    @Id
    private String id;

    private int balance;

    private String owner; // null si no está redimida

    @ElementCollection
    @CollectionTable(name = "giftcard_charges", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "description")
    private List<String> charges = new ArrayList<>();

    public GiftCardEntity() {}

    public GiftCardEntity(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public List<String> getCharges() { return charges; }
    public void setCharges(List<String> charges) { this.charges = charges; }
}
