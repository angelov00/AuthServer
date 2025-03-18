package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.UserUpdateDTO;
import com.angelov00.server.service.AuthService;

public class UpdateUserCommand implements Command {

    private final AuthService authService;

    public UpdateUserCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args, String clientIP) {
        String sessionId = null;
        String newUsername = null;
        String newFirstName = null;
        String newLastName = null;
        String newEmail = null;

        // --session-id <sessionId> и опционално: --new-username, --new-first-name, --new-last-name, --new-email
        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--session-id":
                    sessionId = args[i + 1];
                    break;
                case "--new-username":
                    newUsername = args[i + 1];
                    break;
                case "--new-first-name":
                    newFirstName = args[i + 1];
                    break;
                case "--new-last-name":
                    newLastName = args[i + 1];
                    break;
                case "--new-email":
                    newEmail = args[i + 1];
                    break;
                default:
                    break;
            }
        }

        if (sessionId == null) {
            return "Session ID is required for update-user";
        }

        try {
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setSessionId(sessionId);
            updateDTO.setUsername(newUsername);
            updateDTO.setFirstName(newFirstName);
            updateDTO.setLastName(newLastName);
            updateDTO.setEmail(newEmail);
            return authService.update(updateDTO);
        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
