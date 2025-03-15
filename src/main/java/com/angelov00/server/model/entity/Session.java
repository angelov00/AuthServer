package com.angelov00.server.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {

    private String sessionId;

    private User user;

    private LocalDateTime createdAt;

    private long timeToLive;

    private boolean active;

    public Session(User user, long timeToLive) {
        this.sessionId = UUID.randomUUID().toString();
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.timeToLive = timeToLive;
        this.active = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
