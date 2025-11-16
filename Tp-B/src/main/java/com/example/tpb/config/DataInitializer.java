package com.example.tpb.config;

import com.example.tpb.persistence.repositories.GiftCardRepository;
import com.example.tpb.persistence.repositories.MerchantRepository;
import com.example.tpb.persistence.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.tpb.persistence.entities.*;
import com.example.tpb.persistence.repositories.*;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired private GiftCardRepository cardRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private MerchantRepository merchantRepo;

    @PostConstruct
    public void init() {
        if (userRepo.count() == 0) {
            userRepo.save(new UserEntity("alice", "1234"));
            userRepo.save(new UserEntity("bob", "abcd"));
        }

        if (merchantRepo.count() == 0) {
            merchantRepo.save(new MerchantEntity("AMAZON"));
            merchantRepo.save(new MerchantEntity("SHOP"));
        }

        if (cardRepo.count() == 0) {
            GiftCardEntity c1 = new GiftCardEntity("card01", 1000);
            GiftCardEntity c2 = new GiftCardEntity("card02", 500);
            cardRepo.saveAll(List.of(c1, c2));
        }
    }
}
