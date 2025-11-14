package com.mycompany.iotwebapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.iotwebapp.model.ESP32SensorPayload;
import com.mycompany.iotwebapp.service.SensorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Servlet to receive sensor data from ESP32 devices.
 * Endpoint: POST /api/data
 */
@WebServlet(name = "dataServlet", urlPatterns = "/api/data")
public class DataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient SensorService service = new SensorService();
    private final transient Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            // Read JSON payload from ESP32
            String json = req.getReader().lines().collect(Collectors.joining());
            System.out.println("[DataServlet] Received JSON: " + json);
            
            // Parse JSON to ESP32SensorPayload
            ESP32SensorPayload payload = gson.fromJson(json, ESP32SensorPayload.class);
            System.out.println("[DataServlet] Parsed payload: deviceId=" + payload.getDeviceId() + 
                             ", temp=" + payload.getTemperature() + 
                             ", humidity=" + payload.getHumidity() +
                             ", mq135=" + payload.getMq135() +
                             ", mq7=" + payload.getMq7() +
                             ", mq2=" + payload.getMq2() +
                             ", dust=" + payload.getDust());
            
            // Validate deviceId
            if (payload.getDeviceId() == null || payload.getDeviceId().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"deviceId is required\",\"received\":false}");
                System.err.println("[DataServlet] Error: deviceId is required");
                return;
            }
            
            // Save sensor data to database
            try {
                service.saveSensorDataFromESP32(payload);
                System.out.println("[DataServlet] Successfully saved sensor data for device: " + payload.getDeviceId());
                
                // Return success response
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"status\":\"ok\",\"received\":true,\"message\":\"Data saved successfully\"}");
            } catch (Exception e) {
                System.err.println("[DataServlet] Error saving sensor data: " + e.getMessage());
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to persist data: " + 
                                     e.getMessage().replace("\"", "'") + "\",\"received\":false}");
            }
            
        } catch (JsonSyntaxException e) {
            System.err.println("[DataServlet] JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload: " + 
                                 e.getMessage().replace("\"", "'") + "\",\"received\":false}");
        } catch (Exception e) {
            System.err.println("[DataServlet] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Server error: " + 
                                 e.getMessage().replace("\"", "'") + "\",\"received\":false}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}

