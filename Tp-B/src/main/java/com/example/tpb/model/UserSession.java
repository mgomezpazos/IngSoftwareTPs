package com.example.tpb.model;

import java.time.LocalDateTime;

public class UserSession {
    public static final String SessionExpired = "InvalidToken";
    private static final int SESSION_DURATION_MINUTES = 15;

    private String user;
    private LocalDateTime stamp;

    public UserSession( String user, LocalDateTime stamp ) {
        this.user = user;
        this.stamp = stamp;
    }

    public String userAliveAt( LocalDateTime now ) {
        assertIsActive( now );
        return user;
    }

    public boolean isActiveAt( LocalDateTime now ) {
        return now.isBefore( stamp.plusMinutes( SESSION_DURATION_MINUTES ) );
    }

    private void assertIsActive( LocalDateTime now ) {
        if ( !isActiveAt( now ) ) throw new RuntimeException( SessionExpired );
    }

    // accessors
    public String user() {          return user;  }
    public LocalDateTime stamp() {  return stamp; }
}