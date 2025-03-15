package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.UserRegisterDTO;
import com.angelov00.server.service.UserService;

public class CommandHandler {

    private final UserService userService;

    public CommandHandler(UserService userService) {
        this.userService = userService;
    }

    public String handleCommand(String command) {

        String[] args = command.split(" ");
        String response = "";

        switch (args[0]) {
            case "register":

                break;

            case "login":
                if(args.length > 3) {

                } else {

                }
                break;
            case "update-user":
                break;
            case "reset-password":
                break;
            case "logout":
                break;
            case "add-admin-user":
                break;
            case "remove-admin-user":
                break;
            case "delete-user":
                break;
            default:
        }

        return response;
    }
}