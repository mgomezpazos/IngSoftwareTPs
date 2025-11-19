package com.example.tpb.model;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class Clock {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}