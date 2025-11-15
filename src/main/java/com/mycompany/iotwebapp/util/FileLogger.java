package com.mycompany.iotwebapp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file logger for debugging threshold update issues.
 * Logs to: logs/threshold_debug.log
 */
public class FileLogger {
    
    private static final String LOG_FILE = "logs/threshold_debug.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();
    
    static {
        // Ensure logs directory exists
        try {
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Failed to create logs directory: " + e.getMessage());
        }
    }
    
    public static void log(String level, String message) {
        log(level, message, null);
    }
    
    public static void log(String level, String message, Throwable throwable) {
        synchronized (LOCK) {
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                
                String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
                String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
                pw.println(logEntry);
                
                if (throwable != null) {
                    throwable.printStackTrace(pw);
                }
                
                pw.flush();
                
                // Also print to console
                System.out.println(logEntry);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    public static void separator() {
        log("INFO", "========================================");
    }
}

