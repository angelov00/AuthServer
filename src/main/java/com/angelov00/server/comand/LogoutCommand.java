package com.angelov00.server.comand;

import com.angelov00.server.service.AuthService;

public class LogoutCommand implements Command {

    private final AuthService authService;

    public LogoutCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public String execute(String[] args) {
        this.authService.logout(args[2]);
        return " ";
    }

}
