package com.angelov00.server.repository;

import com.angelov00.server.model.entity.Session;
import com.angelov00.server.model.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionRepository {

    private final ConcurrentHashMap<String, Session> sessions;

    public InMemorySessionRepository() {
        sessions = new ConcurrentHashMap<>();
    }

    public void invalidate(String sessionId) {
        sessions.remove(sessionId);
    }

    public boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void createSession(User user, int timeToLive) {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(user, timeToLive);
        sessions.put(sessionId, session);
    }

    public boolean isValid(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && !session.getCreatedAt().plusSeconds(session.getTimeToLive()).isAfter(LocalDateTime.now())) {
            invalidate(sessionId);
            return false;
        }
        return session != null;
    }
}
