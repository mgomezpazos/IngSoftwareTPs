package com.example.tpb.model;

import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";
    public static final String CardAlreadyRedeemed = "CardAlreadyRedeemed";

    private String id;
    private int balance;
    private String owner;
    private List<String> charges = new ArrayList<>();

    public GiftCard( String id, int initialBalance ) {
        this.id = id;
        this.balance = initialBalance;
    }

    public GiftCard( String id, int balance, String owner, List<String> charges ) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
        this.charges = new ArrayList<>( charges );
    }

    public GiftCard charge( int anAmount, String description ) {
        assertCanCharge( anAmount );

        balance = balance - anAmount;
        charges.add( description );

        return this;
    }

    private void assertCanCharge( int anAmount ) {
        if ( !owned() || ( balance - anAmount < 0 ) ) throw new RuntimeException( CargoImposible );
    }

    public GiftCard redeem( String newOwner ) {
        assertCanRedeem( newOwner );

        owner = newOwner;
        return this;
    }

    private void assertCanRedeem( String newOwner ) {
        if ( owned() ) {
            if ( isOwnedBy( newOwner ) ) {
                throw new RuntimeException( CardAlreadyRedeemed );
            } else {
                throw new RuntimeException( InvalidCard );
            }
        }
    }

    public void assertIsOwnedBy( String expectedOwner ) {
        if ( !owned() || !owner.equals( expectedOwner ) ) throw new RuntimeException( InvalidCard );
    }

    // projectors
    public boolean owned() {return owner != null;}
    public boolean isOwnedBy( String aPossibleOwner ) { return owned() && owner.equals( aPossibleOwner );  }

    // accessors
    public String id() {return id;}
    public int balance() {return balance;}
    public String owner() {return owner;}
    public List<String> charges() {return new ArrayList<>( charges );}
}