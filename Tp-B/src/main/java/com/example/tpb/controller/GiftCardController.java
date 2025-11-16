package com.example.tpb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.tpb.service.GiftCardAppService;

import java.util.*;

@RestController
@RequestMapping("/api/giftcards")
public class GiftCardController {

    @Autowired
    private GiftCardAppService service;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String user, @RequestParam String pass) {
        UUID token = service.login(user, pass);
        Map<String,Object> body = new HashMap<>();
        body.put("token", token.toString());
        // opcional: expiracion en minutos
        body.put("expiresInMinutes", 15);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{cardId}/redeem")
    public ResponseEntity<String> redeemCard(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = service.extractTokenFromHeader(header);
        service.redeem(token, cardId);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Map<String,Object>> balance(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = service.extractTokenFromHeader(header);
        int balance = service.balance(token, cardId);
        Map<String,Object> body = new HashMap<>();
        body.put("cardId", cardId);
        body.put("balance", balance);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{cardId}/details")
    public ResponseEntity<Map<String,Object>> details(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = service.extractTokenFromHeader(header);
        List<String> details = service.details(token, cardId);
        Map<String,Object> body = new HashMap<>();
        body.put("cardId", cardId);
        body.put("charges", details);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API funcionando correctamente!");
    }

    @PostMapping("/{cardId}/charge")
    public ResponseEntity<String> charge(
            @RequestParam String merchant,
            @RequestParam int amount,
            @RequestParam String description,
            @PathVariable String cardId) {
        service.charge(merchant, cardId, amount, description);
        return ResponseEntity.ok("OK");
    }
}
