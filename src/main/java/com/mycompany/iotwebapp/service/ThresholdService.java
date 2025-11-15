package com.mycompany.iotwebapp.service;

import com.mycompany.iotwebapp.dao.ThresholdDAO;
import com.mycompany.iotwebapp.dao.SensorTypeDAO;
import com.mycompany.iotwebapp.dao.DeviceCommandDAO;
import com.mycompany.iotwebapp.model.Threshold;
import com.mycompany.iotwebapp.model.SensorType;
import com.mycompany.iotwebapp.model.DeviceCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for threshold management.
 * Works with new Threshold table schema.
 */
public class ThresholdService {

    private final ThresholdDAO thresholdDAO;
    private final SensorTypeDAO sensorTypeDAO;
    private final DeviceCommandDAO commandDAO;

    public ThresholdService() {
        this.thresholdDAO = new ThresholdDAO();
        this.sensorTypeDAO = new SensorTypeDAO();
        this.commandDAO = new DeviceCommandDAO();
    }

    public ThresholdService(ThresholdDAO thresholdDAO, SensorTypeDAO sensorTypeDAO, DeviceCommandDAO commandDAO) {
        this.thresholdDAO = thresholdDAO;
        this.sensorTypeDAO = sensorTypeDAO;
        this.commandDAO = commandDAO;
    }

    /**
     * Get threshold map for ESP32 (sensor_name -> threshold_value).
     * Returns the MaxValue of the "Bình thường" (normal) level for each sensor type.
     */
    public Map<String, Double> getThresholdMapForESP32(Integer deviceId) {
        System.out.println("[ThresholdService] Getting threshold map for ESP32, deviceId: " + deviceId);
        
        Map<String, Double> standardThresholds = new HashMap<>();
        
        // Map standard names to sensor codes
        // Note: ESP32 sends mq135, mq7, mq2 but server uses different naming
        // Response format: mq2=CO (MQ7), mq3=Gas/LPG (MQ2), mq1/mq135=Air Quality (MQ135)
        Map<String, String> sensorCodeMap = new HashMap<>();
        sensorCodeMap.put("temperature", "TEMP_DHT11");
        sensorCodeMap.put("humidity", "HUM_DHT11");
        sensorCodeMap.put("mq1", "MQ135");      // MQ135 Air Quality
        sensorCodeMap.put("mq135", "MQ135");    // MQ135 Air Quality (alternative)
        sensorCodeMap.put("mq2", "MQ7");        // MQ7 CO (server returns as "mq2")
        sensorCodeMap.put("mq3", "MQ2");        // MQ2 Gas/LPG (server returns as "mq3")
        sensorCodeMap.put("dust", "GP2Y10");
        
        // Get threshold for each sensor type
        for (Map.Entry<String, String> entry : sensorCodeMap.entrySet()) {
            String standardName = entry.getKey();
            String sensorCode = entry.getValue();
            
            try {
                SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
                if (sensorType != null) {
                    // Get all thresholds for this sensor type
                    List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorType.getSensorTypeId());
                    
                    // Find the "Bình thường" (normal) threshold and use its MaxValue
                    for (Threshold threshold : thresholds) {
                        if (threshold.getLevelName() != null && 
                            (threshold.getLevelName().contains("Bình thường") || 
                             threshold.getLevelName().contains("An toàn") ||
                             threshold.getLevelName().contains("Tốt"))) {
                            standardThresholds.put(standardName, (double) threshold.getMaxValue());
                            System.out.println("[ThresholdService] Found threshold for " + standardName + 
                                             " (sensorCode: " + sensorCode + "): " + threshold.getMaxValue());
                            break;
                        }
                    }
                    
                    // If no "Bình thường" found, use the highest MaxValue
                    if (!standardThresholds.containsKey(standardName) && !thresholds.isEmpty()) {
                        Threshold highest = thresholds.get(thresholds.size() - 1);
                        standardThresholds.put(standardName, (double) highest.getMaxValue());
                        System.out.println("[ThresholdService] Using highest threshold for " + standardName + ": " + highest.getMaxValue());
                    }
                } else {
                    System.out.println("[ThresholdService] ⚠ Sensor type not found for code: " + sensorCode);
                }
            } catch (Exception e) {
                System.err.println("[ThresholdService] Error getting threshold for " + standardName + ": " + e.getMessage());
            }
        }
        
