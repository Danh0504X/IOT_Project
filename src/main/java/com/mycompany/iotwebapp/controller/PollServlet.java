package com.mycompany.iotwebapp.controller;

import com.google.gson.Gson;
import com.mycompany.iotwebapp.model.DeviceCommand;
import com.mycompany.iotwebapp.service.ThresholdService;
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
 * Returns: {"update":true,"settings":{...},"commands":[...]}
 */
@WebServlet(name = "pollServlet", urlPatterns = "/api/poll")
public class PollServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ThresholdService service = new ThresholdService();
    private final transient Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            // Get deviceId from query parameter (e.g., "DEVICE001")
            String deviceId = req.getParameter("deviceId");
            if (deviceId == null || deviceId.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"update\":false,\"error\":\"deviceId parameter is required\"}");
                return;
            }

            System.out.println("[PollServlet] Received GET request for deviceId: " + deviceId);

            // Get current thresholds/settings from database
            Map<String, Double> thresholds = null;
            try {
                System.out.println("[PollServlet] Fetching thresholds for device: " + deviceId);
                thresholds = service.getThresholdMapForESP32(deviceId);
                System.out.println("[PollServlet] Retrieved " + (thresholds != null ? thresholds.size() : 0) + " threshold settings");
                if (thresholds != null && !thresholds.isEmpty()) {
                    for (Map.Entry<String, Double> entry : thresholds.entrySet()) {
                        System.out.println("[PollServlet]   " + entry.getKey() + " = " + entry.getValue());
                    }
                } else {
                    System.out.println("[PollServlet] ⚠ No thresholds found for device: " + deviceId + " (will return empty map)");
                }
            } catch (Exception e) {
                System.err.println("[PollServlet] ✗ Error fetching thresholds: " + e.getMessage());
                e.printStackTrace();
                throw e; // Re-throw to be caught by outer catch
            }

            // Get pending commands for the device (can be empty list if table doesn't exist)
            List<DeviceCommand> pendingCommands = new ArrayList<>();
            try {
                pendingCommands = service.getPendingCommands(deviceId);
                System.out.println("[PollServlet] Retrieved " + pendingCommands.size() + " pending commands");
            } catch (Exception e) {
                // If DeviceCommand table doesn't exist or has issues, just log and continue with empty list
                System.err.println("[PollServlet] ⚠ Error fetching commands (continuing with empty list): " + e.getMessage());
                // Don't throw - just use empty list
            }

            // Build response in exact format expected by ESP32
            Map<String, Object> response = new HashMap<>();
            response.put("update", true);
            // Always provide settings, even if empty
            response.put("settings", thresholds != null ? thresholds : new HashMap<String, Double>());

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

            // Send JSON response
            String jsonResponse = gson.toJson(response);
            System.out.println("[PollServlet] Sending response: " + jsonResponse);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("[PollServlet] ✗ INTERNAL ERROR: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"update\":false,\"error\":\"Internal server error: " + 
                                 e.getMessage().replace("\"", "'") + "\"}");
        }
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
