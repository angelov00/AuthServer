package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.SessionLoginDTO;
import com.angelov00.server.model.DTO.UserLoginDTO;
import com.angelov00.server.service.AuthService;

public class LoginCommand implements Command {

    private final AuthService authService;

    public LoginCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args, String clientIP) {
        String username = null;
        String password = null;
        String sessionId = null;

        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--username":
                    username = args[i + 1];
                    break;
                case "--password":
                    password = args[i + 1];
                    break;
                case "--session-id":
                    sessionId = args[i + 1];
                    break;
                default:
                    break;
            }
        }
        try {
            if (sessionId != null) {
                return authService.login(sessionId, clientIP);
            } else if (username != null && password != null) {
                return authService.login(username, password, clientIP);
            } else {
                return "Invalid parameters for login";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
