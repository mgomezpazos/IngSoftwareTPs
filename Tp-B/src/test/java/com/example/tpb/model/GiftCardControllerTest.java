package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach public void beforeEach() {when(clock.now()).then(it -> LocalDateTime.now());}

    @Test public void test01CanLoginWithValidCredentials() throws Exception {
        UserEntity user = savedUser();
        String token = loginUser(user.getUsername(), user.getPassword());

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test public void test02CanNotLoginWithInvalidUser() throws Exception {
        loginUserFailing("ManuelitaNotValid", "pass");
    }

    @Test public void test03CanNotLoginWithInvalidPassword() throws Exception {
        UserEntity user = savedUser();
        loginUserFailing(user.getUsername(), "contraseñaEquivocadaBurro");
    }

    @Test public void test04CanRedeemCardAfterLogin() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        int balance = getBalance(token, cardId);
        assertEquals(100, balance);
    }

    @Test public void test05CanNotRedeemWithoutToken() throws Exception {
        String cardId = savedCard(100);
        redeemCardFailing(null, cardId);
    }

    @Test public void test06CanNotRedeemWithInvalidToken() throws Exception {
        savedUser();
        String cardId = savedCard(100);
        redeemCardFailing("invalidToken", cardId);
    }

    @Test public void test07CanNotRedeemInvalidCard() throws Exception {
        UserEntity user= savedUser();
        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCardFailing(token, "tarjetaTrucha");
    }

    @Test public void test08CanNotRedeemAlreadyOwnedCard() throws Exception {
        UserEntity user1= savedUser();
        UserEntity user2= savedUser();
        String cardId= savedCard( 100 );

        String token1= loginUser(user1.getUsername(), user1.getPassword());
        redeemCard(token1, cardId);

        String token2= loginUser(user2.getUsername(), user2.getPassword());
        redeemCardFailing(token2, cardId);
    }

    @Test public void test09CanGetBalanceOfOwnedCard() throws Exception {
        UserEntity user= savedUser();
        String cardId= savedCard(100);
        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        int balance=getBalance(token, cardId);
        assertEquals(100, balance);
    }

    @Test public void test10CanNotGetBalanceWithoutToken() throws Exception {
        String cardId= savedCard(100);
        getBalanceFailing(null, cardId);
    }

    @Test public void test11CanNotGetBalanceOfUnownedCard() throws Exception {
        UserEntity user= savedUser();
        String cardId = savedCard(100);
        String token= loginUser(user.getUsername(), user.getPassword());
        getBalanceFailing(token, cardId);
    }

    @Test public void test12CanNotGetBalanceOfCardOwnedByOther() throws Exception {
        UserEntity user1=savedUser();
        UserEntity user2= savedUser();
        String cardId= savedCard(100);

        String token1=loginUser(user1.getUsername(), user1.getPassword());
        redeemCard(token1, cardId);

        String token2= loginUser(user2.getUsername(), user2.getPassword());
        getBalanceFailing(token2, cardId);
    }

    @Test public void test13CanChargeCardWithValidMerchant() throws Exception {
        UserEntity user= savedUser();
        MerchantEntity merchant= savedMerchant();
        String cardId= savedCard(100);

        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        chargeCard(merchant.getCode(), cardId, 30, "Camiseta de la nba" );

        assertEquals( 70, getBalance( token, cardId ) );
    }

    @Test public void test14CanNotChargeWithInvalidMerchant() throws Exception {
        UserEntity user= savedUser();
        String cardId= savedCard( 100);

        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        chargeCardFailing("merchantTrucho", cardId, 30, "Camiseta de la nba");
    }

    @Test public void test15CanNotChargeUnownedCard() throws Exception {
        MerchantEntity merchant= savedMerchant();
        String cardId= savedCard( 100 );
        chargeCardFailing( merchant.getCode(), cardId, 30, "Camiseta de la nba");
    }

    @Test public void test16CanNotChargeMoreThanBalance() throws Exception {
        UserEntity user= savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId= savedCard( 100 );

        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        chargeCardFailing(merchant.getCode(), cardId, 150, "moto muy cara :(");
    }

    @Test public void test17CanGetDetailsOfOwnedCard() throws Exception {
        UserEntity user= savedUser();
        MerchantEntity merchant= savedMerchant();
        String cardId= savedCard(100);

        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId );
        chargeCard(merchant.getCode(), cardId, 30, "NBA STORE");
        chargeCard(merchant.getCode(), cardId, 20, "F1 STORE");

        List<String> charges= getDetails(token, cardId);

        assertEquals(2, charges.size());
        assertEquals("NBA STORE", charges.get(0));
        assertEquals("F1 STORE", charges.get(1));
    }

    @Test public void test18CanNotGetDetailsWithoutToken() throws Exception {
        String cardId= savedCard(100);
        getDetailsFailing(null, cardId);
    }

    @Test public void test19CanNotGetDetailsOfUnownedCard() throws Exception {
        UserEntity user= savedUser();
        String cardId= savedCard(100);
        String token= loginUser(user.getUsername(), user.getPassword());
        getDetailsFailing(token, cardId);
    }

    @Test public void test20MultipleChargesUpdateBalanceCorrectly() throws Exception {
        UserEntity user= savedUser();
        MerchantEntity merchant= savedMerchant();
        String cardId= savedCard(100);

        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId );

        chargeCard(merchant.getCode(), cardId, 30, "NBA STORe");
        chargeCard(merchant.getCode(), cardId, 20, "F1 Store");
        chargeCard(merchant.getCode(), cardId, 10, "Harley Davidson") ;

        assertEquals(40, getBalance(token, cardId));
        assertEquals(3, getDetails(token, cardId).size());
    }

    @Test public void test21CanNotRedeemWhenSessionIsExpired() throws Exception {
        UserEntity user= savedUser();
        String cardId= savedCard(100);
        String token= loginUser(user.getUsername(), user.getPassword());

        when(clock.now()).then( it -> LocalDateTime.now().plusMinutes(20));
        redeemCardFailing(token, cardId);
    }

    @Test public void test22CanNotGetBalanceWhenSessionIsExpired() throws Exception {
        UserEntity user= savedUser();
        String cardId=savedCard( 100 );
        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        when(clock.now()).then( it -> LocalDateTime.now().plusMinutes(20));
        getBalanceFailing(token, cardId);
    }

    @Test public void test23CanNotGetDetailsWhenSessionIsExpired() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard( 100 );
        String token = loginUser( user.getUsername(), user.getPassword() );
        redeemCard( token, cardId );
        when( clock.now() ).then( it -> LocalDateTime.now().plusMinutes( 20 ) );
        getDetailsFailing( token, cardId );
    }

    @Test public void test24MultipleCardsDoNotInterfere() throws Exception {
        UserEntity user= savedUser();
        String card1= savedCard(100);
        String card2= savedCard(200);
        String token= loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, card1);
        redeemCard(token, card2);
        assertEquals(100, getBalance(token, card1));
        assertEquals(200, getBalance(token, card2));
    }

    @Test public void test25CanLoginMultipleTimesAndGetDifferentTokens() throws Exception {
        UserEntity user = savedUser();
        String token1 = loginUser(user.getUsername(), user.getPassword());
        String token2 = loginUser(user.getUsername(), user.getPassword());
        assertNotEquals(token1, token2);
    }

    @Test public void test26LoginReturnsValidJSONFormat() throws Exception {
     UserEntity user = savedUser();
     String token = loginUser(user.getUsername(), user.getPassword());
     assertNotNull(token);
     assertFalse(token.isEmpty());
    }

    @Test public void test27BalanceReturnsValidJSONFormat() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        int balance = getBalance(token, cardId);
        assertEquals(100, balance);
    }

    @Test public void test28DetailsReturnsValidJSONFormat() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard(100);

        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        chargeCard(merchant.getCode(), cardId, 30, "NBA Store");

        List<String> charges = getDetails(token, cardId);

        assertEquals(1, charges.size());
        assertEquals("NBA Store", charges.get(0));
    }

    @Test public void test29CanNotAccessWithMalformedToken() throws Exception {
        String cardId = savedCard(100);
        getBalanceFailing("tokendeforme", cardId);
    }

    @Test public void test30CanNotAccessWithoutBearerPrefix() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        mockMvc.perform(get("/api/giftcards/" + cardId + "/balance")
                        .header("Authorization", token))
                .andExpect(status().is(500));
    }

    @Test public void test31ChargeWithMissingParametersFails() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard(100);

        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant.getCode())
                        .param("amount", "30"))
                .andExpect(status().is(500));
    }

    @Test public void test32ChargeWithInvalidAmountFormatFails() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard(100);

        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);

        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant.getCode())
                        .param("amount", "treinta")
                        .param("description", "NBA Store"))
                .andExpect(status().is(500));
    }

    @Test public void test33LoginWithMissingCredentialsFails() throws Exception {
        mockMvc.perform(post("/api/giftcards/login")
                        .param("user", "algun usuario"))
                .andExpect(status().is(500));
    }

    @Test public void test34RedeemReturnsSuccessStatus() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
    }

    @Test public void test35ChargeReturnsSuccessStatus() throws Exception {
        UserEntity user = savedUser();
        MerchantEntity merchant = savedMerchant();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
        chargeCard(merchant.getCode(), cardId, 30, "NBA Store");
    }

    @Test public void test36InvalidEndpointReturns500() throws Exception {
        mockMvc.perform(get("/api/giftcards/nonexistent")).andExpect(status().is(500));
    }

    @Test public void test37TokenIsValidImmediatelyAfterLogin() throws Exception {
        UserEntity user = savedUser();
        String cardId = savedCard(100);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, cardId);
    }

    @Test public void test38CanAccessDifferentCardsWithSameToken() throws Exception {
        UserEntity user = savedUser();
        String card1 = savedCard(100);
        String card2 = savedCard(200);
        String token = loginUser(user.getUsername(), user.getPassword());
        redeemCard(token, card1);
        redeemCard(token, card2);
        assertEquals(100, getBalance(token, card1));
        assertEquals(200, getBalance(token, card2));
    }

    @Test public void test39HTTPMethodsReturnError() throws Exception {
        mockMvc.perform(get("/api/giftcards/login")
                        .param("user", "test")
                        .param("pass", "test"))
                .andExpect(status().is(500));
    }


    private UserEntity savedUser() {return userService.save(EntityDrawer.someUser());}

    private MerchantEntity savedMerchant() {return merchantService.save( EntityDrawer.someMerchant() );}

    private String savedCard(int balance) {
        String cardId= EntityDrawer.someCardId();
        giftCardService.save(new GiftCard(cardId, balance));
        return cardId;
    }

    private String loginUser( String username, String password ) throws Exception {
        String response = mockMvc.perform( post("/api/giftcards/login")
                        .param("user", username)
                        .param("pass", password))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> map= new ObjectMapper().readValue(response, Map.class);
        return map.get("token");
    }

    private void loginUserFailing(String username, String password) throws Exception {
        mockMvc.perform(post("/api/giftcards/login" )
                        .param("user", username)
                        .param("pass", password))
                .andDo(print())
                .andExpect(status().is( 500 ));
    }

    private void redeemCard(String token, String cardId) throws Exception {
        mockMvc.perform(post( "/api/giftcards/"+ cardId+ "/redeem")
                        .header("Authorization", "Bearer "+ token))
                .andDo(print())
                .andExpect(status().is(200));
    }

    private void redeemCardFailing(String token, String cardId) throws Exception {
        if (token == null) {
            mockMvc.perform(post("/api/giftcards/" + cardId+ "/redeem"))
                    .andDo(print())
                    .andExpect(status().is(500));
        } else {
            mockMvc.perform(post( "/api/giftcards/" +cardId+ "/redeem")
                            .header( "Authorization", "Bearer "+ token))
                    .andDo(print())
                    .andExpect(status().is(500));
        }
    }

    private int getBalance(String token, String cardId) throws Exception {
        String response= mockMvc.perform(get("/api/giftcards/"+cardId+"/balance")
                        .header("Authorization", "Bearer "+token))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Integer> map= new ObjectMapper().readValue( response, Map.class);
        return map.get("balance");
    }

    private void getBalanceFailing( String token, String cardId ) throws Exception {
        if (token==null) {
            mockMvc.perform(get("/api/giftcards/"+cardId+"/balance"))
                    .andDo(print())
                    .andExpect(status().is(500));
        } else {
            mockMvc.perform(get( "/api/giftcards/"+ cardId+ "/balance")
                            .header("Authorization", "Bearer "+ token))
                    .andDo(print())
                    .andExpect(status().is(500));
        }
    }

    private void chargeCard(String merchant, String cardId, int amount, String description) throws Exception {
        mockMvc.perform(post( "/api/giftcards/"+cardId+"/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", description))
                .andDo(print())
                .andExpect(status().is(200));
    }

    private void chargeCardFailing( String merchant, String cardId, int amount, String description) throws Exception {
        mockMvc.perform(post("/api/giftcards/" +cardId+"/charge")
                        .param("merchant", merchant )
                        .param("amount", String.valueOf(amount))
                        .param("description", description))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private List<String> getDetails( String token, String cardId ) throws Exception {
        String response= mockMvc.perform(get("/api/giftcards/"+ cardId + "/details")
                        .header("Authorization", "Bearer "+ token))
                .andDo(print())
                .andExpect( status().is(200 ))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, List<String>> map=new ObjectMapper().readValue(response, Map.class);
        return map.get("charges") ;
    }

    private void getDetailsFailing(String token, String cardId) throws Exception {
        if (token== null ) {
            mockMvc.perform(get("/api/giftcards/" +cardId+ "/details") )
                    .andDo(print())
                    .andExpect(status().is(500));
        } else {
            mockMvc.perform(get("/api/giftcards/" +cardId+ "/details")
                            .header("Authorization", "Bearer " +token))
                    .andDo(print())
                    .andExpect(status().is( 500));
        }
    }
}