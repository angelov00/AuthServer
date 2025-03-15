package com.angelov00.server.service;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.util.PasswordEncoder;
import com.google.gson.Gson;

public class UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    //private final Gson gson;

    public UserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        //this.gson = new Gson();
    }

    public String register(UserRegisterDTO userRegisterDTO) {

        if(this.userRepository.exists(userRegisterDTO.getUsername())) {
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

        return this.sessionService.createSession(user);
    }

    public String login(String username, String password) {

        if(!this.userRepository.exists(username)) {
            throw new IllegalArgumentException("Username does not exist");
        }

        User user = this.userRepository.findByUsername(username);

        if(!PasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        return this.sessionService.createSession(user);
    }

    public String login(String sessionId) {

        if(!this.sessionService.isValidSession(sessionId)) {
            throw new IllegalArgumentException("Invalid sessionId!");
        };

        return sessionId;
    }

    public void logout(String sessionId) {
        this.sessionService.invalidateSession(sessionId);
    }

}
