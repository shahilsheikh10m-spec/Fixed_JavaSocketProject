package com.project.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MessageUtil {

    public static void sendBinary(DataOutputStream out, byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public static byte[] readBinary(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }

    public static byte[] toBytes(String msg) {
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    public static String toString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }
}
