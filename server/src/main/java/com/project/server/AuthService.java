package com.project.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AuthService {
    private final Properties users = new Properties();

    public AuthService(String usersFile) throws IOException {
        users.load(new FileInputStream(usersFile));
    }

    public boolean authenticate(String username, String password) {
        return password.equals(users.getProperty(username));
    }
}
