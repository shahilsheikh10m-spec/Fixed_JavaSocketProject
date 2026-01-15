package com.project.client;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

public class ClientMain {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.load(new FileInputStream("C:/Users/Client/OneDrive/Desktop/project/Fixed_JavaSocketProject/client/src/main/resources/client.properties"));

        String host = props.getProperty("server.host");
        int port = Integer.parseInt(props.getProperty("server.port"));
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        String logFile = props.getProperty("log.file");
        int reconnectDelay = Integer.parseInt(props.getProperty("reconnect.delay.seconds"));

        Logger logger = LoggerUtil.getLogger("ClientLogger", logFile);

        while (true) {
            try (Socket socket = new Socket(host, port)) {
                logger.info("Connected to server: " + host + ":" + port);

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                MessageUtil.sendBinary(out, MessageUtil.toBytes(username));
                MessageUtil.sendBinary(out, MessageUtil.toBytes(password));

                String authResp = MessageUtil.toString(MessageUtil.readBinary(in));
                if (!"AUTH_OK".equals(authResp)) {
                    logger.warning("Authentication failed.");
                    System.out.println("Authentication failed!");
                    return;
                }

                System.out.println("Authenticated successfully!");

                String start = MessageUtil.toString(MessageUtil.readBinary(in));
                if (!"CSV_START".equals(start)) {
                    throw new RuntimeException("Invalid CSV start message");
                }

                int totalRows = Integer.parseInt(MessageUtil.toString(MessageUtil.readBinary(in)));
                System.out.println("\n--- CSV DATA RECEIVED (" + totalRows + " rows) ---");

                for (int i = 0; i < totalRows; i++) {
                    String row = MessageUtil.toString(MessageUtil.readBinary(in));
                    System.out.println(row);
                }

                String end = MessageUtil.toString(MessageUtil.readBinary(in));
                if (!"CSV_END".equals(end)) {
                    throw new RuntimeException("Invalid CSV end message");
                }

                System.out.println("\n--- CSV DATA END ---\n");

                Scanner sc = new Scanner(System.in);
                while (true) {
                    System.out.print("Enter company short name (or ISO8583_BAL / exit): ");
                    String query = sc.nextLine();

                    MessageUtil.sendBinary(out, MessageUtil.toBytes(query));

                    byte[] respBytes = MessageUtil.readBinary(in);

                    if ("ISO8583_BAL".equalsIgnoreCase(query)) {
                        System.out.println("ISO8583 Binary Response: " + bytesToHex(respBytes));
                        continue;
                    }

                    String resp = MessageUtil.toString(respBytes);

                    if ("NOT_FOUND".equals(resp)) {
                        System.out.println("Company not found!");
                    } else {
                        System.out.println("Response: " + resp);
                    }

                    if ("exit".equalsIgnoreCase(query)) {
                        break;
                    }
                }

                break;

            } catch (Exception e) {
                logger.warning("Connection lost. Reconnecting in " + reconnectDelay + " seconds...");
                Thread.sleep(reconnectDelay * 1000L);
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
