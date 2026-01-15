package com.project.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final CsvCache cache;
    private final AuthService authService;
    private final Logger logger;
    private final int timeoutSeconds;

    public ClientHandler(Socket clientSocket,
                         CsvCache cache,
                         AuthService authService,
                         Logger logger,
                         int timeoutSeconds) {
        this.clientSocket = clientSocket;
        this.cache = cache;
        this.authService = authService;
        this.logger = logger;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            clientSocket.setSoTimeout(timeoutSeconds * 1000);

            String input;

            while ((input = reader.readLine()) != null) {

                logger.info("Received from client: " + input);

                // Example command: FETCH_ALL
                if (input.equalsIgnoreCase("FETCH_ALL")) {

                    List<String> rows = cache.getAllRows();   // ✔ now List<String>
                    for (String row : rows) {
                        writer.println(row);
                    }
                    continue;
                }

                // Authentication command example
                if (input.startsWith("AUTH")) {
                    String[] parts = input.split(" ", 3);
                    boolean ok = authService.authenticate(parts[1], parts[2]);
                    writer.println(ok ? "AUTH_OK" : "AUTH_FAIL");
                    continue;
                }

                // Fetch single key
                if (input.startsWith("GET")) {
                    String[] parts = input.split(" ", 2);
                    String value = cache.getValue(parts[1]);
                    writer.println(value != null ? value : "NOT_FOUND");
                    continue;
                }

                writer.println("INVALID_COMMAND");
            }

        } catch (Exception ex) {
            logger.severe("Error in client handler: " + ex.getMessage());
        }
    }
}
