package com.example.tpb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.tpb.model.GiftCardFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/giftcards")
public class GiftCardController {

    @Autowired private GiftCardFacade facade;

    @ExceptionHandler( RuntimeException.class )
    public ResponseEntity<?> handleException( RuntimeException ex ) {
        return ResponseEntity.internalServerError()
                .body( Map.of( "error", ex.getMessage() ) );
    }

    @PostMapping( value = "/login", params = { "user", "pass" } )
    public ResponseEntity<?> login(
            @RequestParam String user,
            @RequestParam String pass ) {
        UUID token = facade.login( user, pass );
        return ResponseEntity.ok( Map.of( "token", token.toString() ) );
    }

    @PostMapping( "/{cardId}/redeem" )
    public ResponseEntity<?> redeem(
            @RequestHeader( "Authorization" ) String authHeader,
            @PathVariable String cardId ) {
        UUID token = extractToken( authHeader );
        facade.redeem( token, cardId );
        return ResponseEntity.ok( Map.of( "message", "Tarjeta redimida correctamente" ) );
    }

    @GetMapping( "/{cardId}/balance" )
    public ResponseEntity<?> balance(
            @RequestHeader( "Authorization" ) String authHeader,
            @PathVariable String cardId ) {
        UUID token = extractToken( authHeader );
        int balance = facade.balance( token, cardId );
        return ResponseEntity.ok( Map.of( "balance", balance ) );
    }

    @PostMapping( value = "/{cardId}/charge", params = { "merchant", "amount", "description" } )
    public ResponseEntity<?> charge(
            @PathVariable String cardId,
            @RequestParam String merchant,
            @RequestParam int amount,
            @RequestParam String description ) {
        facade.charge( merchant, cardId, amount, description );
        return ResponseEntity.ok( Map.of( "message", "Cargo realizado correctamente" ) );
    }

    @GetMapping( "/{cardId}/details" )
    public ResponseEntity<?> details(
            @RequestHeader( "Authorization" ) String authHeader,
            @PathVariable String cardId ) {
        UUID token = extractToken( authHeader );
        List<String> charges = facade.details( token, cardId );
        return ResponseEntity.ok( Map.of( "charges", charges ) );
    }

    private UUID extractToken( String authHeader ) {
        if ( authHeader == null || !authHeader.startsWith( "Bearer " ) ) {
            throw new RuntimeException( GiftCardFacade.InvalidToken );
        }
        String tokenStr = authHeader.substring( "Bearer ".length() ).trim();
        try {
            return UUID.fromString( tokenStr );
        } catch ( IllegalArgumentException ex ) {
            throw new RuntimeException( GiftCardFacade.InvalidToken );
        }
    }
}