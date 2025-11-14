package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.model.DashboardData;
import com.mycompany.iotwebapp.model.SensorData;
import com.mycompany.iotwebapp.service.SensorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet to display the main dashboard with sensor data.
 * Endpoint: GET /dashboard
 */
@WebServlet(name = "dashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient SensorService service = new SensorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Get deviceId from parameter (default to "DEVICE001")
            String deviceId = req.getParameter("deviceId");
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = "DEVICE001";
            }
            
            System.out.println("[DashboardServlet] Loading data for device: " + deviceId);
            
            // Get recent sensor data from database
            List<SensorData> rawData = service.getRecentData(deviceId, 100);
            System.out.println("[DashboardServlet] Loaded " + rawData.size() + " raw sensor records");
            
            // Debug: Print raw data
            for (SensorData sd : rawData) {
                System.out.println("[DashboardServlet] Raw: sensorTypeId=" + sd.getSensorTypeId() + 
                                 ", sensorName=" + sd.getSensorName() + 
                                 ", value=" + sd.getSensorValue() + 
                                 ", timestamp=" + sd.getTimestamp());
            }
            
            // Convert normalized data to dashboard format
            List<DashboardData> dashboardData = convertToDashboardData(rawData);
            System.out.println("[DashboardServlet] Converted to " + dashboardData.size() + " dashboard records");
            
            // Debug: Print dashboard data
            for (DashboardData dd : dashboardData) {
                System.out.println("[DashboardServlet] Dashboard: deviceId=" + dd.getDeviceId() + 
                                 ", timestamp=" + dd.getTimestamp() + 
                                 ", values=" + dd.getSensorValues());
            }
            
            req.setAttribute("deviceId", deviceId);
            req.setAttribute("sensorData", dashboardData);
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
            
        } catch (Exception e) {
            System.err.println("[DashboardServlet] Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Error loading dashboard data", e);
        }
    }

    /**
     * Convert normalized SensorData list to aggregated DashboardData list.
     * Groups sensor readings by timestamp.
     */
    private List<DashboardData> convertToDashboardData(List<SensorData> rawData) {
        // Group by timestamp
        Map<LocalDateTime, List<SensorData>> grouped = rawData.stream()
                .collect(Collectors.groupingBy(SensorData::getTimestamp));

        // Convert to DashboardData
        List<DashboardData> result = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<SensorData>> entry : grouped.entrySet()) {
            LocalDateTime timestamp = entry.getKey();
            List<SensorData> sensors = entry.getValue();

            DashboardData data = new DashboardData();
            data.setTimestamp(timestamp);

            if (!sensors.isEmpty()) {
                data.setDeviceId(sensors.get(0).getDeviceId());

                // Aggregate sensor values
                // Map database sensor names (e.g., "Temperature", "Gas MQ1") to standard names (e.g., "temperature", "mq1")
                for (SensorData sensor : sensors) {
                    String dbSensorName = sensor.getSensorName();
                    Double value = sensor.getSensorValue();
                    if (dbSensorName != null && value != null) {
                        // Convert database name to standard name for dashboard
                        String standardName = mapDatabaseNameToStandard(dbSensorName);
                        data.addSensorValue(standardName, value);
                    }
                }
            }

            result.add(data);
        }

        // Sort by timestamp descending
        result.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return result;
    }

    /**
     * Map database sensor name to standardized name for dashboard/API.
     * Database: "Temperature", "Gas MQ1" -> Dashboard: "temperature", "mq1"
     */
    private String mapDatabaseNameToStandard(String dbName) {
        if (dbName == null) return null;
        
        // Map database names to standard lowercase names
        switch (dbName.toLowerCase()) {
            case "temperature":
                return "temperature";
            case "humidity":
                return "humidity";
            case "gas mq1":
                return "mq1";
            case "gas mq2":
                return "mq2";
            case "gas mq3":
                return "mq3";
            case "dust density":
                return "dust";
            default:
                return dbName.toLowerCase();
        }
    }
}

