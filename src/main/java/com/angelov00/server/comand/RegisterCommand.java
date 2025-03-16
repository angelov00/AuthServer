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
        UserRegisterDTO registerDTO = new UserRegisterDTO();

        // --username <username> --password <password> --first-name <firstName> --last-name <lastName> --email <email>
        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--username":
                    registerDTO.setUsername(args[i + 1]);
                    break;
                case "--password":
                    registerDTO.setPassword(args[i + 1]);
                    break;
                case "--email":
                    registerDTO.setEmail(args[i + 1]);
                    break;
                case "--first-name":
                    registerDTO.setFirstName(args[i + 1]);
                    break;
                case "--last-name":
                    registerDTO.setLastName(args[i + 1]);
                    break;
                default:
                    break;
            }
        }

        try {
            return authService.register(registerDTO);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
