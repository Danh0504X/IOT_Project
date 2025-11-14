package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.SensorSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for SensorSettings table - JDBC implementation.
 * Manages threshold values for each sensor type per device.
 */
public class SensorSettingsDAO {

    /**
     * Find setting by ID.
     */
    public SensorSettings findById(Long settingId) {
        String sql = "SELECT id, device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at " +
                     "FROM SensorSettings WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, settingId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSensorSettings(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensor setting by ID: " + settingId, e);
        }
        return null;
    }

    /**
     * Find all settings for a device.
     */
    public List<SensorSettings> findByDeviceId(String deviceId) {
        String sql = "SELECT id, device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at " +
                     "FROM SensorSettings WHERE device_id = ?";
        
        List<SensorSettings> settings = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SensorSettings setting = new SensorSettings();
                    setting.setSettingId(rs.getLong("id"));  // Read from 'id' column
                    setting.setDeviceId(rs.getString("device_id"));
                    setting.setSensorTypeId(rs.getInt("sensor_type_id"));
                    setting.setSensitivity(rs.getDouble("sensitivity"));
                    setting.setCalibrationFactor(rs.getDouble("calibration_factor"));
                    setting.setThresholdValue(rs.getDouble("threshold_value"));
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        setting.setUpdatedAt(updatedAt.toLocalDateTime());
                    }
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        setting.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    settings.add(setting);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding settings for device: " + deviceId, e);
        }
        
        return settings;
    }

    /**
     * Find settings for a specific sensor type on a device.
     */
    public SensorSettings findByDeviceAndSensorType(String deviceId, Integer sensorTypeId) {
        String sql = "SELECT id, device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at " +
                     "FROM SensorSettings WHERE device_id = ? AND sensor_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deviceId);
            ps.setInt(2, sensorTypeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSensorSettings(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding setting for device and sensor type", e);
        }
        return null;
    }

    /**
     * Get threshold map for ESP32 (sensor_name -> threshold_value).
     */
    public Map<String, Double> getThresholdMapForDevice(String deviceId) {
        String sql = "SELECT st.name, ss.threshold_value " +
                     "FROM SensorSettings ss " +
                     "INNER JOIN SensorType st ON ss.sensor_type_id = st.sensor_type_id " +
                     "WHERE ss.device_id = ?";
        
        Map<String, Double> thresholds = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sensorName = rs.getString("name");
                    Double thresholdValue = rs.getDouble("threshold_value");
                    thresholds.put(sensorName, thresholdValue);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting threshold map for device: " + deviceId, e);
        }
        
        return thresholds;
    }

    /**
     * Insert new sensor setting.
     */
    public Long insert(SensorSettings setting) {
        String sql = "INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, GETUTCDATE(), GETUTCDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, setting.getDeviceId());
            ps.setInt(2, setting.getSensorTypeId());
            ps.setDouble(3, setting.getSensitivity() != null ? setting.getSensitivity() : 1.0);
            ps.setDouble(4, setting.getCalibrationFactor() != null ? setting.getCalibrationFactor() : 1.0);
            ps.setDouble(5, setting.getThresholdValue());
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting sensor setting", e);
        }
        
        return null;
    }

    /**
     * Update sensor setting.
     */
    public boolean update(SensorSettings setting) {
        String sql = "UPDATE SensorSettings SET device_id = ?, sensor_type_id = ?, sensitivity = ?, calibration_factor = ?, " +
                     "threshold_value = ?, updated_at = GETUTCDATE() WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, setting.getDeviceId());
            ps.setInt(2, setting.getSensorTypeId());
            ps.setDouble(3, setting.getSensitivity() != null ? setting.getSensitivity() : 1.0);
            ps.setDouble(4, setting.getCalibrationFactor() != null ? setting.getCalibrationFactor() : 1.0);
            ps.setDouble(5, setting.getThresholdValue());
            ps.setLong(6, setting.getSettingId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating sensor setting", e);
        }
    }

    /**
     * Update threshold value for a specific device and sensor type.
     */
    public boolean updateThreshold(String deviceId, Integer sensorTypeId, Double thresholdValue) {
        String sql = "UPDATE SensorSettings SET threshold_value = ?, updated_at = GETUTCDATE() " +
                     "WHERE device_id = ? AND sensor_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, thresholdValue);
            ps.setString(2, deviceId);
            ps.setInt(3, sensorTypeId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating threshold", e);
        }
    }

    /**
     * Upsert (insert or update) sensor setting.
     */
    public void upsert(SensorSettings setting) {
        SensorSettings existing = findByDeviceAndSensorType(setting.getDeviceId(), setting.getSensorTypeId());
        
        if (existing != null) {
            setting.setSettingId(existing.getSettingId());
            update(setting);
        } else {
            insert(setting);
        }
    }

    /**
     * Delete sensor setting by ID.
     */
    public boolean delete(Long settingId) {
        String sql = "DELETE FROM SensorSettings WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, settingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sensor setting: " + settingId, e);
        }
    }

    /**
     * Map ResultSet to SensorSettings object.
     */
    private SensorSettings mapResultSetToSensorSettings(ResultSet rs) throws SQLException {
        SensorSettings setting = new SensorSettings();
        setting.setSettingId(rs.getLong("id"));
        setting.setDeviceId(rs.getString("device_id"));
        setting.setSensorTypeId(rs.getInt("sensor_type_id"));
        setting.setSensitivity(rs.getDouble("sensitivity"));
        setting.setCalibrationFactor(rs.getDouble("calibration_factor"));
        setting.setThresholdValue(rs.getDouble("threshold_value"));
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            setting.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            setting.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return setting;
    }
}
