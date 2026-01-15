package com.project.client;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    public static Logger getLogger(String name, String logFile) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);

        try {
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.println("Logging setup failed: " + e.getMessage());
        }
        return logger;
    }
}
