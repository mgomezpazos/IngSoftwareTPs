package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.tpb.persistence.entities.MerchantEntity;
import com.example.tpb.persistence.entities.UserEntity;
import com.example.tpb.service.GiftCardService;
import com.example.tpb.service.MerchantService;
import com.example.tpb.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class GiftCardFacadeTest {

    @Autowired GiftCardFacade facade;
    @Autowired UserService userService;
    @Autowired MerchantService merchantService;
    @Autowired GiftCardService giftCardService;
    @MockitoBean Clock clock;

    @BeforeEach public void beforeEach() {
        when( clock.now() ).then( it -> LocalDateTime.now() );
    }

    @Test public void test01CanLoginWithValidCredentials() {
        UserEntity user = savedUser();

        UUID token = facade.login( user.getUsername(), user.getPassword() );

        assertNotNull( token );
    }

    @Test public void test02CanNotLoginWithInvalidUser() {
        assertThrowsLike( () -> facade.login( "InvalidUser", "pass" ),
                GiftCardFacade.InvalidUser );
    }

    @Test public void test03CanNotLoginWithInvalidPassword() {
        UserEntity user = savedUser();

        assertThrowsLike( () -> facade.login( user.getUsername(), "wrongpass" ),
                GiftCardFacade.InvalidUser );
    }

    @Test public void test04CanRedeemCardWithValidToken() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );

        facade.redeem( token, cardId );

        GiftCard card = giftCardService.findById( cardId );
        assertTrue( card.isOwnedBy( user.getUsername() ) );
    }

    @Test public void test05CanNotRedeemWithInvalidToken() {
        savedUser();
        String cardId = savedCard( 100 );

        assertThrowsLike( () -> facade.redeem( UUID.randomUUID(), cardId ),
                GiftCardFacade.InvalidToken );
    }

    @Test public void test06CanNotRedeemInvalidCard() {
        UserEntity user = savedUser();
        UUID token = facade.login( user.getUsername(), user.getPassword() );

        assertThrowsLike( () -> facade.redeem( token, "INVALID_CARD" ),
                GiftCard.InvalidCard );
    }

    @Test public void test07CanNotRedeemAlreadyOwnedCard() {
        UserEntity user1 = savedUser();
        UserEntity user2 = savedUser();
        String cardId = savedCard( 100 );

        UUID token1 = facade.login( user1.getUsername(), user1.getPassword() );
        facade.redeem( token1, cardId );

        UUID token2 = facade.login( user2.getUsername(), user2.getPassword() );
        assertThrowsLike( () -> facade.redeem( token2, cardId ),
                GiftCard.InvalidCard );
    }

    @Test public void test08CanCheckBalanceOfOwnedCard() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        int balance = facade.balance( token, cardId );

        assertEquals( 100, balance );
    }

    @Test public void test09CanNotCheckBalanceOfUnownedCard() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );

        assertThrowsLike( () -> facade.balance( token, cardId ),
                GiftCard.InvalidCard );
    }

    @Test public void test10CanNotCheckBalanceOfCardOwnedByOther() {
        UserEntity user1 = savedUser();
        UserEntity user2 = savedUser();
        String cardId = savedCard( 100 );

        UUID token1 = facade.login( user1.getUsername(), user1.getPassword() );
        facade.redeem( token1, cardId );

        UUID token2 = facade.login( user2.getUsername(), user2.getPassword() );
        assertThrowsLike( () -> facade.balance( token2, cardId ),
                GiftCard.InvalidCard );
    }

    @Test public void test11CanChargeCardWithValidMerchant() {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        facade.charge( merchant.getCode(), cardId, 30, "Coffee" );

        assertEquals( 70, facade.balance( token, cardId ) );
    }

    @Test public void test12CanNotChargeWithInvalidMerchant() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );

        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        assertThrowsLike( () -> facade.charge( "INVALID_MERCHANT", cardId, 30, "Coffee" ),
                GiftCardFacade.InvalidMerchant );
    }

    @Test public void test13CanNotChargeUnownedCard() {
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        assertThrowsLike( () -> facade.charge( merchant.getCode(), cardId, 30, "Coffee" ),
                GiftCard.CargoImposible );
    }

    @Test public void test14CanNotChargeMoreThanBalance() {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        assertThrowsLike( () -> facade.charge( merchant.getCode(), cardId, 150, "Expensive" ),
                GiftCard.CargoImposible );
    }

    @Test public void test15CanGetDetailsOfOwnedCard() {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );
        facade.charge( merchant.getCode(), cardId, 30, "Coffee" );
        facade.charge( merchant.getCode(), cardId, 20, "Lunch" );

        List<String> charges = facade.details( token, cardId );

        assertEquals( 2, charges.size() );
        assertEquals( "Coffee", charges.get( 0 ) );
        assertEquals( "Lunch", charges.get( 1 ) );
    }

    @Test public void test16CanNotGetDetailsOfUnownedCard() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );

        assertThrowsLike( () -> facade.details( token, cardId ),
                GiftCard.InvalidCard );
    }

    @Test public void test17CanNotGetDetailsOfCardOwnedByOther() {
        UserEntity user1 = savedUser();
        UserEntity user2 = savedUser();
        String cardId = savedCard( 100 );

        UUID token1 = facade.login( user1.getUsername(), user1.getPassword() );
        facade.redeem( token1, cardId );

        UUID token2 = facade.login( user2.getUsername(), user2.getPassword() );
        assertThrowsLike( () -> facade.details( token2, cardId ),
                GiftCard.InvalidCard );
    }

    @Test public void test18CanNotRedeemWhenSessionIsExpired() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        assertThrowsLike( () -> facade.redeem( token, cardId ),
                UserSession.SessionExpired );
    }

    @Test public void test19CanNotCheckBalanceWhenSessionIsExpired() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        assertThrowsLike( () -> facade.balance( token, cardId ),
                UserSession.SessionExpired );
    }

    @Test public void test20CanNotGetDetailsWhenSessionIsExpired() {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        assertThrowsLike( () -> facade.details( token, cardId ),
                UserSession.SessionExpired );
    }

    @Test public void test21MultipleChargesAreAccumulated() {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        UUID token = facade.login( user.getUsername(), user.getPassword() );
        facade.redeem( token, cardId );

        facade.charge( merchant.getCode(), cardId, 30, "Coffee" );
        facade.charge( merchant.getCode(), cardId, 20, "Lunch" );
        facade.charge( merchant.getCode(), cardId, 10, "Snack" );

        assertEquals( 40, facade.balance( token, cardId ) );
        assertEquals( 3, facade.details( token, cardId ).size() );
    }

    // Helper methods

    private UserEntity savedUser() {
        return userService.save( EntityDrawer.someUser() );
    }

    private MerchantEntity savedMerchant() {
        return merchantService.save( EntityDrawer.someMerchant() );
    }

    private String savedCard( int balance ) {
        String cardId = EntityDrawer.someCardId();
        giftCardService.save( new GiftCard( cardId, balance ) );
        return cardId;
    }

    private void assertThrowsLike( Executable executable, String message ) {
        assertEquals( message,
                assertThrows( Exception.class, executable )
                        .getMessage() );
    }
}