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

    private static final int FAILED_LOGIN_TIMEOUT = 120;
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

        if(this.userRepository.isTimeouted(username)) {
            throw new IllegalArgumentException("You are in timeout.");
        }

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            logFailedLogin(username, clientIP);
            registerFailedLogin(username);
            throw new IllegalArgumentException("Wrong password");
        }

        if(this.userRepository.isTimeouted(username)) {
            this.userRepository.removeTimeout(username);
        }

        this.sessionRepository.deleteSessionByUsername(username);

        return createSession(user);
    }

    private void registerFailedLogin(String username) {
        this.userRepository.incrementFailedLoginAttempts(username);
        if(this.userRepository.getFailedLoginAttempts(username) > 3) {
            this.userRepository.timeoutUser(username, LocalDateTime.now().plusSeconds(FAILED_LOGIN_TIMEOUT));
        }
    }

    public String login(String sessionId) {
        if (!sessionRepository.isValid(sessionId)) {
            throw new IllegalArgumentException("Invalid sessionId!");
        }

        return sessionId;
    }

    public void logout(String sessionId) {
        if (!sessionRepository.isValid(sessionId)) {
            throw new IllegalArgumentException("Invalid sessionId!");
        }

        sessionRepository.invalidate(sessionId);
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

        String operationId = UUID.randomUUID().toString();
        User admin = getUserFromSession(sessionId);

        String logStart = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (start) | ADMIN: %s | IP: %s | ACTION: promote | TARGET USER: %s",
                LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
        logger.log(logStart);

        try {
            userRepository.promoteToAdmin(username);
            String logFinish = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (finish) | ADMIN: %s | IP: %s | ACTION: promote | TARGET USER: %s | RESULT: success",
                    LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
            logger.log(logFinish);
        } catch (Exception e) {
            String logFinish = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (finish) | ADMIN: %s | IP: %s | ACTION: promote | TARGET USER: %s | RESULT: failure",
                    LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
            logger.log(logFinish);
            throw e;
        }
    }

    public void demoteToUser(String sessionId, String username, String clientIP) throws IOException {
        validateAdmin(sessionId);

        if (this.userRepository.adminCount() < 1) {
            throw new IllegalArgumentException("You cannot remove all admins!");
        }

        String operationId = UUID.randomUUID().toString();
        User admin = getUserFromSession(sessionId);

        String logStart = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (start) | ADMIN: %s | IP: %s | ACTION: demote | TARGET USER: %s",
                LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
        logger.log(logStart);

        try {
            userRepository.demoteToUser(username);
            String logFinish = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (finish) | ADMIN: %s | IP: %s | ACTION: demote | TARGET USER: %s | RESULT: success",
                    LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
            logger.log(logFinish);
        } catch (Exception e) {
            String logFinish = String.format("[%s] OPERATION ID: %s | EVENT: configuration change (finish) | ADMIN: %s | IP: %s | ACTION: demote | TARGET USER: %s | RESULT: failure",
                    LocalDateTime.now(), operationId, admin.getUsername(), clientIP, username);
            logger.log(logFinish);
            throw e;
        }
    }

    public void deleteUser(String sessionId, String username) {
        validateAdmin(sessionId);
        this.sessionRepository.deleteSessionByUsername(username);
        userRepository.deleteUser(username);
    }

    private String createSession(User user) {
        return sessionRepository.createSession(user, SESSION_TIME_TO_LIVE).getSessionId();
    }

    private User validateSession(String sessionId) {
        return sessionRepository.getUserBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sessionId!"));
    }

    private void validateAdmin(String sessionId) {
        User user = validateSession(sessionId);
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Permission denied!");
        }
    }

    private void logFailedLogin(String username, String clientIP) throws IOException {
        String logEntry = String.format("[%s] EVENT: failed login | USER: %s | IP: %s",
                LocalDateTime.now(), username, clientIP);
        this.logger.log(logEntry);
    }

    private User getUserFromSession(String sessionId) {
        return sessionRepository.getUserBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found!"));
    }
}
