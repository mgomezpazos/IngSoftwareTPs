package com.example.tpb.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "giftcards")
@Getter @Setter
public class GiftCardEntity {
    @Id
    private String id;
    private int balance;
    private String owner;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "giftcard_charges", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "description")
    private List<String> charges = new ArrayList<>();
    public GiftCardEntity() {}
    public GiftCardEntity(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }
}