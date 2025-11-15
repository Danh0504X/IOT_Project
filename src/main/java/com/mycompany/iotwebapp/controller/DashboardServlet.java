package com.mycompany.iotwebapp.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.iotwebapp.model.DashboardData;
import com.mycompany.iotwebapp.model.SensorData;
import com.mycompany.iotwebapp.service.SensorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet to display the main dashboard with sensor data.
 * Endpoint: GET /dashboard
 */
@WebServlet(name = "dashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient SensorService service = new SensorService();
    private final transient Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Get deviceId from parameter (can be number or device name like "DEVICE001")
            String deviceIdStr = req.getParameter("deviceId");
            Integer deviceId = null;
            com.mycompany.iotwebapp.dao.DeviceInfoDAO deviceDAO = new com.mycompany.iotwebapp.dao.DeviceInfoDAO();
            java.util.List<com.mycompany.iotwebapp.model.DeviceInfo> devices = deviceDAO.findAll();
            
            if (deviceIdStr == null || deviceIdStr.isEmpty()) {
                // If no deviceId provided, try to find first device with data or default to first device
                if (!devices.isEmpty()) {
                    // Try to find a device that has sensor data
                    for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                        long count = new com.mycompany.iotwebapp.dao.SensorDataDAO().countByDevice(device.getDeviceId());
                        if (count > 0) {
                            deviceId = device.getDeviceId();
                            System.out.println("[DashboardServlet] No deviceId provided, using device with data: " + deviceId + " (has " + count + " records)");
                            break;
                        }
                    }
                    // If no device has data, use first device
                    if (deviceId == null) {
                        deviceId = devices.get(0).getDeviceId();
                        System.out.println("[DashboardServlet] No deviceId provided, no devices have data, using first device: " + deviceId);
                    }
                } else {
                    deviceId = 1; // Default to device ID 1
                    System.out.println("[DashboardServlet] No deviceId provided and no devices found, defaulting to: " + deviceId);
                }
            } else {
                try {
                    deviceId = Integer.parseInt(deviceIdStr);
                } catch (NumberFormatException e) {
                    // If deviceId is not a number (e.g., "DEVICE001"), try to find device by name
                    System.out.println("[DashboardServlet] deviceId '" + deviceIdStr + "' is not a number, trying to find by name...");
                    com.mycompany.iotwebapp.model.DeviceInfo foundDevice = null;
                    for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                        if (deviceIdStr.equals(device.getDeviceName())) {
                            foundDevice = device;
                            break;
                        }
                    }
                    if (foundDevice != null) {
                        deviceId = foundDevice.getDeviceId();
                        System.out.println("[DashboardServlet] Found device by name: " + deviceIdStr + " -> DeviceID=" + deviceId);
                    } else {
                        // Default to first device with data, or first device, or 1
                        deviceId = null;
                        for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                            long count = new com.mycompany.iotwebapp.dao.SensorDataDAO().countByDevice(device.getDeviceId());
                            if (count > 0) {
                                deviceId = device.getDeviceId();
                                System.out.println("[DashboardServlet] Device not found by name, using device with data: " + deviceId);
                                break;
                            }
                        }
                        if (deviceId == null) {
                            if (!devices.isEmpty()) {
                                deviceId = devices.get(0).getDeviceId();
                                System.out.println("[DashboardServlet] Device not found by name, using first device: " + deviceId);
                            } else {
                                deviceId = 1;
                                System.out.println("[DashboardServlet] Device not found by name, defaulting to: " + deviceId);
                            }
                        }
                    }
                }
            }
            
            // Final check: ensure deviceId is not null
            if (deviceId == null) {
                deviceId = 1;
                System.out.println("[DashboardServlet] Warning: deviceId was null, defaulting to 1");
            }
            
            System.out.println("[DashboardServlet] Loading data for device: " + deviceId);
            
            // Get recent sensor data from database
            List<SensorData> rawData = service.getRecentData(deviceId, 100);
            System.out.println("[DashboardServlet] Loaded " + rawData.size() + " raw sensor records");
            
            // Convert normalized data to dashboard format
            List<DashboardData> dashboardData = convertToDashboardData(rawData);
            System.out.println("[DashboardServlet] Converted to " + dashboardData.size() + " dashboard records");
            
            // Check if JSON format is requested
            String format = req.getParameter("format");
            if ("json".equalsIgnoreCase(format)) {
                // Return JSON response for AJAX requests
                resp.setContentType("application/json");
                resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                
                // Convert DashboardData to JSON-serializable format
                List<Map<String, Object>> jsonData = new ArrayList<>();
                for (DashboardData dd : dashboardData) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceId", dd.getDeviceId());
                    item.put("timestamp", dd.getTimestamp() != null ? 
                        dd.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                    item.put("temperature", dd.getTemperature());
                    item.put("humidity", dd.getHumidity());
                    item.put("mq1", dd.getMq1());
                    item.put("mq2", dd.getMq2());
                    item.put("mq3", dd.getMq3());
                    item.put("dust", dd.getDust());
                    item.put("wifiSignal", dd.getWifiSignal());
                    item.put("uptime", dd.getUptime());
                    jsonData.add(item);
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("deviceId", deviceId);
                response.put("data", jsonData);
                response.put("count", jsonData.size());
                
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            // Default: Render JSP view
            req.setAttribute("deviceId", deviceId);
            req.setAttribute("sensorData", dashboardData);
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
            
        } catch (Exception e) {
            System.err.println("[DashboardServlet] Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            
            // If JSON format requested, return JSON error
            String format = req.getParameter("format");
            if ("json".equalsIgnoreCase(format)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", e.getMessage());
                resp.getWriter().write(gson.toJson(error));
                return;
            }
            
            throw new ServletException("Error loading dashboard data", e);
        }
    }

    /**
     * Convert normalized SensorData list to aggregated DashboardData list.
     * Groups sensor readings by timestamp (with tolerance for microsecond differences).
     */
    private List<DashboardData> convertToDashboardData(List<SensorData> rawData) {
        System.out.println("[DashboardServlet] Converting " + rawData.size() + " raw sensor records to dashboard data");
        
        if (rawData.isEmpty()) {
            System.out.println("[DashboardServlet] No raw data to convert");
            return new ArrayList<>();
        }
        
        // Group by timestamp with tolerance (sensors from same payload may have slightly different timestamps)
        // Use a key based on timestamp rounded to seconds to group sensors from the same reading
        Map<String, List<SensorData>> grouped = new HashMap<>();
        for (SensorData sensor : rawData) {
            LocalDateTime timestamp = sensor.getTimestamp();
            if (timestamp == null) {
                timestamp = sensor.getCreatedAt(); // Fallback to createdAt
            }
            if (timestamp == null) {
                timestamp = LocalDateTime.now(); // Last resort: use current time
                System.out.println("[DashboardServlet] Warning: Sensor data has no timestamp, using current time");
            }
            
            // Create a key based on timestamp rounded to seconds (to group sensors from same payload)
            String timeKey = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            grouped.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(sensor);
        }
        
        System.out.println("[DashboardServlet] Grouped into " + grouped.size() + " timestamp groups");

        // Convert to DashboardData
        List<DashboardData> result = new ArrayList<>();
        for (Map.Entry<String, List<SensorData>> entry : grouped.entrySet()) {
            List<SensorData> sensors = entry.getValue();
            
            // Use the earliest timestamp from the group as the representative timestamp
            LocalDateTime timestamp = sensors.stream()
                .map(s -> s.getTimestamp() != null ? s.getTimestamp() : s.getCreatedAt())
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

            DashboardData data = new DashboardData();
            data.setTimestamp(timestamp);

            if (!sensors.isEmpty()) {
                data.setDeviceId(sensors.get(0).getDeviceId());
                System.out.println("[DashboardServlet] Processing group with " + sensors.size() + " sensors for device " + data.getDeviceId() + " at " + timestamp);

                // Aggregate sensor values
                // Map database sensor names to standard names for dashboard
                for (SensorData sensor : sensors) {
                    String dbSensorName = sensor.getSensorName();
                    Double value = sensor.getSensorValue();
                    Integer sensorTypeId = sensor.getSensorTypeId();
                    
                    System.out.println("[DashboardServlet] Processing sensor: typeId=" + sensorTypeId + ", name='" + dbSensorName + "', value=" + value);
                    
                    if (value != null) {
                        // Try to map by sensor type ID first (more reliable than name due to encoding issues)
                        String standardName = mapSensorTypeIdToStandard(sensorTypeId);
                        
                        // If mapping by ID failed, try by name
                        if (standardName == null && dbSensorName != null) {
                            standardName = mapDatabaseNameToStandard(dbSensorName);
                            System.out.println("[DashboardServlet] Mapped by name: '" + dbSensorName + "' -> '" + standardName + "'");
                        } else if (standardName != null) {
                            System.out.println("[DashboardServlet] Mapped by typeId: " + sensorTypeId + " -> '" + standardName + "'");
                        }
                        
                        if (standardName != null) {
                            data.addSensorValue(standardName, value);
                        } else {
                            System.out.println("[DashboardServlet] Warning: Could not map sensor - typeId=" + sensorTypeId + ", name='" + dbSensorName + "'");
                        }
                    } else {
                        System.out.println("[DashboardServlet] Warning: Sensor has null value - typeId=" + sensorTypeId + ", name=" + dbSensorName);
                    }
                }
                
                System.out.println("[DashboardServlet] DashboardData created with " + data.getSensorValues().size() + " sensor values: " + data.getSensorValues());
            }

            result.add(data);
        }

        // Sort by timestamp descending
        result.sort((a, b) -> {
            LocalDateTime tsA = a.getTimestamp();
            LocalDateTime tsB = b.getTimestamp();
            if (tsA == null && tsB == null) return 0;
            if (tsA == null) return 1;
            if (tsB == null) return -1;
            return tsB.compareTo(tsA);
        });

        System.out.println("[DashboardServlet] Converted to " + result.size() + " dashboard records");
        return result;
    }
    
    /**
     * Map sensor type ID to standard name (more reliable than name mapping due to encoding issues).
     * Based on sql_script.txt:
     * 1 = TEMP_DHT11 (Temperature)
     * 2 = HUM_DHT11 (Humidity)
     * 3 = MQ2 (Gas/LPG) -> mq3
     * 4 = MQ7 (CO) -> mq2
     * 5 = MQ135 (Air Quality) -> mq1
     * 6 = GP2Y10 (PM2.5/Dust) -> dust
     */
    private String mapSensorTypeIdToStandard(Integer sensorTypeId) {
        if (sensorTypeId == null) return null;
        
        switch (sensorTypeId) {
            case 1: return "temperature";  // TEMP_DHT11
            case 2: return "humidity";     // HUM_DHT11
            case 3: return "mq3";          // MQ2 (Gas/LPG)
            case 4: return "mq2";          // MQ7 (CO)
            case 5: return "mq1";          // MQ135 (Air Quality)
            case 6: return "dust";         // GP2Y10 (PM2.5)
            default:
                System.out.println("[DashboardServlet] Unknown sensorTypeId: " + sensorTypeId);
                return null;
        }
    }

    /**
     * Map database sensor name to standardized name for dashboard/API.
     * New schema uses names like "DHT11 - Nhiệt độ", "MQ2 - Gas/LPG", etc.
     */
    private String mapDatabaseNameToStandard(String dbName) {
        if (dbName == null) {
            System.out.println("[DashboardServlet] Warning: dbName is null in mapDatabaseNameToStandard");
            return null;
        }
        
        String lowerName = dbName.toLowerCase();
        
        // Map new database names to standard lowercase names
        // Check for temperature first (TEMP_DHT11)
        if (lowerName.contains("nhiệt độ") || lowerName.contains("temperature") || lowerName.contains("temp_dht11") || lowerName.contains("temp")) {
            return "temperature";
        } 
        // Check for humidity (HUM_DHT11)
        else if (lowerName.contains("độ ẩm") || lowerName.contains("humidity") || lowerName.contains("hum_dht11") || lowerName.contains("hum")) {
            return "humidity";
        } 
        // Check for MQ135 (Air Quality) - maps to mq1 for backward compatibility
        else if (lowerName.contains("mq135") || lowerName.contains("chất lượng không khí")) {
            return "mq1";
        } 
        // Check for MQ7 (CO) - maps to mq2 for backward compatibility
        else if (lowerName.contains("mq7") || (lowerName.contains("co") && !lowerName.contains("mq135"))) {
            return "mq2";
        } 
        // Check for MQ2 (Gas/LPG) - maps to mq3 for backward compatibility
        else if (lowerName.contains("mq2") || lowerName.contains("gas") || lowerName.contains("lpg")) {
            return "mq3";
        } 
        // Check for GP2Y10 (PM2.5/Dust)
        else if (lowerName.contains("bụi") || lowerName.contains("pm2.5") || lowerName.contains("gp2y10") || lowerName.contains("dust")) {
            return "dust";
        }
        
        System.out.println("[DashboardServlet] Warning: Unknown sensor name '" + dbName + "', returning lowercase");
        return dbName.toLowerCase();
    }
}
