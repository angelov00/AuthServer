package com.angelov00.server.comand;

import com.angelov00.server.service.AuthService;

public class AddAdminUserCommand implements Command {

    private final AuthService authService;

    public AddAdminUserCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args, String clientIP) {
        String sessionId = null;
        String username = null;
        // --session-id <sessionId> --username <username>
        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--session-id":
                    sessionId = args[i + 1];
                    break;
                case "--username":
                    username = args[i + 1];
                    break;
                default:
                    break;
            }
        }
        if (sessionId == null || username == null) {
            return "Missing parameters for add-admin-user";
        }
        try {
            authService.promoteToAdmin(sessionId, username, clientIP);
            return "User promoted to admin";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
