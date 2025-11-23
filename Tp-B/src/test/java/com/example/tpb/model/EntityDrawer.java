package com.example.tpb.model;

import com.example.tpb.persistence.entities.MerchantEntity;
import com.example.tpb.persistence.entities.UserEntity;

import java.time.Instant;
import java.util.Random;

public class EntityDrawer {

    static private Random rnd= new Random(Instant.now().getEpochSecond());

    static public UserEntity someUser() {return new UserEntity("Manu" + rnd.nextInt(), "manu123");}

    static public MerchantEntity someMerchant() {return new MerchantEntity("TestMerchant" + rnd.nextInt());}

    static public String someCardId() {return "CARD" + rnd.nextInt(100000, 999999);}
    static public int randomAmount() {return rnd.nextInt(100, 1000);}
}