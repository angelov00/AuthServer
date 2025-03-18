package com.angelov00.server.comand;

import com.angelov00.server.service.AuthService;

public class ResetPasswordCommand implements Command {

    private final AuthService authService;

    public ResetPasswordCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args, String clientIP) {
        String sessionId = null;
        String username = null;
        String oldPassword = null;
        String newPassword = null;
        // --session-id <sessionId> --username <username> --old-password <oldPassword> --new-password <newPassword>
        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--session-id":
                    sessionId = args[i + 1];
                    break;
                case "--username":
                    username = args[i + 1];
                    break;
                case "--old-password":
                    oldPassword = args[i + 1];
                    break;
                case "--new-password":
                    newPassword = args[i + 1];
                    break;
                default:
                    break;
            }
        }

        if (sessionId == null || username == null || oldPassword == null || newPassword == null) {
            return "Missing parameters for reset-password";
        }

        try {
            authService.resetPassword(sessionId, username, oldPassword, newPassword);
            return "Password reset successful";
        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
