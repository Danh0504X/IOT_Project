package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.dao.SensorDataDAO;
import com.mycompany.iotwebapp.dao.DeviceInfoDAO;
import com.mycompany.iotwebapp.model.SensorData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import org.json.JSONObject;

// DEPRECATED: Use DataServlet instead
// This servlet is kept for backward compatibility but should be removed
// @WebServlet(name = "DataApiServlet", urlPatterns = {"/api/data"})
@WebServlet(name = "DataApiServlet", urlPatterns = {"/api/data-legacy"})
public class DataApiServlet extends HttpServlet {

    private SensorDataDAO sensorDataDAO;
    private DeviceInfoDAO deviceInfoDAO;

    @Override
    public void init() throws ServletException {
        sensorDataDAO = new SensorDataDAO();
        deviceInfoDAO = new DeviceInfoDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            String jsonString = jsonBuilder.toString();
            if (jsonString.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"status\":\"error\",\"message\":\"Empty request body\"}");
                return;
            }

            JSONObject json = new JSONObject(jsonString);

            String deviceId = json.optString("deviceId", "DEVICE001");
            
            // CRITICAL: Ensure device exists in database before saving sensor data
            // This prevents FOREIGN KEY constraint violation
            try {
                deviceInfoDAO.updateLastSeen(deviceId);
                System.out.println("[DataApiServlet] Device " + deviceId + " exists or was created");
            } catch (Exception e) {
                System.err.println("[DataApiServlet] Error ensuring device exists: " + e.getMessage());
                e.printStackTrace();
                // Continue anyway - will fail later with better error message
            }
            
            LocalDateTime now = LocalDateTime.now();
            int savedCount = 0;

            if (json.has("temperature"))
                if (saveSensorData(deviceId, 1, json.getDouble("temperature"), now)) savedCount++;

            if (json.has("humidity"))
                if (saveSensorData(deviceId, 2, json.getDouble("humidity"), now)) savedCount++;

            if (json.has("mq135"))
                if (saveSensorData(deviceId, 3, json.getDouble("mq135"), now)) savedCount++;

            if (json.has("mq7"))
                if (saveSensorData(deviceId, 4, json.getDouble("mq7"), now)) savedCount++;

            if (json.has("mq2"))
                if (saveSensorData(deviceId, 5, json.getDouble("mq2"), now)) savedCount++;

            if (json.has("dust"))
                if (saveSensorData(deviceId, 6, json.getDouble("dust"), now)) savedCount++;

            response.setStatus(HttpServletResponse.SC_OK);
            out.write(String.format(
                "{\"status\":\"success\",\"message\":\"Data saved\",\"deviceId\":\"%s\",\"recordsSaved\":%d}",
                deviceId, savedCount
            ));

            System.out.printf("[%s] Received data from %s - Saved %d records%n",
                    now.toString(), deviceId, savedCount);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\",\"message\":\"" +
                    e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private boolean saveSensorData(String deviceId, int sensorTypeId, double value, LocalDateTime timestamp) {
        try {
            SensorData data = new SensorData(deviceId, sensorTypeId, value);

            data.setTs(timestamp);
            data.setTimestamp(timestamp);
            data.setCreatedAt(timestamp);

            Long id = sensorDataDAO.insert(data);
            return id != null && id > 0;
        } catch (Exception e) {
            System.err.println("Failed saving sensorTypeId=" + sensorTypeId +
                    " value=" + value + " for device=" + deviceId);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.write("{"
                + "\"status\":\"online\","
                + "\"endpoint\":\"/api/data\","
                + "\"method\":\"POST\","
                + "\"description\":\"Receive sensor data from ESP32\","
                + "\"format\":{"
                + "\"deviceId\":\"string\","
                + "\"temperature\":\"number\","
                + "\"humidity\":\"number\","
                + "\"mq135\":\"number\","
                + "\"mq7\":\"number\","
                + "\"mq2\":\"number\","
                + "\"dust\":\"number\""
                + "}"
                + "}");
    }
}
