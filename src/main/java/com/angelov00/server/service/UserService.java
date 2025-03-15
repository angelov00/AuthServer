package com.angelov00.server.service;

import com.angelov00.server.model.DTO.RegisterResponse;
import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.model.enums.Role;
import com.angelov00.server.repository.UserRepository;
import com.google.gson.Gson;

public class UserService {

    private final UserRepository userRepository;
    private final Gson gson;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.gson = new Gson();
    }

    public RegisterResponse register(UserRegisterDTO userRegisterDTO) {

        // TODO add validation
        // already existing name

        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        user.setFirstName(userRegisterDTO.getFirstName());
        user.setLastName(userRegisterDTO.getLastName());
        user.setRole(Role.USER);
        this.userRepository.save(user);
        return null;
    }
}
