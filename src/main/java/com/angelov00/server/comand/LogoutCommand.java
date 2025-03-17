package com.angelov00.server.comand;

import com.angelov00.server.service.AuthService;

public class LogoutCommand implements Command {

    private final AuthService authService;

    public LogoutCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args, String clientIP) {
        String sessionId = null;
        // --session-id <sessionId>
        for (int i = 1; i < args.length; i += 2) {
            if ("--session-id".equals(args[i])) {
                sessionId = args[i + 1];
            }
        }
        if (sessionId == null) {
            return "Session ID is required for logout";
        }
        boolean success = authService.logout(sessionId);
        return success ? "Logout successful!" : "Logout failed!";
    }

}
