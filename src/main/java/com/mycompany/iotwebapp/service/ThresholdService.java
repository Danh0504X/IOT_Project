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

    /**
     * Get all threshold levels for a specific sensor type.
     * Returns a map: sensorName -> List of Thresholds (all levels)
     * ALWAYS returns default thresholds for form display, even if database is empty.
     * This ensures the form is always visible for user input.
     */
    public Map<String, List<Threshold>> getAllThresholdLevels() {
        Map<String, List<Threshold>> result = new HashMap<>();
        
        // Map standard names to sensor codes (only unique mappings to avoid overwriting)
        // Use consistent keys: temperature, humidity, mq1, mq2, mq3, dust
        Map<String, String> sensorCodeMap = new HashMap<>();
        sensorCodeMap.put("temperature", "TEMP_DHT11");
        sensorCodeMap.put("humidity", "HUM_DHT11");
        sensorCodeMap.put("mq1", "MQ135");      // MQ135 Air Quality
        sensorCodeMap.put("mq2", "MQ7");        // MQ7 CO
        sensorCodeMap.put("mq3", "MQ2");        // MQ2 Gas/LPG
        sensorCodeMap.put("dust", "GP2Y10");    // PM2.5
        
        // Default SensorType IDs (fallback if not found in DB)
        // These should match the order in sql_script.txt
        Map<String, Integer> defaultSensorTypeIds = new HashMap<>();
        defaultSensorTypeIds.put("temperature", 1);  // TEMP_DHT11
        defaultSensorTypeIds.put("humidity", 2);     // HUM_DHT11
        defaultSensorTypeIds.put("mq3", 3);          // MQ2 (Gas/LPG)
        defaultSensorTypeIds.put("mq2", 4);          // MQ7 (CO)
        defaultSensorTypeIds.put("mq1", 5);          // MQ135
        defaultSensorTypeIds.put("dust", 6);         // GP2Y10
        
        for (Map.Entry<String, String> entry : sensorCodeMap.entrySet()) {
            String standardName = entry.getKey();
            String sensorCode = entry.getValue();
            Integer sensorTypeId = null;
            
            try {
                // Try to find SensorType from database
                SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
                if (sensorType != null) {
                    sensorTypeId = sensorType.getSensorTypeId();
                    System.out.println("[ThresholdService] Found SensorType for " + standardName + ": ID=" + sensorTypeId);
                    
                    // Try to load existing thresholds from database
                    List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorTypeId);
                    if (thresholds != null && !thresholds.isEmpty()) {
                        result.put(standardName, thresholds);
                        System.out.println("[ThresholdService] ✅ Loaded " + thresholds.size() + " levels from DB for " + standardName);
                        continue; // Skip to next sensor
                    }
                } else {
                    // SensorType not found, use default ID
                    sensorTypeId = defaultSensorTypeIds.get(standardName);
                    System.out.println("[ThresholdService] ⚠ SensorType not found for " + standardName + ", using default ID: " + sensorTypeId);
                }
                
                // If we reach here, either no SensorType found or no thresholds in DB
                // Always provide default thresholds for form display
                if (sensorTypeId == null) {
                    // If still no sensorTypeId, use default from map
                    sensorTypeId = defaultSensorTypeIds.get(standardName);
                    System.out.println("[ThresholdService] Using fallback SensorTypeID: " + sensorTypeId + " for " + standardName);
                }
                
                if (sensorTypeId != null) {
                    List<Threshold> defaultThresholds = getDefaultThresholds(standardName, sensorTypeId);
                    if (defaultThresholds != null && !defaultThresholds.isEmpty()) {
                        result.put(standardName, defaultThresholds);
                        System.out.println("[ThresholdService] ✅ Using " + defaultThresholds.size() + " default levels for " + standardName + " (form display)");
                    } else {
                        System.err.println("[ThresholdService] ✗ Failed to create default thresholds for " + standardName);
                    }
                } else {
                    System.err.println("[ThresholdService] ✗ No SensorTypeID available for " + standardName + " - cannot create defaults");
                }
                
            } catch (Exception e) {
                System.err.println("[ThresholdService] ✗ Error getting thresholds for " + standardName + ": " + e.getMessage());
                e.printStackTrace();
                
                // Even on error, try to provide defaults
                try {
                    Integer fallbackId = defaultSensorTypeIds.get(standardName);
                    if (fallbackId != null) {
                        List<Threshold> defaultThresholds = getDefaultThresholds(standardName, fallbackId);
                        if (defaultThresholds != null && !defaultThresholds.isEmpty()) {
                            result.put(standardName, defaultThresholds);
                            System.out.println("[ThresholdService] ✅ Using defaults for " + standardName + " after error");
                        }
                    }
                } catch (Exception e2) {
                    System.err.println("[ThresholdService] ✗ Failed to create defaults after error: " + e2.getMessage());
                }
            }
        }
        
        // Ensure we always return at least 6 sensors (one for each type)
        if (result.size() < 6) {
            System.err.println("[ThresholdService] ⚠ WARNING: Only " + result.size() + " sensors loaded, expected 6");
        }
        
        System.out.println("[ThresholdService] getAllThresholdLevels() returning " + result.size() + " sensor types (form will always display)");
        return result;
    }

    /**
     * Get default threshold structure for a sensor type (for form display when no data exists).
     */
    private List<Threshold> getDefaultThresholds(String sensorName, Integer sensorTypeId) {
        List<Threshold> defaults = new java.util.ArrayList<>();
        
        switch (sensorName) {
            case "temperature":
                // 4 levels: Lạnh, Bình thường, Nóng, Rất nóng
                defaults.add(createDefaultThreshold(sensorTypeId, "Lạnh", -40f, 18f, 0, "Nhiệt độ thấp"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Bình thường", 18f, 32f, 0, "Nhiệt độ thoải mái"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Nóng", 32f, 39f, 1, "Nhiệt độ cao"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Rất nóng", 39f, 80f, 2, "Cảnh báo say nóng"));
                break;
            case "humidity":
                // 3 levels: Khô, Bình thường, Ẩm
                defaults.add(createDefaultThreshold(sensorTypeId, "Khô", 0f, 30f, 1, "Không khí khô"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Bình thường", 30f, 70f, 0, "Độ ẩm tốt"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Ẩm", 70f, 100f, 1, "Độ ẩm cao"));
                break;
            case "mq1": // MQ135
                // 4 levels: Tốt, Trung bình, Nguy hiểm, Rất nguy hiểm
                defaults.add(createDefaultThreshold(sensorTypeId, "Tốt", 0f, 200f, 0, "Chất lượng không khí tốt"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Trung bình", 200f, 400f, 1, "Chất lượng không khí trung bình"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Nguy hiểm", 400f, 600f, 2, "Chất lượng không khí kém"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Rất nguy hiểm", 600f, 1000f, 3, "Chất lượng không khí rất kém"));
                break;
            case "mq2": // MQ7 CO
                // 4 levels: An toàn, Chú ý, Nguy hiểm, Rất nguy hiểm
                defaults.add(createDefaultThreshold(sensorTypeId, "An toàn", 0f, 50f, 0, "Nồng độ CO an toàn"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Chú ý", 50f, 200f, 1, "Kiểm tra thông gió"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Nguy hiểm", 200f, 800f, 2, "NGUY HIỂM! Tăng thông gió"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Rất nguy hiểm", 800f, 2000f, 3, "SƠ TÁN NGAY! Nguy cơ tử vong"));
                break;
            case "mq3": // MQ2 Gas/LPG
                // 4 levels: An toàn, Chú ý, Rò rỉ, Nguy cơ nổ
                defaults.add(createDefaultThreshold(sensorTypeId, "An toàn", 0f, 300f, 0, "Không phát hiện gas"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Chú ý", 300f, 1000f, 1, "Có gas, kiểm tra nguồn"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Rò rỉ", 1000f, 5000f, 2, "RÒ RỈ GAS! Tắt nguồn ngay"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Nguy cơ nổ", 5000f, 10000f, 3, "CỰC NGUY HIỂM! Sơ tán ngay"));
                break;
            case "dust": // PM2.5
                // 5 levels: Tốt, Trung bình, Kém, Xấu, Nguy hiểm
                defaults.add(createDefaultThreshold(sensorTypeId, "Tốt", 0f, 12f, 0, "Không khí tốt"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Trung bình", 12f, 35f, 1, "Chấp nhận được"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Kém", 35f, 55f, 2, "Nhạy cảm nên hạn chế ra ngoài"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Xấu", 55f, 150f, 2, "Hạn chế hoạt động ngoài trời"));
                defaults.add(createDefaultThreshold(sensorTypeId, "Nguy hiểm", 150f, 500f, 3, "KHẨN CẤP! Ở trong nhà"));
                break;
        }
        
        return defaults;
    }

    /**
     * Helper method to create a default threshold object.
     */
    private Threshold createDefaultThreshold(Integer sensorTypeId, String levelName, Float minValue, Float maxValue, Integer alertLevel, String message) {
        Threshold threshold = new Threshold();
        threshold.setSensorTypeId(sensorTypeId);
        threshold.setLevelName(levelName);
        threshold.setMinValue(minValue);
        threshold.setMaxValue(maxValue);
        threshold.setAlertLevel(alertLevel);
        threshold.setMessage(message);
        // thresholdId is null for new thresholds
        return threshold;
    }

    /**
     * Update all threshold levels for a sensor type.
     * @param sensorName Standard sensor name (e.g., "temperature", "humidity")
     * @param thresholds List of Threshold objects with updated values
     * @param updatedBy User ID who made the update
     */
    public void updateAllThresholdLevels(String sensorName, List<Threshold> thresholds, Integer updatedBy) {
        System.out.println("[ThresholdService] Updating all threshold levels for sensor: " + sensorName);
        
        if (thresholds == null || thresholds.isEmpty()) {
            System.out.println("[ThresholdService] ⚠ No thresholds provided for " + sensorName);
            return;
        }
        
        // Get sensor type ID
        String sensorCode = mapStandardNameToSensorCode(sensorName);
        if (sensorCode == null) {
            System.err.println("[ThresholdService] ✗ Unknown sensor name: " + sensorName);
            return;
        }
        
        SensorType sensorType = sensorTypeDAO.findByCode(sensorCode);
        if (sensorType == null) {
            System.err.println("[ThresholdService] ✗ SensorType not found for code: " + sensorCode);
            return;
        }
        
        for (Threshold threshold : thresholds) {
            // Ensure sensorTypeId is set
            threshold.setSensorTypeId(sensorType.getSensorTypeId());
            
            if (threshold.getThresholdId() != null) {
                // Update existing threshold
                boolean updated = thresholdDAO.update(threshold, updatedBy);
                if (updated) {
                    System.out.println("[ThresholdService] ✅ Updated threshold ID: " + threshold.getThresholdId() + 
                                     ", Level: " + threshold.getLevelName());
                } else {
                    System.err.println("[ThresholdService] ✗ Failed to update threshold ID: " + threshold.getThresholdId());
                }
            } else {
                // Insert new threshold
                Integer thresholdId = thresholdDAO.insert(threshold);
                if (thresholdId != null) {
                    System.out.println("[ThresholdService] ✅ Created new threshold ID: " + thresholdId + 
                                     ", Level: " + threshold.getLevelName());
                } else {
                    System.err.println("[ThresholdService] ✗ Failed to create new threshold");
                }
            }
        }
    }
}
