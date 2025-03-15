package com.angelov00.server.comand;

import com.angelov00.server.model.enums.Role;
import com.angelov00.server.model.entity.User;
import com.angelov00.server.repository.UserRepository;

public class CommandHandler {

    private final UserRepository userRepository;

    public CommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String command) {

        String[] args = command.split(" ");

        switch (args[0]) {
            case "register":
                User user = new User();

                for (int i = 1; i < args.length; i += 2) {
                    switch (args[i]) {
                        case "--username":
                            user.setUsername(args[i + 1]);
                            break;
                        case "--password":
                            user.setPassword(args[i + 1]);
                            break;
                        case "--first-name":
                            user.setFirstName(args[i + 1]);
                            break;
                        case "--last-name":
                            user.setLastName(args[i + 1]);
                            break;
                        case "--email":
                            user.setEmail(args[i + 1]);
                            break;
                    }
                }
                user.setRole(Role.USER);
                userRepository.save(user);
                break;
        }
    }
}
