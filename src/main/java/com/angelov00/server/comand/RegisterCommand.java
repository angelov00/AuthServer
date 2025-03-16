package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.service.AuthService;

public class RegisterCommand implements Command {

    private final AuthService authService;

    public RegisterCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args) {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();

        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--username":
                    userRegisterDTO.setUsername(args[i + 1]);
                    break;
                case "--password":
                    userRegisterDTO.setPassword(args[i + 1]);
                    break;
                case "--first-name":
                    userRegisterDTO.setFirstName(args[i + 1]);
                    break;
                case "--last-name":
                    userRegisterDTO.setLastName(args[i + 1]);
                    break;
                case "--email":
                    userRegisterDTO.setEmail(args[i + 1]);
                    break;
            }
        }

        return authService.register(userRegisterDTO);
    }
}
