package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.dao.SensorDataDAO;
import com.mycompany.iotwebapp.dao.DeviceInfoDAO;
import com.mycompany.iotwebapp.dao.SensorTypeDAO;
import com.mycompany.iotwebapp.model.SensorData;
import com.mycompany.iotwebapp.model.SensorType;
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
    private SensorTypeDAO sensorTypeDAO;

    @Override
    public void init() throws ServletException {
        sensorDataDAO = new SensorDataDAO();
        deviceInfoDAO = new DeviceInfoDAO();
        sensorTypeDAO = new SensorTypeDAO();
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

            String deviceIdStr = json.optString("deviceId", "1");
            
            // Convert deviceId to Integer
            Integer deviceId;
            try {
                deviceId = Integer.parseInt(deviceIdStr);
            } catch (NumberFormatException e) {
                // If deviceId is not a number, try to find device by name or default to 1
                System.out.println("[DataApiServlet] Warning: deviceId '" + deviceIdStr + "' is not a number, using default deviceId=1");
                deviceId = 1;
            }
            
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

            // Map ESP32 field names to SensorType IDs based on new schema
            // SensorType IDs: 1=TEMP_DHT11, 2=HUM_DHT11, 3=MQ2, 4=MQ7, 5=MQ135, 6=GP2Y10
            if (json.has("temperature")) {
                Integer sensorTypeId = getSensorTypeIdByCode("TEMP_DHT11");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("temperature"), now)) {
                    savedCount++;
                }
            }

            if (json.has("humidity")) {
                Integer sensorTypeId = getSensorTypeIdByCode("HUM_DHT11");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("humidity"), now)) {
                    savedCount++;
                }
            }

            if (json.has("mq135")) {
                Integer sensorTypeId = getSensorTypeIdByCode("MQ135");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("mq135"), now)) {
                    savedCount++;
                }
            }

            if (json.has("mq7")) {
                Integer sensorTypeId = getSensorTypeIdByCode("MQ7");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("mq7"), now)) {
                    savedCount++;
                }
            }

            if (json.has("mq2")) {
                Integer sensorTypeId = getSensorTypeIdByCode("MQ2");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("mq2"), now)) {
                    savedCount++;
                }
            }

            if (json.has("dust")) {
                Integer sensorTypeId = getSensorTypeIdByCode("GP2Y10");
                if (sensorTypeId != null && saveSensorData(deviceId, sensorTypeId, json.getDouble("dust"), now)) {
                    savedCount++;
                }
            }

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

    /**
     * Get SensorType ID by SensorCode.
     */
    private Integer getSensorTypeIdByCode(String sensorCode) {
        try {
            SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
            return sensorType != null ? sensorType.getSensorTypeId() : null;
        } catch (Exception e) {
            System.err.println("[DataApiServlet] Error finding sensor type for code: " + sensorCode);
            e.printStackTrace();
            return null;
        }
    }

    private boolean saveSensorData(Integer deviceId, Integer sensorTypeId, double value, LocalDateTime timestamp) {
        try {
            SensorData data = new SensorData(deviceId, sensorTypeId, value);
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
                + "\"endpoint\":\"/api/data-legacy\","
                + "\"method\":\"POST\","
                + "\"description\":\"Receive sensor data from ESP32 (Legacy endpoint)\","
                + "\"format\":{"
                + "\"deviceId\":\"string or number\","
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
