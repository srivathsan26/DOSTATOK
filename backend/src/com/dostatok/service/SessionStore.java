package com.dostatok.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory, expiring server-side browser sessions. */
public final class SessionStore {
    public static final String COOKIE_NAME = "DOSTATOK_SESSION";
    private static final long SESSION_SECONDS = 8 * 60 * 60;
    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public record Session(int userId, Instant expiresAt) {}

    public String create(int userId) {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
        byte[] token = new byte[32];
        random.nextBytes(token);
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        sessions.put(id, new Session(userId, now.plusSeconds(SESSION_SECONDS)));
        return id;
    }

    public Integer resolve(String id) {
        if (id == null) return null;
        Session session = sessions.get(id);
        if (session == null) return null;
        if (Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(id, session);
            return null;
        }
        return session.userId();
    }

    public void invalidate(String id) {
        if (id != null) sessions.remove(id);
    }

    public long maxAgeSeconds() { return SESSION_SECONDS; }
}
