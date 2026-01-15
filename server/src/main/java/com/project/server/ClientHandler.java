package com.project.server;

import java.io.*;
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
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            clientSocket.setSoTimeout(timeoutSeconds * 1000);
            logger.info("Client connected: " + clientSocket.getInetAddress());

            // Authentication
            String username = MessageUtil.toString(MessageUtil.readBinary(in));
            String password = MessageUtil.toString(MessageUtil.readBinary(in));

            logger.info("Authentication attempt - Username: " + username);

            boolean authResult = authService.authenticate(username, password);
            MessageUtil.sendBinary(out, MessageUtil.toBytes(authResult ? "AUTH_OK" : "AUTH_FAIL"));

            if (!authResult) {
                logger.warning("Authentication failed for user: " + username);
                return;
            }

            logger.info("Authentication successful for user: " + username);

            // Send CSV data in binary format
            MessageUtil.sendBinary(out, MessageUtil.toBytes("CSV_START"));

            List<String> rows = cache.getAllRows();
            MessageUtil.sendBinary(out, MessageUtil.toBytes(String.valueOf(rows.size())));

            for (String row : rows) {
                MessageUtil.sendBinary(out, MessageUtil.toBytes(row));
            }

            MessageUtil.sendBinary(out, MessageUtil.toBytes("CSV_END"));
            logger.info("Sent " + rows.size() + " CSV rows to client");

            // Handle client queries
            while (true) {
                try {
                    String query = MessageUtil.toString(MessageUtil.readBinary(in));
                    logger.info("Received query: " + query);

                    if ("exit".equalsIgnoreCase(query)) {
                        logger.info("Client requested exit");
                        break;
                    }

                    if ("ISO8583_BAL".equalsIgnoreCase(query)) {
                        // Send ISO 8583 balance enquiry response
                        byte[] isoResponse = createISO8583BalanceResponse();
                        MessageUtil.sendBinary(out, isoResponse);
                        logger.info("Sent ISO 8583 balance response");
                        continue;
                    }

                    // Handle company lookup
                    String result = cache.getValue(query);
                    if (result != null) {
                        MessageUtil.sendBinary(out, MessageUtil.toBytes(result));
                        logger.info("Found company: " + query);
                    } else {
                        MessageUtil.sendBinary(out, MessageUtil.toBytes("NOT_FOUND"));
                        logger.warning("Company not found: " + query);
                    }

                } catch (RuntimeException e) {
                    logger.warning("Client timeout, disconnecting...");
                    break;
                }
            }

        } catch (Exception ex) {
            logger.severe("Error in client handler: " + ex.getMessage());
        } finally {
            try {
                clientSocket.close();
                logger.info("Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private byte[] createISO8583BalanceResponse() {
        // Simple ISO 8583 balance enquiry response (binary format)
        // This is a simplified version for demonstration
        byte[] response = new byte[16];

        // Message type (0200 - Authorization response)
        response[0] = 0x02;
        response[1] = 0x00;

        // Primary account number (PAN) - simplified
        response[2] = 0x12;
        response[3] = 0x34;
        response[4] = 0x56;
        response[5] = 0x78;
        response[6] = (byte) 0x90;
        response[7] = 0x12;
        response[8] = 0x34;
        response[9] = 0x56;

        // Amount (balance) - 12345678 in cents
        response[10] = 0x00;
        response[11] = (byte) 0xBC;
        response[12] = 0x61;
        response[13] = 0x4E;

        // Response code (00 - Approved)
        response[14] = 0x30;
        response[15] = 0x30;

        return response;
    }
}
