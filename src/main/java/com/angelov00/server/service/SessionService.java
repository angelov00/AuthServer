package com.angelov00.server.service;

import com.angelov00.server.model.entity.Session;
import com.angelov00.server.repository.InMemorySessionRepository;
import com.angelov00.server.repository.UserRepository;

public class SessionService {

    private static final int SESSION_TIME_TO_LIVE = 3600;
    private final InMemorySessionRepository sessionRepository;

    public SessionService(InMemorySessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void invalidateSession(String sessionId) {

    }

    public Session createSession() {
        return null;
    }


}
