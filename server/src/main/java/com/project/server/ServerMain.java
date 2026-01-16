package com.project.server;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerMain {
    public static void main(String[] args) throws Exception {


        Properties props = new Properties();
        InputStream propStream = ServerMain.class.getClassLoader()
                .getResourceAsStream("server.properties");

        if (propStream == null) {
            throw new IllegalStateException("server.properties NOT FOUND in resources folder!");
        }

        props.load(propStream);

        int port = Integer.parseInt(props.getProperty("server.port"));
        int timeout = Integer.parseInt(props.getProperty("client.timeout.seconds"));


        String csvFileName = props.getProperty("csv.file");

        CsvCache cache = new CsvCache(csvFileName);

        InputStream usersStream = ServerMain.class.getClassLoader()
                .getResourceAsStream("users.properties");

        if (usersStream == null) {
            throw new IllegalStateException("users.properties NOT FOUND in resources folder!");
        }

        Properties userProps = new Properties();
        userProps.load(usersStream);
        AuthService authService = new AuthService(userProps);


        Logger logger = LoggerUtil.getLogger("ServerLogger", props.getProperty("log.file"));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler =
                        new ClientHandler(clientSocket, cache, authService, logger, timeout);

                new Thread(handler).start();
            }
        }
    }
}
