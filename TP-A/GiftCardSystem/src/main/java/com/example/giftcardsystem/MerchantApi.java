package com.example.giftcardsystem;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import java.util.Map;
import org.springframework.web.client.RestClient;

public class MerchantApi {

    @SuppressWarnings("unchecked")
    public String processCharge(String cardNumber, int amount, String merchantKey, String description) {
        Map<String, Object> response = (Map<String, Object>) RestClient.create()
                .post()
                .uri("http://localhost:8085/charge")
                .contentType(APPLICATION_JSON)
                .body(createChargeRequest(cardNumber, amount, merchantKey, description))
                .retrieve()
                .body(Map.class);

        Object transactionId = response.get("transactionId");
        if (transactionId == null) {
            throw new RuntimeException("Transaction ID not received from merchant service");
        }

        return transactionId.toString();
    }

    private String createChargeRequest(String cardNumber, int amount, String merchantKey, String description) {
        return String.format(
                "{ \"cardNumber\": \"%s\", \"amount\": %d, \"merchantKey\": \"%s\", \"description\": \"%s\" }",
                cardNumber, amount, merchantKey, description
        );
    }
}