package com.mycompany.iotwebapp.service;

import com.mycompany.iotwebapp.dao.*;
import com.mycompany.iotwebapp.model.*;
import com.mycompany.iotwebapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for sensor data operations.
 * Handles business logic and coordinates between DAOs.
 */
public class SensorService {

    private final SensorDataDAO sensorDataDAO;
    private final DeviceInfoDAO deviceInfoDAO;
    private final SensorTypeDAO sensorTypeDAO;

    public SensorService() {
        this.sensorDataDAO = new SensorDataDAO();
        this.deviceInfoDAO = new DeviceInfoDAO();
        this.sensorTypeDAO = new SensorTypeDAO();
    }

    public SensorService(SensorDataDAO sensorDataDAO, DeviceInfoDAO deviceInfoDAO, SensorTypeDAO sensorTypeDAO) {
        this.sensorDataDAO = sensorDataDAO;
        this.deviceInfoDAO = deviceInfoDAO;
        this.sensorTypeDAO = sensorTypeDAO;
    }

    /**
     * Process and save sensor data from ESP32 payload.
     * Converts flat ESP32 payload to multiple SensorData records.
     * Maps ESP32 field names (mq135, mq7, mq2) to database sensor names.
     * ALL sensors from the same payload share the SAME timestamp (saved together).
     */
    public void saveSensorDataFromESP32(ESP32SensorPayload payload) {
        List<SensorData> dataList = new ArrayList<>();
        
        String deviceId = payload.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            throw new RuntimeException("deviceId is required");
        }
        
        // CRITICAL: Create ONE timestamp for ALL sensors from the same payload
        // This ensures all sensors are saved with the same timestamp
        LocalDateTime timestamp = LocalDateTime.now();
        
        System.out.println("[SensorService] Saving sensor data batch for device: " + deviceId + " at timestamp: " + timestamp);
        
        // Map each sensor reading to a SensorData record
        // Note: Database uses names like "Temperature", "Gas MQ1", etc.
        if (payload.getTemperature() != null) {
            dataList.add(createSensorData(deviceId, "Temperature", payload.getTemperature(), timestamp));
        }
        if (payload.getHumidity() != null) {
            dataList.add(createSensorData(deviceId, "Humidity", payload.getHumidity(), timestamp));
        }
        // Map mq135 (ESP32) -> "Gas MQ1" (database)
        if (payload.getMq135() != null) {
            dataList.add(createSensorData(deviceId, "Gas MQ1", payload.getMq135(), timestamp));
        }
        // Map mq7 (ESP32) -> "Gas MQ2" (database)
        if (payload.getMq7() != null) {
            dataList.add(createSensorData(deviceId, "Gas MQ2", payload.getMq7(), timestamp));
        }
        // Map mq2 (ESP32) -> "Gas MQ3" (database)
        if (payload.getMq2() != null) {
            dataList.add(createSensorData(deviceId, "Gas MQ3", payload.getMq2(), timestamp));
        }
        if (payload.getDust() != null) {
            dataList.add(createSensorData(deviceId, "Dust Density", payload.getDust(), timestamp));
        }
        
        // Save all data in batch with the SAME timestamp
        if (!dataList.isEmpty()) {
            System.out.println("[SensorService] Saving " + dataList.size() + " sensor records together with timestamp: " + timestamp);
            sensorDataDAO.insertBatch(dataList);
            System.out.println("[SensorService] Successfully saved batch of " + dataList.size() + " sensor records");
            
            // Update device last_seen
            deviceInfoDAO.updateLastSeen(deviceId);
        } else {
            System.out.println("[SensorService] No sensor data to save (all values are null)");
        }
    }

    /**
     * Helper method to create SensorData from sensor name (database name) and value.
     * Sets all required fields including defaults for foreign key constraints.
     * Uses the provided timestamp to ensure all sensors from the same payload have the same timestamp.
     */
    private SensorData createSensorData(String deviceId, String sensorName, Double value, LocalDateTime timestamp) {
        SensorType sensorType = sensorTypeDAO.findByName(sensorName);
        if (sensorType == null) {
            throw new RuntimeException("Sensor type not found: " + sensorName);
        }
        
        // Use the provided timestamp (same for all sensors from the same payload)
        SensorData data = new SensorData();
        data.setDeviceId(deviceId);
        data.setSensorTypeId(sensorType.getSensorTypeId());
        data.setSensorValue(value);
        data.setValue(value); // Also set 'value' field (duplicate but required by DB)
        data.setSensorIndex(0); // CRITICAL: Must match DeviceSensor.sensor_index (default is 0)
        data.setIsValidated(true); // Default to validated
        data.setCreatedAt(timestamp);
        data.setTs(timestamp); // Set ts timestamp (same for all sensors)
        data.setTimestamp(timestamp); // Set timestamp (same for all sensors)
        return data;
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

    /**
     * Get recent sensor data for a device.
     */
    public List<SensorData> getRecentData(String deviceId, int limit) {
        return sensorDataDAO.findLatestByDevice(deviceId, limit);
    }

    /**
     * Get sensor data within time range.
     */
    public List<SensorData> getDataByTimeRange(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return sensorDataDAO.findByTimeRange(deviceId, startTime, endTime);
    }

    /**
     * Get latest reading for a specific sensor type.
     */
    public SensorData getLatestBySensorType(String deviceId, Integer sensorTypeId) {
        return sensorDataDAO.findLatestBySensorType(deviceId, sensorTypeId);
    }

    /**
     * Get all sensor data with pagination.
     */
    public List<SensorData> getAllData(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<SensorData> query = em.createQuery(
                "SELECT sd FROM SensorData sd ORDER BY sd.timestamp DESC", 
                SensorData.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}


