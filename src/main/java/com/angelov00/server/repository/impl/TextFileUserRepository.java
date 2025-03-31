package com.angelov00.server.repository.impl;

import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.util.PasswordEncoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TextFileUserRepository implements UserRepository {

    private static final String FILE_PATH = "users.txt";
    private static final Path USER_FILE = Path.of(FILE_PATH);
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_WRITE = "|";

    public TextFileUserRepository() {
        try {
            if(Files.notExists(USER_FILE)) {
                Files.createFile(USER_FILE);
                User superUser = new User();
                superUser.setId(0L);
                superUser.setUsername("admin");
                superUser.setPassword(PasswordEncoder.hashPassword("admin_password"));
                superUser.setEmail("admin@angelov00.com");
                superUser.setFirstName("super");
                superUser.setLastName("admin");
                superUser.setRole(Role.ADMIN);
                this.save(superUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(User user) {
        List<User> users = readAllUsers();
        if (user.getId() == null) {
            long nextId = users.stream()
                    .map(User::getId)
                    .filter(Objects::nonNull)
                    .max(Long::compare)
                    .orElse(0L) + 1;
            user.setId(nextId);
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE.toFile(), true))) {
            writer.write(userToString(user));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(User user) {
        List<User> users = readAllUsers();
        for (int i = 0, size = users.size(); i < size; i++) {
            if(users.get(i).getUsername().equals(user.getUsername())) {
                users.set(i, user);
                break;
            }
        }
        writeAllUsers(users);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (BufferedReader reader = Files.newBufferedReader(USER_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = stringToUser(line);
                if (user.getUsername().equals(username)) {
                    return Optional.of(user);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void promoteToAdmin(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole(Role.ADMIN);
            update(user);
        }
    }

    @Override
    public void demoteToUser(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole(Role.USER);
            update(user);
        }
    }

    @Override
    public boolean exists(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public void deleteUser(String username) {
        List<User> users = readAllUsers();
        users.removeIf(user -> user.getUsername().equals(username));
        writeAllUsers(users);
    }

    @Override
    public long adminCount() {
        return readAllUsers().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count();
    }

    @Override
    public int getFailedLoginAttempts(String username) {
        Optional<User> optionalUser = findByUsername(username);
        return optionalUser.map(User::getFailedLoggedAttempts).orElse(0);
    }

    @Override
    public boolean isTimeouted(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            LocalDateTime timeout = user.getTimeout();
            if(timeout == null) {
                return false;
            }
            return LocalDateTime.now().isBefore(timeout);
        }
        return false;
    }

    @Override
    public void removeTimeout(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setTimeout(null);
            user.setFailedLoggedAttempts(0);
            update(user);
        }
    }

    @Override
    public void incrementFailedLoginAttempts(String username) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setFailedLoggedAttempts(user.getFailedLoggedAttempts() + 1);
            update(user);
        }
    }

    @Override
    public void timeoutUser(String username, LocalDateTime localDateTime) {
        Optional<User> optionalUser = findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setTimeout(localDateTime);
            update(user);
        }
    }

    private List<User> readAllUsers() {
        List<User> users = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(USER_FILE)) {
            String line;
            while((line = reader.readLine()) != null) {
                User user = stringToUser(line);
                if(user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    private void writeAllUsers(List<User> users) {
        try(BufferedWriter writer = Files.newBufferedWriter(USER_FILE)) {
            for(User user : users) {
                writer.write(userToString(user));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Format: id|username|password|firstName|lastName|email|failedLoggedAttempts|timeout|role
    private String userToString(User user) {
        String timeoutStr = (user.getTimeout() == null) ? "null" : user.getTimeout().toString();
        String idStr = (user.getId() == null) ? "null" : user.getId().toString();
        return idStr + DELIMITER_WRITE +
                user.getUsername() + DELIMITER_WRITE +
                user.getPassword() + DELIMITER_WRITE +
                user.getFirstName() + DELIMITER_WRITE +
                user.getLastName() + DELIMITER_WRITE +
                user.getEmail() + DELIMITER_WRITE +
                user.getFailedLoggedAttempts() + DELIMITER_WRITE +
                timeoutStr + DELIMITER_WRITE +
                (user.getRole() == null ? "null" : user.getRole().toString());
    }

    private User stringToUser(String line) {
        String[] parts = line.split(DELIMITER);
        if(parts.length < 9){
            return null;
        }
        User user = new User();
        // id
        if(!parts[0].equals("null")){
            try {
                user.setId(Long.parseLong(parts[0]));
            } catch (NumberFormatException e) {
                user.setId(null);
            }
        }
        user.setUsername(parts[1]);
        user.setPassword(parts[2]);
        user.setFirstName(parts[3]);
        user.setLastName(parts[4]);
        user.setEmail(parts[5]);
        try {
            user.setFailedLoggedAttempts(Integer.parseInt(parts[6]));
        } catch (NumberFormatException e) {
            user.setFailedLoggedAttempts(0);
        }
        // timeout
        if(!parts[7].equals("null")){
            try {
                user.setTimeout(LocalDateTime.parse(parts[7]));
            } catch (Exception e) {
                user.setTimeout(null);
            }
        } else {
            user.setTimeout(null);
        }
        // role
        if(!parts[8].equals("null")){
            try {
                user.setRole(Role.valueOf(parts[8]));
            } catch (IllegalArgumentException e) {
                user.setRole(null);
            }
        } else {
            user.setRole(null);
        }
        return user;
    }
}
