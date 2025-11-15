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
     * Maps ESP32 field names to database sensor codes.
     * ALL sensors from the same payload share the SAME timestamp (saved together).
     */
    public void saveSensorDataFromESP32(ESP32SensorPayload payload) {
        List<SensorData> dataList = new ArrayList<>();
        
        String deviceIdStr = payload.getDeviceId();
        if (deviceIdStr == null || deviceIdStr.isEmpty()) {
            throw new RuntimeException("deviceId is required");
        }
        
        // Convert deviceId from String to Integer
        // If deviceId is a number, parse it; otherwise, try to find device by name or create it
        Integer deviceId;
        try {
            deviceId = Integer.parseInt(deviceIdStr);
        } catch (NumberFormatException e) {
            // If deviceId is not a number, try to find device by name
            DeviceInfo device = findDeviceByName(deviceIdStr);
            if (device != null) {
                deviceId = device.getDeviceId();
            } else {
                // Create new device if not found
                DeviceInfo newDevice = new DeviceInfo();
                newDevice.setDeviceName(deviceIdStr);
                newDevice.setLocation("Unknown");
                newDevice.setStatus("Active");
                deviceId = deviceInfoDAO.insert(newDevice);
                if (deviceId == null) {
                    throw new RuntimeException("Failed to create device: " + deviceIdStr);
                }
            }
        }
        
        // CRITICAL: Create ONE timestamp for ALL sensors from the same payload
        // This ensures all sensors are saved with the same timestamp
        LocalDateTime timestamp = LocalDateTime.now();
        
        System.out.println("[SensorService] Saving sensor data batch for device: " + deviceId + " at timestamp: " + timestamp);
        
        // Map each sensor reading to a SensorData record
        // New schema uses SensorCode: TEMP_DHT11, HUM_DHT11, MQ2, MQ7, MQ135, GP2Y10
        if (payload.getTemperature() != null) {
            dataList.add(createSensorData(deviceId, "TEMP_DHT11", payload.getTemperature(), timestamp));
        }
        if (payload.getHumidity() != null) {
            dataList.add(createSensorData(deviceId, "HUM_DHT11", payload.getHumidity(), timestamp));
        }
        // Map mq2 (ESP32) -> MQ2 (database)
        if (payload.getMq2() != null) {
            dataList.add(createSensorData(deviceId, "MQ2", payload.getMq2(), timestamp));
        }
        // Map mq7 (ESP32) -> MQ7 (database)
        if (payload.getMq7() != null) {
            dataList.add(createSensorData(deviceId, "MQ7", payload.getMq7(), timestamp));
        }
        // Map mq135 (ESP32) -> MQ135 (database)
        if (payload.getMq135() != null) {
            dataList.add(createSensorData(deviceId, "MQ135", payload.getMq135(), timestamp));
        }
        // Map dust (ESP32) -> GP2Y10 (database)
        if (payload.getDust() != null) {
            dataList.add(createSensorData(deviceId, "GP2Y10", payload.getDust(), timestamp));
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
     * Helper method to find device by name.
     */
    private DeviceInfo findDeviceByName(String deviceName) {
        List<DeviceInfo> devices = deviceInfoDAO.findAll();
        for (DeviceInfo device : devices) {
            if (deviceName.equals(device.getDeviceName())) {
                return device;
            }
        }
        return null;
    }

    /**
     * Helper method to create SensorData from sensor code and value.
     * Uses the provided timestamp to ensure all sensors from the same payload have the same timestamp.
     */
    private SensorData createSensorData(Integer deviceId, String sensorCode, Double value, LocalDateTime timestamp) {
        SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
        if (sensorType == null) {
            throw new RuntimeException("Sensor type not found with code: " + sensorCode);
        }
        
        // Use the provided timestamp (same for all sensors from the same payload)
        SensorData data = new SensorData(deviceId, sensorType.getSensorTypeId(), value);
        data.setCreatedAt(timestamp);
        return data;
    }

    /**
     * Get recent sensor data for a device.
     */
    public List<SensorData> getRecentData(Integer deviceId, int limit) {
        return sensorDataDAO.findLatestByDevice(deviceId, limit);
    }

    /**
     * Get sensor data within time range.
     */
    public List<SensorData> getDataByTimeRange(Integer deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return sensorDataDAO.findByTimeRange(deviceId, startTime, endTime);
    }

    /**
     * Get latest reading for a specific sensor type.
     */
    public SensorData getLatestBySensorType(Integer deviceId, Integer sensorTypeId) {
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
                "SELECT sd FROM SensorData sd ORDER BY sd.createdAt DESC", 
                SensorData.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
