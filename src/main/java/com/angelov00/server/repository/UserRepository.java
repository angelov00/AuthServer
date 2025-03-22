package com.angelov00.server.repository;

import com.angelov00.server.model.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository {
    void save(User user);

    void update(User user);

    Optional<User> findByUsername(String username);

    void promoteToAdmin(String username);

    void demoteToUser(String username);

    boolean exists(String username);

    void deleteUser(String username);

    long adminCount();

    int getFailedLoginAttempts(String username);

    boolean isTimeouted(String username);

    void removeTimeout(String username);

    void incrementFailedLoginAttempts(String username);

    void timeoutUser(String username, LocalDateTime localDateTime);
}
