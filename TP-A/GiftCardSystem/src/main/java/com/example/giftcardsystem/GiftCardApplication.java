package com.example.giftcardsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class GiftCardApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftCardApplication.class, args);
    }

    @Bean
    public GiftCardSystemFacade giftCardSystemFacade() {
        // Setup valid users (in a real app, this would be from database)
        Map<String, String> validUsers = new HashMap<>();
        validUsers.put("admin", "admin123");
        validUsers.put("user1", "password1");
        validUsers.put("user2", "password2");
        validUsers.put("testuser", "testpass");

        // Setup gift cards (in a real app, this would be from database)
        Map<String, GiftCard> giftCards = new HashMap<>();
        GiftCard card1 = GiftCard.numberedWithBalance("1111222233334444", 1000);
        GiftCard card2 = GiftCard.numberedWithBalance("5555666677778888", 500);
        GiftCard card3 = GiftCard.numberedWithBalance("9999000011112222", 2000);
        GiftCard card4 = GiftCard.numberedWithBalance("1234567890123456", 750);

        giftCards.put("1111222233334444", card1);
        giftCards.put("5555666677778888", card2);
        giftCards.put("9999000011112222", card3);
        giftCards.put("1234567890123456", card4);

        // Setup valid merchant keys (in a real app, this would be from database)
        Map<String, String> validMerchantKeys = new HashMap<>();
        validMerchantKeys.put("COFFEE_SHOP_001", "Downtown Coffee Shop");
        validMerchantKeys.put("BOOK_STORE_002", "University Book Store");
        validMerchantKeys.put("RESTAURANT_003", "Italian Bistro");
        validMerchantKeys.put("RETAIL_004", "Fashion Outlet");

        // Create dependencies
        MerchantApi merchantApi = new MerchantApi();
        Clock clock = new Clock();

        return new GiftCardSystemFacade(validUsers, giftCards, validMerchantKeys, merchantApi, clock);
    }
}