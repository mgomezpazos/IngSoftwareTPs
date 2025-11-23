package com.example.tpb.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

//Los ponemos por las dudas! esto se testeó en el tp1 :)

public class UserSessionTest {

    @Test public void test01NewSessionIsActive() {
        UserSession session = createSession();
        assertTrue(session.isActiveAt( LocalDateTime.now()));
    }

    @Test public void test02SessionIsActiveWithinDuration() {
        LocalDateTime now= LocalDateTime.now();
        UserSession session= new UserSession( "Manu", now);
        assertTrue(session.isActiveAt(now.plusMinutes(14)));
    }

    @Test public void test03SessionIsNotActiveAfterDuration() {
        LocalDateTime now= LocalDateTime.now();
        UserSession session= new UserSession("Zoe", now);
        assertFalse(session.isActiveAt(now.plusMinutes(16)));
    }

    @Test public void test04SessionIsActiveExactlyAtExpirationBoundary() {
        LocalDateTime now= LocalDateTime.now();
        UserSession session=new UserSession("Emilio", now);
        assertFalse(session.isActiveAt(now.plusMinutes(15)));
    }

    @Test public void test05UserAliveAtReturnsUserWhenActive() {
        LocalDateTime now=LocalDateTime.now();
        UserSession session= new UserSession("Julio", now);
        assertEquals("Julio", session.userAliveAt(now.plusMinutes(5)));
    }

    @Test public void test06UserAliveAtThrowsWhenExpired() {
        LocalDateTime now= LocalDateTime.now();
        UserSession session=new UserSession("Zoe", now);
        assertThrowsLike(() -> session.userAliveAt(now.plusMinutes(20)), UserSession.SessionExpired);
    }

    @Test public void test07SessionRemembersUser() {
        UserSession session= new UserSession("Manu", LocalDateTime.now());
        assertEquals("Manu", session.user());
    }

    @Test public void test08SessionRemembersStamp() {
        LocalDateTime stamp= LocalDateTime.now();
        UserSession session= new UserSession("Zoe", stamp);
        assertEquals(stamp, session.stamp());
    }

    @Test public void test09SessionIsNotActiveBeforeCreation() {
        LocalDateTime now= LocalDateTime.now();
        UserSession session= new UserSession("Emilio", now);
        assertFalse(session.isActiveAt(now.minusMinutes(1)));
    }

    private static UserSession createSession() {
        return new UserSession("Usuario test", LocalDateTime.now() );
    }

    private void assertThrowsLike( Executable executable, String message ) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }
}