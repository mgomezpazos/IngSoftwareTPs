package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

public class UserSessionTest {

    @Test public void test01NewSessionIsActive() {
        UserSession session = createSession();

        assertTrue( session.isActiveAt( LocalDateTime.now() ) );
    }

    @Test public void test02SessionIsActiveWithinDuration() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertTrue( session.isActiveAt( now.plusMinutes( 14 ) ) );
    }

    @Test public void test03SessionIsNotActiveAfterDuration() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertFalse( session.isActiveAt( now.plusMinutes( 16 ) ) );
    }

    @Test public void test04SessionIsActiveExactlyAtExpirationBoundary() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertFalse( session.isActiveAt( now.plusMinutes( 15 ) ) );
    }

    @Test public void test05UserAliveAtReturnsUserWhenActive() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertEquals( "Alice", session.userAliveAt( now.plusMinutes( 5 ) ) );
    }

    @Test public void test06UserAliveAtThrowsWhenExpired() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertThrowsLike( () -> session.userAliveAt( now.plusMinutes( 20 ) ),
                UserSession.SessionExpired );
    }

    @Test public void test07SessionRemembersUser() {
        UserSession session = new UserSession( "Alice", LocalDateTime.now() );

        assertEquals( "Alice", session.user() );
    }

    @Test public void test08SessionRemembersStamp() {
        LocalDateTime stamp = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", stamp );

        assertEquals( stamp, session.stamp() );
    }

    @Test public void test09SessionIsNotActiveBeforeCreation() {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession( "Alice", now );

        assertFalse( session.isActiveAt( now.minusMinutes( 1 ) ) );
    }

    // Helper methods
    private static UserSession createSession() {
        return new UserSession( "TestUser", LocalDateTime.now() );
    }

    private void assertThrowsLike( Executable executable, String message ) {
        assertEquals( message,
                assertThrows( Exception.class, executable )
                        .getMessage() );
    }
}