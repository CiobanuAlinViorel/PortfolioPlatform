package com.example.portfolio.contact.application;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class ContactRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_SECONDS = 3600;

    private final ConcurrentHashMap<String, Deque<Instant>> requestsByIp = new ConcurrentHashMap<>();

    public void checkAndRecord(String ip) {
        Deque<Instant> timestamps = requestsByIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                throw new RateLimitExceededException("Too many messages sent. Please try again later.");
            }
            timestamps.addLast(Instant.now());
        }
    }
}
