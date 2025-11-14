package com.mycompany.iotwebapp.service;

import com.mycompany.iotwebapp.dao.SensorSettingsDAO;
import com.mycompany.iotwebapp.dao.SensorTypeDAO;
import com.mycompany.iotwebapp.dao.DeviceCommandDAO;
import com.mycompany.iotwebapp.model.SensorSettings;
import com.mycompany.iotwebapp.model.SensorType;
import com.mycompany.iotwebapp.model.DeviceCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for sensor settings and thresholds management.
 */
public class ThresholdService {

    private final SensorSettingsDAO settingsDAO;
    private final SensorTypeDAO sensorTypeDAO;
    private final DeviceCommandDAO commandDAO;

    public ThresholdService() {
        this.settingsDAO = new SensorSettingsDAO();
        this.sensorTypeDAO = new SensorTypeDAO();
        this.commandDAO = new DeviceCommandDAO();
    }

    public ThresholdService(SensorSettingsDAO settingsDAO, SensorTypeDAO sensorTypeDAO, DeviceCommandDAO commandDAO) {
        this.settingsDAO = settingsDAO;
        this.sensorTypeDAO = sensorTypeDAO;
        this.commandDAO = commandDAO;
    }

    /**
     * Get all settings for a device.
     */
    public List<SensorSettings> getSettings(String deviceId) {
        return settingsDAO.findByDeviceId(deviceId);
    }

    /**
     * Get threshold map for ESP32 (sensor_name -> threshold_value).
     * Maps database names to standard names (e.g., "Gas MQ1" -> "mq1").
     */
    public Map<String, Double> getThresholdMapForESP32(String deviceId) {
        System.out.println("[ThresholdService] Getting threshold map for ESP32, deviceId: " + deviceId);
        
        Map<String, Double> dbThresholds = settingsDAO.getThresholdMapForDevice(deviceId);
        System.out.println("[ThresholdService] Retrieved " + (dbThresholds != null ? dbThresholds.size() : 0) + " thresholds from database");
        
        if (dbThresholds != null && !dbThresholds.isEmpty()) {
            System.out.println("[ThresholdService] Database thresholds:");
            for (Map.Entry<String, Double> entry : dbThresholds.entrySet()) {
                System.out.println("  DB Name: '" + entry.getKey() + "' = " + entry.getValue());
            }
        } else {
            System.out.println("[ThresholdService] ⚠ No thresholds found in database for device: " + deviceId);
        }
        
        Map<String, Double> standardThresholds = new HashMap<>();
        
        // Map database sensor names to standard names for ESP32/dashboard
        for (Map.Entry<String, Double> entry : dbThresholds.entrySet()) {
            String dbName = entry.getKey();
            String standardName = mapDatabaseNameToStandard(dbName);
            System.out.println("[ThresholdService] Mapping: '" + dbName + "' -> '" + standardName + "' = " + entry.getValue());
            standardThresholds.put(standardName, entry.getValue());
        }
        
        System.out.println("[ThresholdService] Final standard thresholds map size: " + standardThresholds.size());
        if (!standardThresholds.isEmpty()) {
            System.out.println("[ThresholdService] Standard thresholds:");
            for (Map.Entry<String, Double> entry : standardThresholds.entrySet()) {
                System.out.println("  '" + entry.getKey() + "' = " + entry.getValue());
            }
        }
        
        return standardThresholds;
    }

