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

            // Get current thresholds/settings from database
            Map<String, Double> thresholds = service.getThresholdMapForESP32(deviceId);

            // Get pending commands for the device
            List<DeviceCommand> pendingCommands = service.getPendingCommands(deviceId);

            // Build response in exact format expected by ESP32
            Map<String, Object> response = new HashMap<>();
            response.put("update", true);
            response.put("settings", thresholds);

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
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"update\":false,\"error\":\"Internal server error\"}");
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
