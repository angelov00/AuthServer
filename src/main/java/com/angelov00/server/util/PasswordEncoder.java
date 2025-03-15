package com.angelov00.server.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    private static final int WORK_FACTOR = 6;

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String plain, String hashedPassword) {
        return BCrypt.checkpw(hashedPassword, plain);
    }
}
