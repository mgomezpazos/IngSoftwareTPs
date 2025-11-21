package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tpb.model.Clock;
import com.example.tpb.model.EntityDrawer;
import com.example.tpb.model.GiftCard;
import com.example.tpb.persistence.entities.MerchantEntity;
import com.example.tpb.persistence.entities.UserEntity;
import com.example.tpb.service.GiftCardService;
import com.example.tpb.service.MerchantService;
import com.example.tpb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class GiftCardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired MerchantService merchantService;
    @Autowired GiftCardService giftCardService;
    @MockitoBean
    Clock clock;

    @BeforeEach public void beforeEach() {
        when( clock.now() ).then( it -> LocalDateTime.now() );
    }

    @Test public void test01CanLoginWithValidCredentials() throws Exception {
        UserEntity user = savedUser();

        String token = loginUser( user.getUsername(), user.getPassword() );

        assertNotNull( token );
        assertFalse( token.isEmpty() );
    }

    @Test public void test02CanNotLoginWithInvalidUser() throws Exception {
        loginUserFailing( "InvalidUser", "pass" );
    }

    @Test public void test03CanNotLoginWithInvalidPassword() throws Exception {
        UserEntity user = savedUser();

        loginUserFailing( user.getUsername(), "wrongpass" );
    }

    @Test public void test04CanRedeemCardAfterLogin() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );

        redeemCard( token, cardId );

        int balance = getBalance( token, cardId );
        assertEquals( 100, balance );
    }

    @Test public void test05CanNotRedeemWithoutToken() throws Exception {
        String cardId = savedCard( 100 );

        redeemCardFailing( null, cardId );
    }

    @Test public void test06CanNotRedeemWithInvalidToken() throws Exception {
        savedUser();
        String cardId = savedCard( 100 );

        redeemCardFailing( "invalid-token", cardId );
    }

    @Test public void test07CanNotRedeemInvalidCard() throws Exception {
        UserEntity user = savedUser();
        String token = loginUser( user.getUsername(), user.getPassword() );

        redeemCardFailing( token, "INVALID_CARD" );
    }

    @Test public void test08CanNotRedeemAlreadyOwnedCard() throws Exception {
        UserEntity user1 = savedUser();
        UserEntity user2 = savedUser();
        String cardId = savedCard( 100 );

        String token1 = loginUser( user1.getUsername(), user1.getPassword() );
        redeemCard( token1, cardId );

        String token2 = loginUser( user2.getUsername(), user2.getPassword() );
        redeemCardFailing( token2, cardId );
    }

    @Test public void test09CanGetBalanceOfOwnedCard() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        int balance = getBalance( token, cardId );

        assertEquals( 100, balance );
    }

    @Test public void test10CanNotGetBalanceWithoutToken() throws Exception {
        String cardId = savedCard( 100 );

        getBalanceFailing( null, cardId );
    }

    @Test public void test11CanNotGetBalanceOfUnownedCard() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );

        getBalanceFailing( token, cardId );
    }

    @Test public void test12CanNotGetBalanceOfCardOwnedByOther() throws Exception {
        UserEntity user1 = savedUser();
        UserEntity user2 = savedUser();
        String cardId = savedCard( 100 );

        String token1 = loginUser( user1.getUsername(), user1.getPassword() );
        redeemCard( token1, cardId );

        String token2 = loginUser( user2.getUsername(), user2.getPassword() );
        getBalanceFailing( token2, cardId );
    }

    @Test public void test13CanChargeCardWithValidMerchant() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        chargeCard( merchant.getCode(), cardId, 30, "Coffee" );

        assertEquals( 70, getBalance( token, cardId ) );
    }

    @Test public void test14CanNotChargeWithInvalidMerchant() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );

        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        chargeCardFailing( "INVALID_MERCHANT", cardId, 30, "Coffee" );
    }

    @Test public void test15CanNotChargeUnownedCard() throws Exception {
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        chargeCardFailing( merchant.getCode(), cardId, 30, "Coffee" );
    }

    @Test public void test16CanNotChargeMoreThanBalance() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        chargeCardFailing( merchant.getCode(), cardId, 150, "Expensive" );
    }

    @Test public void test17CanGetDetailsOfOwnedCard() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );
        chargeCard( merchant.getCode(), cardId, 30, "Coffee" );
        chargeCard( merchant.getCode(), cardId, 20, "Lunch" );

        List<String> charges = getDetails( token, cardId );

        assertEquals( 2, charges.size() );
        assertEquals( "Coffee", charges.get( 0 ) );
        assertEquals( "Lunch", charges.get( 1 ) );
    }

    @Test public void test18CanNotGetDetailsWithoutToken() throws Exception {
        String cardId = savedCard( 100 );

        getDetailsFailing( null, cardId );
    }

    @Test public void test19CanNotGetDetailsOfUnownedCard() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );

        getDetailsFailing( token, cardId );
    }

    @Test public void test20MultipleChargesUpdateBalanceCorrectly() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard( 100 );

        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        chargeCard( merchant.getCode(), cardId, 30, "Coffee" );
        chargeCard( merchant.getCode(), cardId, 20, "Lunch" );
        chargeCard( merchant.getCode(), cardId, 10, "Snack" );

        assertEquals( 40, getBalance( token, cardId ) );
        assertEquals( 3, getDetails( token, cardId ).size() );
    }

    @Test public void test21CanNotRedeemWhenSessionIsExpired() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        redeemCardFailing( token, cardId );
    }

    @Test public void test22CanNotGetBalanceWhenSessionIsExpired() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        getBalanceFailing( token, cardId );
    }

    @Test public void test23CanNotGetDetailsWhenSessionIsExpired() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );

        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );

        getDetailsFailing( token, cardId );
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

    private String loginUser( String username, String password ) throws Exception {
        String response = mockMvc.perform( post( "/api/giftcards/login" )
                        .param( "user", username )
                        .param( "pass", password ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) )
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> map = new ObjectMapper().readValue( response, Map.class );
        return map.get( "token" );
    }

    private void loginUserFailing( String username, String password ) throws Exception {
        mockMvc.perform( post( "/api/giftcards/login" )
                        .param( "user", username )
                        .param( "pass", password ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private void redeemCard( String token, String cardId ) throws Exception {
        mockMvc.perform( post( "/api/giftcards/" + cardId + "/redeem" )
                        .header( "Authorization", "Bearer " + token ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
    }

    private void redeemCardFailing( String token, String cardId ) throws Exception {
        if ( token == null ) {
            mockMvc.perform( post( "/api/giftcards/" + cardId + "/redeem" ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        } else {
            mockMvc.perform( post( "/api/giftcards/" + cardId + "/redeem" )
                            .header( "Authorization", "Bearer " + token ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        }
    }

    private int getBalance( String token, String cardId ) throws Exception {
        String response = mockMvc.perform( get( "/api/giftcards/" + cardId + "/balance" )
                        .header( "Authorization", "Bearer " + token ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) )
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Integer> map = new ObjectMapper().readValue( response, Map.class );
        return map.get( "balance" );
    }

    private void getBalanceFailing( String token, String cardId ) throws Exception {
        if ( token == null ) {
            mockMvc.perform( get( "/api/giftcards/" + cardId + "/balance" ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        } else {
            mockMvc.perform( get( "/api/giftcards/" + cardId + "/balance" )
                            .header( "Authorization", "Bearer " + token ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        }
    }

    private void chargeCard( String merchant, String cardId, int amount, String description ) throws Exception {
        mockMvc.perform( post( "/api/giftcards/" + cardId + "/charge" )
                        .param( "merchant", merchant )
                        .param( "amount", String.valueOf( amount ) )
                        .param( "description", description ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
    }

    private void chargeCardFailing( String merchant, String cardId, int amount, String description ) throws Exception {
        mockMvc.perform( post( "/api/giftcards/" + cardId + "/charge" )
                        .param( "merchant", merchant )
                        .param( "amount", String.valueOf( amount ) )
                        .param( "description", description ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private List<String> getDetails( String token, String cardId ) throws Exception {
        String response = mockMvc.perform( get( "/api/giftcards/" + cardId + "/details" )
                        .header( "Authorization", "Bearer " + token ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) )
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, List<String>> map = new ObjectMapper().readValue( response, Map.class );
        return map.get( "charges" );
    }

    private void getDetailsFailing( String token, String cardId ) throws Exception {
        if ( token == null ) {
            mockMvc.perform( get( "/api/giftcards/" + cardId + "/details" ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        } else {
            mockMvc.perform( get( "/api/giftcards/" + cardId + "/details" )
                            .header( "Authorization", "Bearer " + token ) )
                    .andDo( print() )
                    .andExpect( status().is( 500 ) );
        }
    }
}