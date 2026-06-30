package com.tss.FoodApp.util;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.tss.FoodApp.config.AppConfig;

public class AppLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public static void info(String message) {
        String formatted = format("INFO", message);
        writeToFile(formatted);
    }

    public static void warn(String message) {
        String formatted = format("WARN", message);
        System.out.println("  " + message);
        writeToFile(formatted);
    }

    public static void error(String message) {
        String formatted = format("ERROR", message);
        System.out.println("  " + message);
        writeToFile(formatted);
    }

    public static void error(String message, Throwable e) {
        error(message + " | Cause: " + e.getMessage());
    }

    private static String format(String level, String message) {
        return "[" + LocalDateTime.now().format(FORMATTER) + "] [" + level + "] " + message;
    }

    private static void writeToFile(String formattedMessage) {
        try {
            File logDir = new File(AppConfig.LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            FileWriter fw = new FileWriter(AppConfig.LOG_FILE, true);
            fw.write(formattedMessage + "\n");
            fw.close();
        } catch (Exception e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
