package com.tss.FoodApp.util;

import com.tss.FoodApp.config.AppConfig;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logger that writes to both console and a log file.
 * Why custom logger not java.util.logging? → Simpler to understand, no XML config needed.
 * Why write to file? → Logs persist after app closes. Useful for debugging.
 * Why also print to console? → Only ERROR and WARN shown on console so user sees critical issues.
 */
public class AppLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {} // Static utility class

    /**
     * Log an INFO message (file only, not shown on console).
     */
    public static void info(String message) {
        writeToFile("INFO", message);
    }

    /**
     * Log a WARN message (file + console).
     */
    public static void warn(String message) {
        String formatted = format("WARN", message);
        System.out.println("  " + message);
        writeToFile(formatted);
    }

    /**
     * Log an ERROR message (file + console).
     */
    public static void error(String message) {
        String formatted = format("ERROR", message);
        System.out.println("  " + message);
        writeToFile(formatted);
    }

    /**
     * Log an ERROR with exception stack trace.
     */
    public static void error(String message, Throwable e) {
        error(message + " | Cause: " + e.getMessage());
    }

    private static String format(String level, String message) {
        return "[" + LocalDateTime.now().format(FORMATTER) + "] [" + level + "] " + message;
    }

    private static void writeToFile(String level, String message) {
        writeToFile(format(level, message));
    }

    private static void writeToFile(String formattedMessage) {
        try {
            File logDir = new File(AppConfig.LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            try (FileWriter fw = new FileWriter(AppConfig.LOG_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(formattedMessage);
                bw.newLine();
            }
        } catch (IOException e) {
            // Can't log the logging error — just print to console
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}