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

        switch (args[0]) {
            case "register":
                UserRegisterDTO userRegisterDTO = new UserRegisterDTO();

                for (int i = 1; i < args.length; i += 2) {
                    switch (args[i]) {
                        case "--username":
                            userRegisterDTO.setUsername(args[i + 1]);
                            break;
                        case "--password":
                            userRegisterDTO.setPassword(args[i + 1]);
                            break;
                        case "--first-name":
                            userRegisterDTO.setFirstName(args[i + 1]);
                            break;
                        case "--last-name":
                            userRegisterDTO.setLastName(args[i + 1]);
                            break;
                        case "--email":
                            userRegisterDTO.setEmail(args[i + 1]);
                            break;
                    }
                }

                this.userService.register(userRegisterDTO);
                break;

            case "login":
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

        return "response";
    }
}