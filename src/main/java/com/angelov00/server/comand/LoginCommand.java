package com.angelov00.server.comand;

import com.angelov00.server.model.DTO.SessionLoginDTO;
import com.angelov00.server.model.DTO.UserLoginDTO;
import com.angelov00.server.service.UserService;

public class LoginCommand implements Command {

    private final UserService userService;

    public LoginCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String execute(String[] args) {

        SessionLoginDTO sessionLoginDTO = new SessionLoginDTO();
        UserLoginDTO userLoginDTO = new UserLoginDTO();

        if (args.length > 3) {
            for (int i = 1; i < args.length; i += 2) {
                switch (args[i]) {
                    case "--username":
                        userLoginDTO.setUsername(args[i + 1]);
                        break;
                    case "--password":
                        userLoginDTO.setPassword(args[i + 1]);
                        break;
                }
            }
            return this.userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
        } else {
            return this.userService.login(args[2]);
        }
    }
}
