package com.example.giftcardsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/giftcards")
public class GiftCardController {

    @Autowired
    private GiftCardSystemFacade giftCardSystem;

    // Nuevo: registro de usuario
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
        try {
            String user = request.get("user");
            String password = request.get("password");

            giftCardSystem.registerUser(user, password);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "user", user
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        try {
            String user = credentials.get("user");
            String password = credentials.get("password");

            String token = giftCardSystem.loginUser(user, password);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "Login successful"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/claim")
    public ResponseEntity<Map<String, String>> claimGiftCard(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String cardNumber = request.get("cardNumber");

            giftCardSystem.claimGiftCard(token, cardNumber);

            return ResponseEntity.ok(Map.of(
                    "message", "Gift card claimed successfully",
                    "cardNumber", cardNumber
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@RequestParam String token,
                                                          @RequestParam String cardNumber) {
        try {
            int balance = giftCardSystem.getGiftCardBalance(token, cardNumber);

            return ResponseEntity.ok(Map.of(
                    "cardNumber", cardNumber,
                    "balance", balance
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/charges")
    public ResponseEntity<Map<String, Object>> getCharges(@RequestParam String token,
                                                          @RequestParam String cardNumber) {
        try {
            List<Charge> charges = giftCardSystem.getGiftCardCharges(token, cardNumber);

            return ResponseEntity.ok(Map.of(
                    "cardNumber", cardNumber,
                    "charges", charges
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/claimed-cards")
    public ResponseEntity<Map<String, Object>> getUserClaimedCards(@RequestParam String token) {
        try {
            List<String> claimedCards = giftCardSystem.getUserClaimedCards(token);

            return ResponseEntity.ok(Map.of(
                    "claimedCards", claimedCards,
                    "count", claimedCards.size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/merchant/charge")
    public ResponseEntity<Map<String, String>> merchantCharge(@RequestBody Map<String, Object> request) {
        try {
            String merchantKey = (String) request.get("merchantKey");
            String cardNumber = (String) request.get("cardNumber");
            Integer amount = (Integer) request.get("amount");
            String description = (String) request.get("description");

            giftCardSystem.merchantChargeToCard(merchantKey, cardNumber, amount, description);

            return ResponseEntity.ok(Map.of(
                    "message", "Charge processed successfully",
                    "cardNumber", cardNumber,
                    "amount", String.valueOf(amount)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "Gift Card System is running",
                "version", "1.0"
        ));
    }
}
