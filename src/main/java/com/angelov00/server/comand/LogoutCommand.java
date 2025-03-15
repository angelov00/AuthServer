package com.angelov00.server.comand;

import com.angelov00.server.service.UserService;

public class LogoutCommand implements Command {

    private final UserService userService;

    public LogoutCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String execute(String[] args) {
        return this.userService.logout(args[2]);
    }

}
