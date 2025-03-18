package com.angelov00.server.repository.impl;

import com.angelov00.server.model.entity.Session;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class SessionRepositoryImpl implements SessionRepository {

    private final ConcurrentHashMap<String, Session> sessions;

    public SessionRepositoryImpl() {
        sessions = new ConcurrentHashMap<>();

        try(ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {
            executor.scheduleWithFixedDelay(() -> {
                for (String sessionId : sessions.keySet()) {
                    Session session = sessions.get(sessionId);
                    if (session != null && isExpired(session)) {
                        sessions.remove(sessionId);
                    }
                }
            }, 30, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public void invalidate(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public boolean contains(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public Session createSession(User user, int timeToLive) {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(user, timeToLive);
        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public boolean isValid(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && isExpired(session)) {
            invalidate(sessionId);
            return false;
        }
        return session != null;
    }

    @Override
    public Optional<User> getUserBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).map(Session::getUser);
    }

    @Override
    public boolean isAdmin(String sessionId) {
        return getUserBySessionId(sessionId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    private boolean isExpired(Session session) {
        LocalDateTime now = LocalDateTime.now();
        return session.getCreatedAt().plusSeconds(session.getTimeToLive()).isBefore(now);
    }

    public void deleteSessionByUsername(String username) {
        sessions.values().removeIf(session -> session.getUser().getUsername().equals(username));
    }
}
