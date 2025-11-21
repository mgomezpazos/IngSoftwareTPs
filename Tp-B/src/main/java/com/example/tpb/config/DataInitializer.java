package com.example.tpb.config;

import com.example.tpb.persistence.repositories.GiftCardRepository;
import com.example.tpb.persistence.repositories.MerchantRepository;
import com.example.tpb.persistence.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.tpb.persistence.entities.*;
import com.example.tpb.persistence.repositories.*;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired private GiftCardRepository cardRepo;
        @Autowired private UserRepository userRepo;
        @Autowired private MerchantRepository merchantRepo;

        @Override
        public void run(String... args) throws Exception {
            if (userRepo.count() == 0) {
                userRepo.save(new UserEntity("Emilio", "motos123"));
                userRepo.save(new UserEntity("Julio", "polimorfismo"));
                userRepo.save(new UserEntity("Manuelita", "mylittleponny"));
                userRepo.save(new UserEntity("Zoe", "vroom"));
            }

            if (merchantRepo.count() == 0) {
                merchantRepo.save(new MerchantEntity("Harley Davidson"));
                merchantRepo.save(new MerchantEntity("NBA Store"));
                merchantRepo.save(new MerchantEntity("F1 store"));
            }

            if (cardRepo.count() == 0) {
                GiftCardEntity c1 = new GiftCardEntity("GiftCard1", 1000);
                GiftCardEntity c2 = new GiftCardEntity("GiftCard2", 5000);
                cardRepo.saveAll(List.of(c1, c2));
            }
        }
}
