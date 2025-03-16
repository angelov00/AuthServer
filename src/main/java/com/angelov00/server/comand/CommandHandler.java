package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.model.DTO.UserUpdateDTO;
import com.angelov00.server.service.AuthService;

public class CommandHandler {

    private final AuthService authService;

    public CommandHandler(AuthService authService) {
        this.authService = authService;
    }

    public String handleCommand(String command) {
        String[] args = command.split(" ");
        if (args.length == 0) {
            return "No command provided";
        }

        return switch (args[0]) {
            case "register" -> register(args);
            case "login" -> login(args);
            case "update-user" -> updateUser(args);
            case "reset-password" -> resetPassword(args);
            case "logout" -> logout(args);
            case "add-admin-user" -> addAdminUser(args);
            case "remove-admin-user" -> removeAdminUser(args);
            case "delete-user" -> deleteUser(args);
            default -> "Unknown command";
        };
    }

    private String register(String[] args) {
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

        // TODO add validation if fields are missing
        try {
            return authService.register(registerDTO);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String login(String[] args) {
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
                return authService.login(sessionId);
            } else if (username != null && password != null) {
                return authService.login(username, password);
            } else {
                return "Invalid parameters for login";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String logout(String[] args) {
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

    private String updateUser(String[] args) {
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

    private String resetPassword(String[] args) {
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

    private String addAdminUser(String[] args) {
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
            authService.promoteToAdmin(sessionId, username);
            return "User promoted to admin";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String removeAdminUser(String[] args) {
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
            return "Missing parameters for remove-admin-user";
        }
        try {
            authService.demoteToUser(sessionId, username);
            return "Admin rights removed from user";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String deleteUser(String[] args) {
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
            return "Missing parameters for delete-user";
        }
        try {
            authService.deleteUser(sessionId, username);
            return "User deleted";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
