package com.mycompany.iotwebapp.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.iotwebapp.model.DeviceCommand;
import com.mycompany.iotwebapp.model.Threshold;
import com.mycompany.iotwebapp.service.ThresholdService;
import com.mycompany.iotwebapp.dao.ThresholdDAO;
import com.mycompany.iotwebapp.dao.SensorTypeDAO;
import com.mycompany.iotwebapp.model.SensorType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet to handle ESP32 polling requests for threshold updates and commands.
 * Endpoint: GET /api/poll?deviceId=1
 * Returns: {"update":true,"thresholds":{...},"settings":{...},"commands":[...]}
 */
@WebServlet(name = "pollServlet", urlPatterns = "/api/poll")
public class PollServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ThresholdService service = new ThresholdService();
    private final transient ThresholdDAO thresholdDAO = new ThresholdDAO();
    private final transient SensorTypeDAO sensorTypeDAO = new SensorTypeDAO();
    private final transient Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping()
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            // Get deviceId from query parameter (e.g., "1" or "DEVICE001")
            String deviceIdStr = req.getParameter("deviceId");
            if (deviceIdStr == null || deviceIdStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"update\":false,\"error\":\"deviceId parameter is required\"}");
                return;
            }

            // Convert deviceId to Integer (try parsing, or find by name)
            Integer deviceId;
            try {
                deviceId = Integer.parseInt(deviceIdStr);
            } catch (NumberFormatException e) {
                // If deviceId is not a number (e.g., "DEVICE001"), try to find device by name
                System.out.println("[PollServlet] deviceId '" + deviceIdStr + "' is not a number, trying to find by name...");
                com.mycompany.iotwebapp.dao.DeviceInfoDAO deviceDAO = new com.mycompany.iotwebapp.dao.DeviceInfoDAO();
                java.util.List<com.mycompany.iotwebapp.model.DeviceInfo> devices = deviceDAO.findAll();
                com.mycompany.iotwebapp.model.DeviceInfo foundDevice = null;
                for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                    if (deviceIdStr.equals(device.getDeviceName())) {
                        foundDevice = device;
                        break;
                    }
                }
                if (foundDevice != null) {
                    deviceId = foundDevice.getDeviceId();
                    System.out.println("[PollServlet] Found device by name: " + deviceIdStr + " -> DeviceID=" + deviceId);
                } else {
                    // Default to 1 if not found
                    System.out.println("[PollServlet] Device not found by name, using default deviceId=1");
                    deviceId = 1;
                }
            }

            System.out.println("[PollServlet] Received GET request for deviceId: " + deviceId);

            // Get simple threshold map (for backward compatibility)
            Map<String, Double> simpleThresholds = null;
            try {
                System.out.println("[PollServlet] Fetching simple thresholds for device: " + deviceId);
                simpleThresholds = service.getThresholdMapForESP32(deviceId);
                System.out.println("[PollServlet] Retrieved " + (simpleThresholds != null ? simpleThresholds.size() : 0) + " threshold settings");
            } catch (Exception e) {
                System.err.println("[PollServlet] ✗ Error fetching simple thresholds: " + e.getMessage());
                e.printStackTrace();
            }

            // Get multi-level thresholds (for ESP32 multi-level support)
            Map<String, List<Map<String, Object>>> multiLevelThresholds = getMultiLevelThresholds();
            System.out.println("[PollServlet] Retrieved multi-level thresholds for " + multiLevelThresholds.size() + " sensor types");

            // Get pending commands for the device
            List<DeviceCommand> pendingCommands = new ArrayList<>();
            try {
                pendingCommands = service.getPendingCommands(deviceId);
                System.out.println("[PollServlet] Retrieved " + pendingCommands.size() + " pending commands");
            } catch (Exception e) {
                System.err.println("[PollServlet] ⚠ Error fetching commands (continuing with empty list): " + e.getMessage());
            }

            // Build response in format expected by ESP32
            Map<String, Object> response = new HashMap<>();
            response.put("update", true);
            
            // Multi-level thresholds (ESP32 expects this)
            response.put("thresholds", multiLevelThresholds);
            
            // Simple settings (for backward compatibility)
            response.put("settings", simpleThresholds != null ? simpleThresholds : new HashMap<String, Double>());

            // Convert commands to simple format
            List<Map<String, Object>> commandsList = new ArrayList<>();
            for (DeviceCommand command : pendingCommands) {
                Map<String, Object> cmdMap = new HashMap<>();
                cmdMap.put("command_type", command.getCommandType());
                cmdMap.put("command_value", command.getCommandValue());
                cmdMap.put("command_id", command.getCommandId());
                commandsList.add(cmdMap);
            }
            response.put("commands", commandsList);

            // Send JSON response with proper UTF-8 encoding
            String jsonResponse = gson.toJson(response);
            System.out.println("[PollServlet] Sending response (length: " + jsonResponse.length() + " chars)");
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("[PollServlet] ✗ INTERNAL ERROR: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"update\":false,\"error\":\"Internal server error: " + 
                                 e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    /**
     * Get multi-level thresholds for all sensor types.
     * Returns format: {"temperature":[{levelName,minValue,maxValue,alertLevel,message},...], ...}
     */
    private Map<String, List<Map<String, Object>>> getMultiLevelThresholds() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        // Map sensor codes to standard names (matching ESP32 expectations)
        // ESP32 uses: mq2 for CO (MQ-7), mq3 for Gas/LPG (MQ-2), mq1/mq135 for Air Quality (MQ-135)
        Map<String, String> sensorCodeToName = new HashMap<>();
        sensorCodeToName.put("TEMP_DHT11", "temperature");
        sensorCodeToName.put("HUM_DHT11", "humidity");
        sensorCodeToName.put("MQ2", "mq3");      // MQ2 (Gas/LPG) -> mq3 in ESP32
        sensorCodeToName.put("MQ7", "mq2");      // MQ7 (CO) -> mq2 in ESP32  
        sensorCodeToName.put("MQ135", "mq135");  // MQ135 (Air Quality) -> mq135 in ESP32
        sensorCodeToName.put("GP2Y10", "pm25");  // GP2Y10 (PM2.5) -> pm25 in ESP32
        
        try {
            // Get all sensor types
            List<SensorType> sensorTypes = sensorTypeDAO.findAll();
            
            for (SensorType sensorType : sensorTypes) {
                String sensorCode = sensorType.getSensorCode();
                String standardName = sensorCodeToName.get(sensorCode);
                
                if (standardName == null) {
                    continue; // Skip unknown sensor types
                }
                
                // Get all thresholds for this sensor type
                List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorType.getSensorTypeId());
                
                // Convert to ESP32 format
                List<Map<String, Object>> levels = new ArrayList<>();
                for (Threshold threshold : thresholds) {
                    Map<String, Object> level = new HashMap<>();
                    level.put("levelName", threshold.getLevelName());
                    level.put("minValue", threshold.getMinValue());
                    level.put("maxValue", threshold.getMaxValue());
                    level.put("alertLevel", threshold.getAlertLevel());
                    level.put("message", threshold.getMessage());
                    levels.add(level);
                }
                
                if (!levels.isEmpty()) {
                    result.put(standardName, levels);
                    System.out.println("[PollServlet] Added " + levels.size() + " threshold levels for " + standardName);
                } else {
                    System.out.println("[PollServlet] ⚠ No threshold levels found for " + standardName + " (sensorCode: " + sensorCode + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("[PollServlet] Error getting multi-level thresholds: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[PollServlet] Total multi-level thresholds: " + result.size() + " sensor types");
        return result;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            // Handle command acknowledgment from ESP32
            String commandIdParam = req.getParameter("commandId");
            if (commandIdParam != null) {
                Integer commandId = Integer.parseInt(commandIdParam);
                service.markCommandExecuted(commandId);
                resp.getWriter().write("{\"status\":\"ok\",\"message\":\"Command marked as executed\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"commandId parameter is required\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid commandId format\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Internal server error\"}");
        }
    }
}
