package com.example.giftcardsystem;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return new Clock();
    }

    @Bean
    public MerchantApi merchantApi() {
        return new MerchantApi();
    }

    @Bean
    public Map<String, String> validUsers() {
        Map<String, String> users = new HashMap<>();
        users.put("user1", "password1");
        users.put("admin", "admin123");
        return users;
    }

    @Bean
    public Map<String, GiftCard> giftCards() {
        Map<String, GiftCard> cards = new HashMap<>();
        cards.put("1111222233334444", GiftCard.numberedWithBalance("1111222233334444", 1000));
        cards.put("5555666677778888", GiftCard.numberedWithBalance("5555666677778888", 500));
        cards.put("9999000011112222", GiftCard.numberedWithBalance("9999000011112222", 2000));
        cards.put("1234567890123456", GiftCard.numberedWithBalance("1234567890123456", 750));
        return cards;
    }

    @Bean
    @Qualifier("validMerchantKeys")
    public Map<String, String> validMerchantKeys() {
        Map<String, String> merchants = new HashMap<>();
        merchants.put("F1_STORE", "F1 Store");
        merchants.put("NBA_STORE", "NBA Store");
        merchants.put("HARLEY_DEALERSHIP", "Harley Davidson Dealership");
        merchants.put("V8_GAS_STATION", "Café V8 de Flo");
        return merchants;
    }
}