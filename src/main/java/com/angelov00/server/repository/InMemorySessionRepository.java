package com.angelov00.server.repository;

import com.angelov00.server.model.entity.Session;
import com.angelov00.server.model.entity.User;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;

public class InMemorySessionRepository {

    private final ConcurrentHashMap<String, Session> sessions;
    private final ScheduledExecutorService executor;

    public InMemorySessionRepository() {
        sessions = new ConcurrentHashMap<>();
        this.executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleWithFixedDelay(() -> {
            Iterator<String> iterator = sessions.keySet().iterator();
            while (iterator.hasNext()) {
                String sessionId = iterator.next();
                Session session = sessions.get(sessionId);
                if (session != null && isExpired(session)) {
                    iterator.remove();
                }
            }
        }, 30, 5, TimeUnit.MINUTES);
    }

    public void invalidate(String sessionId) {
        sessions.remove(sessionId);
    }

    public boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public String createSession(User user, int timeToLive) {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(user, timeToLive);
        sessions.put(sessionId, session);
        return sessionId;
    }

    public boolean isValid(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && isExpired(session)) {
            invalidate(sessionId);
            return false;
        }
        return session != null;
    }

    private boolean isExpired(Session session) {
        return session.getCreatedAt().plusSeconds(session.getTimeToLive()).isBefore(LocalDateTime.now());
    }
}
