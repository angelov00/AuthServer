package com.angelov00.server.service;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.model.DTO.UserUpdateDTO;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.SessionRepository;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.util.PasswordEncoder;
import com.angelov00.server.util.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private static final int SESSION_TIME_TO_LIVE = 3600; // seconds
    private static final String LOG_FILE_PATH = "C:\\Users\\Martin\\Desktop\\log.txt";

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final Logger logger;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) throws IOException {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.logger = new Logger(LOG_FILE_PATH);
    }

    public String register(UserRegisterDTO userRegisterDTO) {
        if (userRepository.exists(userRegisterDTO.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(PasswordEncoder.hashPassword(userRegisterDTO.getPassword()));
        user.setEmail(userRegisterDTO.getEmail());
        user.setFirstName(userRegisterDTO.getFirstName());
        user.setLastName(userRegisterDTO.getLastName());
        user.setRole(Role.USER);

        this.userRepository.save(user);
        return createSession(user);
    }

    public String update(UserUpdateDTO userUpdateDTO) {
        User user = validateSession(userUpdateDTO.getSessionId());

        Optional.ofNullable(userUpdateDTO.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(userUpdateDTO.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(userUpdateDTO.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(userUpdateDTO.getEmail()).ifPresent(user::setEmail);

        userRepository.update(user);
        return "User updated";
    }

    public String login(String username, String password, String clientIP) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username does not exist"));

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            String logEntry = String.format("[%s] EVENT: failed login | USER: %s | IP: %s",
                    LocalDateTime.now(), username, clientIP);
            this.logger.log(logEntry);
            throw new IllegalArgumentException("Wrong password");
        }

        return createSession(user);
    }

    public String login(String sessionId, String clientIP) {
        if (!this.isValidSession(sessionId)) {
            throw new IllegalArgumentException("Invalid sessionId!");
        }

        return sessionId;
    }


    public boolean logout(String sessionId) {
        if (!isValidSession(sessionId)) {
            return false;
        }
        invalidateSession(sessionId);
        return true;
    }

    public void resetPassword(String sessionId, String username, String oldPass, String newPass) {
        User user = validateSession(sessionId);

        if (!user.getUsername().equals(username)) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (!PasswordEncoder.matches(oldPass, user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        user.setPassword(PasswordEncoder.hashPassword(newPass));
        userRepository.update(user);
    }

    public void promoteToAdmin(String sessionId, String username, String clientIP) throws IOException {
        validateAdmin(sessionId);
        userRepository.promoteToAdmin(username);

        String logEntry = String.format("[%s] EVENT: role change | ADMIN: %s | IP: %s | ACTION: promote | USER: %s",
                LocalDateTime.now().toString(), this.sessionRepository.getUserBySessionId(sessionId).getUsername(), clientIP, username);
        this.logger.log(logEntry);
    }

    public void demoteToUser(String sessionId, String username, String clientIP) throws IOException {
        validateAdmin(sessionId);
        userRepository.demoteToUser(username);
        String logEntry = String.format("[%s] EVENT: role change | ADMIN: %s | IP: %s | ACTION: demote | USER: %s",
                LocalDateTime.now().toString(), this.sessionRepository.getUserBySessionId(sessionId).getUsername(), clientIP, username);
        this.logger.log(logEntry);
    }

    public void deleteUser(String sessionId, String username) {
        validateAdmin(sessionId);
        invalidateSession(sessionId);
        userRepository.deleteUser(username);
    }

    private void invalidateSession(String sessionId) {
        sessionRepository.invalidate(sessionId);
    }

    private String createSession(User user) {
        return sessionRepository.createSession(user, SESSION_TIME_TO_LIVE);
    }

    private boolean isValidSession(String sessionId) {
        return sessionRepository.isValid(sessionId);
    }

    private User validateSession(String sessionId) {
        return Optional.ofNullable(sessionRepository.getUserBySessionId(sessionId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId!"));
    }

    private void validateAdmin(String sessionId) {
        User user = validateSession(sessionId);
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Permission denied!");
        }
    }

    private String generateOperationId() {
        return UUID.randomUUID().toString();
    }
}