    /**
     * Update threshold for a specific sensor type.
     * Maps standard names to database names (e.g., "mq1" -> "Gas MQ1").
     * Preserves existing sensitivity and calibrationFactor values.
     */
    public void updateThreshold(String deviceId, String sensorName, Double thresholdValue) {
        // Map standard name to database name
        String dbSensorName = mapStandardNameToDatabase(sensorName);
        System.out.println("[ThresholdService] Updating threshold: device=" + deviceId + ", sensor=" + sensorName + " -> " + dbSensorName + ", value=" + thresholdValue);
        
        SensorType sensorType = sensorTypeDAO.findByName(dbSensorName);
        if (sensorType == null) {
            throw new RuntimeException("Sensor type not found: " + dbSensorName + " (from standard name: " + sensorName + ")");
        }
        
        // Check if setting already exists
        SensorSettings existing = settingsDAO.findByDeviceAndSensorType(deviceId, sensorType.getSensorTypeId());
        
        if (existing != null) {
            // Update existing setting - preserve sensitivity and calibrationFactor
            System.out.println("[ThresholdService] Found existing setting for device=" + deviceId + ", sensorTypeId=" + sensorType.getSensorTypeId() + 
                             ", existing threshold=" + existing.getThresholdValue() + 
                             ", sensitivity=" + existing.getSensitivity() + 
                             ", calibrationFactor=" + existing.getCalibrationFactor());
            existing.setThresholdValue(thresholdValue);
            boolean updated = settingsDAO.update(existing);
            System.out.println("[ThresholdService] Updated existing setting: " + updated);
        } else {
            // Create new setting with default values
            System.out.println("[ThresholdService] Creating new setting for device=" + deviceId + ", sensorTypeId=" + sensorType.getSensorTypeId());
            SensorSettings setting = new SensorSettings();
            setting.setDeviceId(deviceId);
            setting.setSensorTypeId(sensorType.getSensorTypeId());
            setting.setThresholdValue(thresholdValue);
            setting.setSensitivity(1.0); // Default value
            setting.setCalibrationFactor(1.0); // Default value
            Long settingId = settingsDAO.insert(setting);
            System.out.println("[ThresholdService] Created new setting with id: " + settingId);
        }
    }

    /**
     * Map standard sensor name to database name.
     * Standard: "temperature", "mq1" -> Database: "Temperature", "Gas MQ1"
     */
    private String mapStandardNameToDatabase(String standardName) {
        if (standardName == null) return null;
        
        switch (standardName.toLowerCase()) {
            case "temperature":
                return "Temperature";
            case "humidity":
                return "Humidity";
            case "mq1":
                return "Gas MQ1";
            case "mq2":
                return "Gas MQ2";
            case "mq3":
                return "Gas MQ3";
            case "dust":
                return "Dust Density";
            default:
                return standardName; // Assume it's already a database name
        }
    }

    /**
     * Map database sensor name to standard name.
     * Database: "Temperature", "Gas MQ1" -> Standard: "temperature", "mq1"
     */
    private String mapDatabaseNameToStandard(String dbName) {
        if (dbName == null) return null;
        
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
     * Update multiple thresholds from form data.
     */
    public void updateThresholds(String deviceId, Map<String, Double> thresholds) {
        for (Map.Entry<String, Double> entry : thresholds.entrySet()) {
            updateThreshold(deviceId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get pending commands for a device.
     */
    public List<DeviceCommand> getPendingCommands(String deviceId) {
        return commandDAO.findPendingByDeviceId(deviceId);
    }

    /**
     * Mark command as executed.
     */
    public void markCommandExecuted(Integer commandId) {
        commandDAO.markAsExecuted(commandId);
    }

    /**
     * Create a new command for a device.
     */
    public Integer createCommand(String deviceId, String commandType, String commandValue) {
        DeviceCommand command = new DeviceCommand(deviceId, commandType, commandValue);
        return commandDAO.insert(command);
    }

    /**
     * Get setting for a specific sensor type.
     * Maps standard name to database name.
     */
    public SensorSettings getSettingBySensorType(String deviceId, String sensorName) {
        // Map standard name to database name
        String dbSensorName = mapStandardNameToDatabase(sensorName);
        
        SensorType sensorType = sensorTypeDAO.findByName(dbSensorName);
        if (sensorType == null) {
            return null;
        }
        return settingsDAO.findByDeviceAndSensorType(deviceId, sensorType.getSensorTypeId());
    }
}

