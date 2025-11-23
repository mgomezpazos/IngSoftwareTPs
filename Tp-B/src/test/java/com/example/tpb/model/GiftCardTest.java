package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

public class GiftCardTest {

    @Test public void test01NewCardsAreCreatedWithoutOwner() {
        assertFalse(createCard(100 ).owned());
    }

    @Test public void test02NewCardsAreCreatedWithCorrectBalance() {
        assertEquals(100, createCard(100).balance());
    }

    @Test public void test03NewCardsAreCreatedWithCorrectId() {
        assertEquals("Giftcard1", new GiftCard("Giftcard1", 100).id());
    }

    @Test public void test04NewCardsHaveNoCharges() {
        assertTrue(createCard(100).charges().isEmpty());
    }

    @Test public void test05CanRedeemAnUnownedCard() {
        GiftCard card=createCard(100).redeem("Manuelita");
        assertTrue(card.owned());
        assertTrue(card.isOwnedBy("Manuelita"));
    }

    @Test public void test06CanNotRedeemAnAlreadyOwnedCard() {
        GiftCard card= createCard(100).redeem("Zoe");
        assertThrowsLike(() -> card.redeem("Manuelita"), GiftCard.InvalidCard);
    }

    @Test public void test07CanNotRedeemCardTwiceWithSameOwner() {
        GiftCard card= createCard(100).redeem("Zoe");
        assertThrowsLike(() -> card.redeem("Zoe"), GiftCard.CardAlreadyRedeemed);
    }

    @Test public void test08CanChargeAnOwnedCard() {
        GiftCard card= createCard(100).redeem("Emilio").charge(30, "Harley Davidson");
        assertEquals(70, card.balance());
    }

    @Test public void test09CanNotChargeAnUnownedCard() {
        assertThrowsLike(() -> createCard(100).charge(30, "F1 Store"), GiftCard.CargoImposible);
    }

    @Test public void test10CanNotChargeMoreThanBalance() {
        GiftCard card= createCard(100).redeem("Emilio");
        assertThrowsLike(() -> card.charge(150, "Moto carísima"), GiftCard.CargoImposible);
    }

    @Test public void test11CanChargeExactBalance() {
        GiftCard card=createCard(100).redeem("Julio" ).charge(100, "todo el balance");
        assertEquals(0, card.balance());
    }

    @Test public void test12ChargeRemembersDescription() {
        GiftCard card=createCard(100).redeem("Manuelita").charge(30, "Nba Store");
        assertEquals(1, card.charges().size());
        assertEquals("Nba Store", card.charges().get(0));
    }

    @Test public void test13MultipleChargesAreRemembered() {
        GiftCard card=createCard(100)
                .redeem("Zoe")
                .charge(30, "Nba Store")
                .charge(20, "F1 Store");

        assertEquals(50, card.balance());
        assertEquals(2, card.charges().size());
        assertEquals("Nba Store", card.charges().get(0));
        assertEquals("F1 Store", card.charges().get(1));
    }

    @Test public void test14AssertIsOwnedByThrowsForUnownedCard() {
        assertThrowsLike(() -> createCard(100).assertIsOwnedBy("Julio"), GiftCard.InvalidCard);
    }

    @Test public void test15AssertIsOwnedByThrowsForDifferentOwner() {
        GiftCard card=createCard(100).redeem("Manuelita");
        assertThrowsLike(() -> card.assertIsOwnedBy("Zoe"), GiftCard.InvalidCard);
    }

    @Test public void test16AssertIsOwnedBySucceedsForCorrectOwner() {
        GiftCard card=createCard(100).redeem("Zoe");
        assertDoesNotThrow(() -> card.assertIsOwnedBy("Zoe"));
    }

    @Test public void test17IsOwnedByReturnsFalseForUnownedCard() {
        assertFalse(createCard(100).isOwnedBy("Manuelita"));
    }

    @Test public void test18IsOwnedByReturnsFalseForDifferentOwner() {
        GiftCard card= createCard( 100 ).redeem( "Manuelita" );
        assertFalse(card.isOwnedBy("Zoe"));
    }

    @Test public void test19IsOwnedByReturnsTrueForCorrectOwner() {
        GiftCard card= createCard(100).redeem("Emilio");
        assertTrue(card.isOwnedBy("Emilio"));
    }

    @Test public void test20CanCreateCardWithExistingData() {
        GiftCard card=new GiftCard("Giftcard1", 70, "Manuelita", List.of( "Nba store", "F1 store" ) );
        assertEquals("Giftcard1", card.id());
        assertEquals(70, card.balance());
        assertTrue(card.isOwnedBy("Manuelita"));
        assertEquals(2, card.charges().size());
    }

    @Test public void test21ChargesListIsDefensiveCopy() {
        GiftCard card = createCard(100).redeem( "Emilio").charge( 30, "Harley Davidson");
        List<String> charges = card.charges();
        charges.add("Hacked charge");
        assertEquals(1, card.charges().size());
    }

    @Test public void test22ConstructorWithChargesUsesDefensiveCopy() {
        List<String> originalCharges=List.of("Nba Store");
        GiftCard card=new GiftCard("Giftcard1", 70, "Manuelita", originalCharges);
        assertEquals(1, card.charges().size());
    }

    @Test public void test23CanNotCreateCardWithNegativeBalance() {
        assertThrowsLike(() -> new GiftCard("Giftcard1", -100), GiftCard.InvalidBalance);
    }

    @Test public void test24CanNotCreateCardWithZeroBalance() {
        assertThrowsLike(() -> new GiftCard("Giftcard1", 0), GiftCard.InvalidBalance);
    }

    @Test public void test25CanNotChargeZeroAmount() {
        GiftCard card= createCard(100).redeem("Emilio");
        assertThrowsLike(() -> card.charge(0, "Gratis?"), GiftCard.CargoImposible);
    }

    @Test public void test26CanNotChargeNegativeAmount() {
        GiftCard card = createCard(100).redeem("Emilio");
        assertThrowsLike(() -> card.charge(-50, "Intentando agregar plata"), GiftCard.CargoImposible);
    }

    @Test public void test27CanNotRedeemWithNullOwner() {
        assertThrowsLike(() -> createCard(100).redeem(null), GiftCard.InvalidOwner);
    }

    @Test public void test28CanNotRedeemWithEmptyOwner() {
        assertThrowsLike(() -> createCard(100).redeem(""), GiftCard.InvalidOwner);
    }

    @Test public void test29CanNotChargeWithNullDescription() {
        GiftCard card= createCard(100).redeem("Emilio");
        assertThrowsLike(() -> card.charge(50, null), GiftCard.InvalidDescription);
    }

    @Test public void test30EmptyDescriptionIsAllowed() {
        GiftCard card = createCard(100).redeem("Emilio").charge(50, "");
        assertEquals(1, card.charges().size());
        assertEquals("", card.charges().get(0));
    }

    @Test public void test31CanNotCreateCardWithNullId() {
        assertThrowsLike(() -> new GiftCard(null, 100), GiftCard.InvalidCard);
    }

    @Test public void test32CanNotCreateCardWithEmptyId() {
        assertThrowsLike(() -> new GiftCard("", 100), GiftCard.InvalidCard);
    }


    private static GiftCard createCard(int balance) {
        return new GiftCard("Giftcard1", balance);
    }

    private void assertThrowsLike(Executable executable, String message) {
        assertEquals( message, assertThrows( Exception.class, executable).getMessage());
    }
}