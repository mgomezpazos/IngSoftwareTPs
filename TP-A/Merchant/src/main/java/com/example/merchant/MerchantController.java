package com.example.merchant;

import java.util.Map;
import java.util.Random;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class MerchantController {

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Merchant service is running");
    }

    @PostMapping(value = "/charge", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> processCharge(@RequestBody Map<String, Object> body) {
        String cardNumber = (String) body.get("cardNumber");
        Integer amount = (Integer) body.get("amount");
        String merchantKey = (String) body.get("merchantKey");
        String description = (String) body.get("description");

        return ResponseEntity.ok(Map.of(
                "transactionId", generateTransactionId(),
                "status", "success",
                "message", String.format("Charge processed: $%d on card %s by merchant %s",
                        amount,
                        maskCardNumber(cardNumber),
                        merchantKey)
        ));
    }

    private String generateTransactionId() {
        return "TXN-" + Math.abs(new Random().nextInt());
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
