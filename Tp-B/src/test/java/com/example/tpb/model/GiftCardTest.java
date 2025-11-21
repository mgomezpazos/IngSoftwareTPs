package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

public class GiftCardTest {

    @Test public void test01NewCardsAreCreatedWithoutOwner() {
        assertFalse( createCard( 100 ).owned() );
    }

    @Test public void test02NewCardsAreCreatedWithCorrectBalance() {
        assertEquals( 100, createCard( 100 ).balance() );
    }

    @Test public void test03NewCardsAreCreatedWithCorrectId() {
        assertEquals( "CARD001", new GiftCard( "CARD001", 100 ).id() );
    }

    @Test public void test04NewCardsHaveNoCharges() {
        assertTrue( createCard( 100 ).charges().isEmpty() );
    }

    @Test public void test05CanRedeemAnUnownedCard() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertTrue( card.owned() );
        assertTrue( card.isOwnedBy( "Alice" ) );
    }

    @Test public void test06CanNotRedeemAnAlreadyOwnedCard() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertThrowsLike( () -> card.redeem( "Bob" ), GiftCard.InvalidCard );
    }

    @Test public void test07CanNotRedeemCardTwiceWithSameOwner() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertThrowsLike( () -> card.redeem( "Alice" ), GiftCard.CardAlreadyRedeemed );
    }

    @Test public void test08CanChargeAnOwnedCard() {
        GiftCard card = createCard( 100 ).redeem( "Alice" ).charge( 30, "Coffee" );

        assertEquals( 70, card.balance() );
    }

    @Test public void test09CanNotChargeAnUnownedCard() {
        assertThrowsLike( () -> createCard( 100 ).charge( 30, "Coffee" ),
                GiftCard.CargoImposible );
    }

    @Test public void test10CanNotChargeMoreThanBalance() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertThrowsLike( () -> card.charge( 150, "Expensive item" ),
                GiftCard.CargoImposible );
    }

    @Test public void test11CanChargeExactBalance() {
        GiftCard card = createCard( 100 ).redeem( "Alice" ).charge( 100, "All balance" );

        assertEquals( 0, card.balance() );
    }

    @Test public void test12ChargeRemembersDescription() {
        GiftCard card = createCard( 100 ).redeem( "Alice" ).charge( 30, "Coffee" );

        assertEquals( 1, card.charges().size() );
        assertEquals( "Coffee", card.charges().get( 0 ) );
    }

    @Test public void test13MultipleChargesAreRemembered() {
        GiftCard card = createCard( 100 )
                .redeem( "Alice" )
                .charge( 30, "Coffee" )
                .charge( 20, "Lunch" );

        assertEquals( 50, card.balance() );
        assertEquals( 2, card.charges().size() );
        assertEquals( "Coffee", card.charges().get( 0 ) );
        assertEquals( "Lunch", card.charges().get( 1 ) );
    }

    @Test public void test14AssertIsOwnedByThrowsForUnownedCard() {
        assertThrowsLike( () -> createCard( 100 ).assertIsOwnedBy( "Alice" ),
                GiftCard.InvalidCard );
    }

    @Test public void test15AssertIsOwnedByThrowsForDifferentOwner() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertThrowsLike( () -> card.assertIsOwnedBy( "Bob" ),
                GiftCard.InvalidCard );
    }

    @Test public void test16AssertIsOwnedBySucceedsForCorrectOwner() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertDoesNotThrow( () -> card.assertIsOwnedBy( "Alice" ) );
    }

    @Test public void test17IsOwnedByReturnsFalseForUnownedCard() {
        assertFalse( createCard( 100 ).isOwnedBy( "Alice" ) );
    }

    @Test public void test18IsOwnedByReturnsFalseForDifferentOwner() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertFalse( card.isOwnedBy( "Bob" ) );
    }

    @Test public void test19IsOwnedByReturnsTrueForCorrectOwner() {
        GiftCard card = createCard( 100 ).redeem( "Alice" );

        assertTrue( card.isOwnedBy( "Alice" ) );
    }

    @Test public void test20CanCreateCardWithExistingData() {
        GiftCard card = new GiftCard( "CARD001", 70, "Alice", List.of( "Coffee", "Lunch" ) );

        assertEquals( "CARD001", card.id() );
        assertEquals( 70, card.balance() );
        assertTrue( card.isOwnedBy( "Alice" ) );
        assertEquals( 2, card.charges().size() );
    }

    @Test public void test21ChargesListIsDefensiveCopy() {
        GiftCard card = createCard( 100 ).redeem( "Alice" ).charge( 30, "Coffee" );

        List<String> charges = card.charges();
        charges.add( "Hacked charge" );

        assertEquals( 1, card.charges().size() );
    }

    @Test public void test22ConstructorWithChargesUsesDefensiveCopy() {
        List<String> originalCharges = List.of( "Coffee" );
        GiftCard card = new GiftCard( "CARD001", 70, "Alice", originalCharges );

        // Modifications to original list should not affect the card
        assertEquals( 1, card.charges().size() );
    }

    // Helper methods
    private static GiftCard createCard( int balance ) {
        return new GiftCard( "CARD001", balance );
    }

    private void assertThrowsLike( Executable executable, String message ) {
        assertEquals( message,
                assertThrows( Exception.class, executable )
                        .getMessage() );
    }
}