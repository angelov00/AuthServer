package com.angelov00.server.repository;

import com.angelov00.server.model.entity.Session;
import com.angelov00.server.model.entity.User;

import java.util.Optional;

public interface SessionRepository {
    void invalidate(String sessionId);

    boolean contains(String sessionId);

    Session createSession(User user, int timeToLive);

    boolean isValid(String sessionId);

    Optional<User> getUserBySessionId(String sessionId);

    boolean isAdmin(String sessionId);
}
