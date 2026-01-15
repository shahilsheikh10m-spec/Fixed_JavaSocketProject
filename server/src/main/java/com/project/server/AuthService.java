package com.project.server;

import java.io.IOException;
import java.util.Properties;

public class AuthService {
    private final Properties users = new Properties();

    public AuthService(Properties userProps) throws IOException {
        this.users.putAll(userProps);
    }

    public boolean authenticate(String username, String password) {
        return password.equals(users.getProperty(username));
    }
}
