package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.DeviceSensor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for DeviceSensor table - JDBC implementation.
 */
public class DeviceSensorDAO {

    /**
     * Find device sensor by ID.
     */
    public DeviceSensor findById(Integer deviceSensorId) {
        String sql = "SELECT device_sensor_id, device_id, sensor_type_id, sensor_pin, status " +
                     "FROM DeviceSensor WHERE device_sensor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceSensorId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDeviceSensor(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding device sensor by ID: " + deviceSensorId, e);
        }
        return null;
    }

    /**
     * Find all sensors for a device.
     */
    public List<DeviceSensor> findByDeviceId(Integer deviceId) {
        String sql = "SELECT device_sensor_id, device_id, sensor_type_id, sensor_pin, status " +
                     "FROM DeviceSensor WHERE device_id = ?";
        
        List<DeviceSensor> sensors = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sensors.add(mapResultSetToDeviceSensor(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensors for device: " + deviceId, e);
        }
        
        return sensors;
    }

    /**
     * Find all active sensors for a device.
     */
    public List<DeviceSensor> findActiveByDeviceId(Integer deviceId) {
        String sql = "SELECT device_sensor_id, device_id, sensor_type_id, sensor_pin, status " +
                     "FROM DeviceSensor WHERE device_id = ? AND status = 'ACTIVE'";
        
        List<DeviceSensor> sensors = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sensors.add(mapResultSetToDeviceSensor(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active sensors for device: " + deviceId, e);
        }
        
        return sensors;
    }

    /**
     * Find all device sensors.
     */
    public List<DeviceSensor> findAll() {
        String sql = "SELECT device_sensor_id, device_id, sensor_type_id, sensor_pin, status FROM DeviceSensor";
        
        List<DeviceSensor> sensors = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                sensors.add(mapResultSetToDeviceSensor(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all device sensors", e);
        }
        
        return sensors;
    }

    /**
     * Insert new device sensor mapping.
     */
    public Integer insert(DeviceSensor deviceSensor) {
        String sql = "INSERT INTO DeviceSensor (device_id, sensor_type_id, sensor_pin, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, deviceSensor.getDeviceId());
            ps.setInt(2, deviceSensor.getSensorTypeId());
            ps.setString(3, deviceSensor.getSensorPin());
            ps.setString(4, deviceSensor.getStatus() != null ? deviceSensor.getStatus() : "ACTIVE");
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting device sensor", e);
        }
        
        return null;
    }

    /**
     * Update device sensor.
     */
    public boolean update(DeviceSensor deviceSensor) {
        String sql = "UPDATE DeviceSensor SET device_id = ?, sensor_type_id = ?, sensor_pin = ?, status = ? " +
                     "WHERE device_sensor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceSensor.getDeviceId());
            ps.setInt(2, deviceSensor.getSensorTypeId());
            ps.setString(3, deviceSensor.getSensorPin());
            ps.setString(4, deviceSensor.getStatus());
            ps.setInt(5, deviceSensor.getDeviceSensorId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating device sensor", e);
        }
    }

    /**
     * Delete device sensor by ID.
     */
    public boolean delete(Integer deviceSensorId) {
        String sql = "DELETE FROM DeviceSensor WHERE device_sensor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceSensorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting device sensor: " + deviceSensorId, e);
        }
    }

    /**
     * Map ResultSet to DeviceSensor object.
     */
    private DeviceSensor mapResultSetToDeviceSensor(ResultSet rs) throws SQLException {
        DeviceSensor deviceSensor = new DeviceSensor();
        deviceSensor.setDeviceSensorId(rs.getInt("device_sensor_id"));
        deviceSensor.setDeviceId(rs.getInt("device_id"));
        deviceSensor.setSensorTypeId(rs.getInt("sensor_type_id"));
        deviceSensor.setSensorPin(rs.getString("sensor_pin"));
        deviceSensor.setStatus(rs.getString("status"));
        return deviceSensor;
    }
}
