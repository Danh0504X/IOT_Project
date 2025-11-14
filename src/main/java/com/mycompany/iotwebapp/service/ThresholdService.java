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
        Map<String, Double> dbThresholds = settingsDAO.getThresholdMapForDevice(deviceId);
        Map<String, Double> standardThresholds = new HashMap<>();
        
        // Map database sensor names to standard names for ESP32/dashboard
        for (Map.Entry<String, Double> entry : dbThresholds.entrySet()) {
            String dbName = entry.getKey();
            String standardName = mapDatabaseNameToStandard(dbName);
            standardThresholds.put(standardName, entry.getValue());
        }
        
        return standardThresholds;
    }

    /**
     * Update threshold for a specific sensor type.
     * Maps standard names to database names (e.g., "mq1" -> "Gas MQ1").
     */
    public void updateThreshold(String deviceId, String sensorName, Double thresholdValue) {
        // Map standard name to database name
        String dbSensorName = mapStandardNameToDatabase(sensorName);
        
        SensorType sensorType = sensorTypeDAO.findByName(dbSensorName);
        if (sensorType == null) {
            throw new RuntimeException("Sensor type not found: " + dbSensorName + " (from standard name: " + sensorName + ")");
        }
        
        SensorSettings setting = new SensorSettings();
        setting.setDeviceId(deviceId);
        setting.setSensorTypeId(sensorType.getSensorTypeId());
        setting.setThresholdValue(thresholdValue);
        
        settingsDAO.upsert(setting);
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