        System.out.println("[ThresholdService] Final thresholds map: " + standardThresholds);
        return standardThresholds;
    }

    /**
     * Update threshold for a specific sensor type.
     * Updates the MaxValue of the "Bình thường" threshold level.
     * If "Bình thường" doesn't exist, creates a new threshold level.
     */
    public void updateThreshold(Integer deviceId, String sensorName, Double thresholdValue) {
        System.out.println("[ThresholdService] ========================================");
        System.out.println("[ThresholdService] Updating threshold: device=" + deviceId + ", sensor=" + sensorName + ", value=" + thresholdValue);
        
        // Map standard name to sensor code
        String sensorCode = mapStandardNameToSensorCode(sensorName);
        if (sensorCode == null) {
            System.err.println("[ThresholdService] ✗ Unknown sensor name: " + sensorName);
            throw new RuntimeException("Unknown sensor name: " + sensorName);
        }
        System.out.println("[ThresholdService] Mapped sensor name '" + sensorName + "' to sensor code: " + sensorCode);
        
        SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
        if (sensorType == null) {
            System.err.println("[ThresholdService] ✗ Sensor type not found for code: " + sensorCode);
            throw new RuntimeException("Sensor type not found for code: " + sensorCode + " (from standard name: " + sensorName + ")");
        }
        System.out.println("[ThresholdService] Found sensor type: ID=" + sensorType.getSensorTypeId() + ", Name='" + sensorType.getSensorName() + "'");
        
        // Get all thresholds for this sensor type
        List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorType.getSensorTypeId());
        System.out.println("[ThresholdService] Found " + thresholds.size() + " existing thresholds for sensor type " + sensorType.getSensorTypeId());
        
        // Find the "Bình thường" threshold
        Threshold normalThreshold = null;
        for (Threshold threshold : thresholds) {
            System.out.println("[ThresholdService] Checking threshold: ID=" + threshold.getThresholdId() + 
                             ", LevelName='" + threshold.getLevelName() + 
                             "', MinValue=" + threshold.getMinValue() + 
                             ", MaxValue=" + threshold.getMaxValue());
            if (threshold.getLevelName() != null && 
                (threshold.getLevelName().contains("Bình thường") || 
                 threshold.getLevelName().contains("An toàn") ||
                 threshold.getLevelName().contains("Tốt"))) {
                normalThreshold = threshold;
                System.out.println("[ThresholdService] ✓ Found matching threshold: ID=" + threshold.getThresholdId() + 
                                 ", LevelName='" + threshold.getLevelName() + "'");
                break;
            }
        }
        
        if (normalThreshold != null) {
            // Update existing "Bình thường" threshold - update MaxValue
            System.out.println("[ThresholdService] Updating existing threshold (ID: " + normalThreshold.getThresholdId() + 
                             "), LevelName: '" + normalThreshold.getLevelName() + 
                             "', MaxValue: " + normalThreshold.getMaxValue() + " → " + thresholdValue);
            
            // Get current user ID from session (for logging) - default to 1 if not available
            Integer updatedBy = 1; // TODO: Get from session
            
            normalThreshold.setMaxValue(thresholdValue.floatValue());
            boolean updated = thresholdDAO.update(normalThreshold, updatedBy);
            if (updated) {
                System.out.println("[ThresholdService] ✅ Successfully updated threshold ID: " + normalThreshold.getThresholdId());
            } else {
                System.err.println("[ThresholdService] ✗ Failed to update threshold ID: " + normalThreshold.getThresholdId());
            }
        } else {
            // Create new threshold level
            System.out.println("[ThresholdService] ⚠ No 'Bình thường' threshold found, creating new one for sensorTypeId=" + sensorType.getSensorTypeId());
            
            // Determine appropriate MinValue based on sensor type
            Float minValue = getDefaultMinValue(sensorCode);
            System.out.println("[ThresholdService] Using default MinValue: " + minValue + " for sensor code: " + sensorCode);
            
            Threshold newThreshold = new Threshold();
            newThreshold.setSensorTypeId(sensorType.getSensorTypeId());
            newThreshold.setLevelName("Bình thường");
            newThreshold.setMinValue(minValue);
            newThreshold.setMaxValue(thresholdValue.floatValue());
            newThreshold.setAlertLevel(0);
            newThreshold.setMessage("Ngưỡng cảnh báo " + sensorName);
            
            Integer thresholdId = thresholdDAO.insert(newThreshold);
            if (thresholdId != null) {
                System.out.println("[ThresholdService] ✅ Created new threshold with id: " + thresholdId);
            } else {
                System.err.println("[ThresholdService] ✗ Failed to create new threshold");
            }
        }
        System.out.println("[ThresholdService] ========================================");
    }

    /**
     * Map standard sensor name to sensor code.
     */
    private String mapStandardNameToSensorCode(String standardName) {
        if (standardName == null) return null;
        
        switch (standardName.toLowerCase()) {
            case "temperature":
                return "TEMP_DHT11";
            case "humidity":
                return "HUM_DHT11";
            case "mq1":
                return "MQ135";
            case "mq2":
                return "MQ7";
            case "mq3":
                return "MQ2";
            case "dust":
                return "GP2Y10";
            default:
                return null;
        }
    }

    /**
     * Get default MinValue for a sensor code.
     */
    private Float getDefaultMinValue(String sensorCode) {
        switch (sensorCode) {
            case "TEMP_DHT11":
                return 18.0f;
            case "HUM_DHT11":
                return 30.0f;
            case "MQ135":
            case "MQ7":
            case "MQ2":
                return 0.0f;
            case "GP2Y10":
                return 0.0f;
            default:
                return 0.0f;
        }
    }

    /**
     * Update multiple thresholds from form data.
     */
    public void updateThresholds(Integer deviceId, Map<String, Double> thresholds) {
        for (Map.Entry<String, Double> entry : thresholds.entrySet()) {
            updateThreshold(deviceId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get pending commands for a device.
     */
    public List<DeviceCommand> getPendingCommands(Integer deviceId) {
        // Convert Integer deviceId to String for DeviceCommandDAO
        return commandDAO.findPendingByDeviceId(String.valueOf(deviceId));
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
    public Integer createCommand(Integer deviceId, String commandType, String commandValue) {
        // Convert Integer deviceId to String for DeviceCommand
        DeviceCommand command = new DeviceCommand(String.valueOf(deviceId), commandType, commandValue);
        return commandDAO.insert(command);
    }

    /**
     * Get threshold for a specific sensor type.
     */
    public Threshold getThresholdBySensorType(String sensorName) {
        String sensorCode = mapStandardNameToSensorCode(sensorName);
        if (sensorCode == null) {
            return null;
        }
        
        SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
        if (sensorType == null) {
            return null;
        }
        
        List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorType.getSensorTypeId());
        for (Threshold threshold : thresholds) {
            if (threshold.getLevelName() != null && 
                (threshold.getLevelName().contains("Bình thường") || 
                 threshold.getLevelName().contains("An toàn") ||
                 threshold.getLevelName().contains("Tốt"))) {
                return threshold;
            }
        }
        
        return thresholds.isEmpty() ? null : thresholds.get(0);
    }
}
