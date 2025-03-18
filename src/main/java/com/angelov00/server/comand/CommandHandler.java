package com.angelov00.server.comand;

import com.angelov00.server.service.AuthService;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private final Map<String, Command> commands;

    public CommandHandler(AuthService authService) {
        this.commands= new HashMap<>();
        commands.put("register", new RegisterCommand(authService));
        commands.put("login", new LoginCommand(authService));
        commands.put("logout", new LogoutCommand(authService));
        commands.put("update-user", new UpdateUserCommand(authService));
        commands.put("reset-password", new ResetPasswordCommand(authService));
        commands.put("add-admin-user", new AddAdminUserCommand(authService));
        commands.put("remove-admin-user", new RemoveAdminUserCommand(authService));
        commands.put("delete-user", new DeleteUserCommand(authService));
    }

    public String handleCommand(String command, String clientIP) {
        String[] args = command.split(" ");
        if (args.length == 0) {
            return "No command provided";
        }

        Command cmd = commands.get(args[0]);
        if (cmd == null) {
            return "Unknown command";
        }

        return cmd.execute(args, clientIP);
    }
}
