package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.DeviceInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for DeviceInfo table - JDBC implementation.
 */
public class DeviceInfoDAO {

    /**
     * Find device by ID.
     */
    public DeviceInfo findById(Integer deviceId) {
        String sql = "SELECT device_id, device_name, device_type, mac_address, ip_address, " +
                     "location, status, registered_at, last_seen FROM DeviceInfo WHERE device_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDeviceInfo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding device by ID: " + deviceId, e);
        }
        return null;
    }

    /**
     * Find all devices.
     */
    public List<DeviceInfo> findAll() {
        String sql = "SELECT device_id, device_name, device_type, mac_address, ip_address, " +
                     "location, status, registered_at, last_seen FROM DeviceInfo ORDER BY device_name";
        
        List<DeviceInfo> devices = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                devices.add(mapResultSetToDeviceInfo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all devices", e);
        }
        
        return devices;
    }

    /**
     * Find devices by status.
     */
    public List<DeviceInfo> findByStatus(String status) {
        String sql = "SELECT device_id, device_name, device_type, mac_address, ip_address, " +
                     "location, status, registered_at, last_seen FROM DeviceInfo WHERE status = ?";
        
        List<DeviceInfo> devices = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    devices.add(mapResultSetToDeviceInfo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding devices by status: " + status, e);
        }
        
        return devices;
    }

    /**
     * Insert new device.
     */
    public Integer insert(DeviceInfo device) {
        String sql = "INSERT INTO DeviceInfo (device_name, device_type, mac_address, ip_address, " +
                     "location, status, registered_at) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getDeviceType());
            ps.setString(3, device.getMacAddress());
            ps.setString(4, device.getIpAddress());
            ps.setString(5, device.getLocation());
            ps.setString(6, device.getStatus() != null ? device.getStatus() : "ACTIVE");
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting device", e);
        }
        
        return null;
    }

    /**
     * Update device.
     */
    public boolean update(DeviceInfo device) {
        String sql = "UPDATE DeviceInfo SET device_name = ?, device_type = ?, mac_address = ?, " +
                     "ip_address = ?, location = ?, status = ? WHERE device_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getDeviceType());
            ps.setString(3, device.getMacAddress());
            ps.setString(4, device.getIpAddress());
            ps.setString(5, device.getLocation());
            ps.setString(6, device.getStatus());
            ps.setInt(7, device.getDeviceId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating device", e);
        }
    }

    /**
     * Update device updated_at timestamp (acts as last_seen).
     * If device doesn't exist, creates it automatically.
     * Also ensures DeviceSensor entries exist for all sensor types (required for foreign key constraints).
     */
    public boolean updateLastSeen(String deviceId) {
        // First, try to update existing device
        // Note: Database uses 'updated_at' column, not 'last_seen'
        String updateSql = "UPDATE DeviceInfo SET updated_at = GETDATE() WHERE device_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            
            ps.setString(1, deviceId);
            int updated = ps.executeUpdate();
            
            // If device doesn't exist, create it
            if (updated == 0) {
                String insertSql = "INSERT INTO DeviceInfo (device_id, device_name, status, created_at, updated_at) " +
                                 "VALUES (?, ?, 'ACTIVE', GETDATE(), GETDATE())";
                
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, deviceId);
                    insertPs.setString(2, deviceId); // Use deviceId as device_name if not provided
                    insertPs.executeUpdate();
                    System.out.println("[DeviceInfoDAO] Created new device: " + deviceId);
                }
                
                // CRITICAL: Create DeviceSensor entries for all sensor types (1-6)
                // This is required because SensorData has a foreign key constraint on (device_id, sensor_type_id, sensor_index)
                // Sensor types: 1=Temperature, 2=Humidity, 3=Gas MQ1, 4=Gas MQ2, 5=Gas MQ3, 6=Dust Density
                ensureDeviceSensorsExist(conn, deviceId);
            } else {
                // Device exists, but ensure DeviceSensor entries exist (in case they were deleted)
                ensureDeviceSensorsExist(conn, deviceId);
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("[DeviceInfoDAO] Error updating updated_at for device: " + deviceId);
            e.printStackTrace();
            throw new RuntimeException("Error updating updated_at for device: " + deviceId, e);
        }
    }
    
    /**
     * Ensure DeviceSensor entries exist for all sensor types (1-6) for the given device.
     * This is required for the foreign key constraint on SensorData.
     * Also ensures SensorType entries exist (required for foreign key constraint).
     */
    private void ensureDeviceSensorsExist(Connection conn, String deviceId) throws SQLException {
        // CRITICAL: First ensure all SensorType entries exist (1-6)
        ensureSensorTypesExist(conn);
        
        // Check if DeviceSensor entries exist for this device
        String checkSql = "SELECT COUNT(*) FROM DeviceSensor WHERE device_id = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, deviceId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // DeviceSensor entries already exist
                    return;
                }
            }
        }
        
        // Insert DeviceSensor entries for all sensor types (1-6) with sensor_index=0
        // Schema: device_id (varchar), sensor_type_id (int), sensor_index (int), is_active (bit), installed_at (datetime2)
        String insertSensorSql = "INSERT INTO DeviceSensor (device_id, sensor_type_id, sensor_index, is_active, installed_at) " +
                                "VALUES (?, ?, 0, 1, GETDATE())";
        
        try (PreparedStatement insertPs = conn.prepareStatement(insertSensorSql)) {
            // Insert entries for sensor types 1-6
            for (int sensorTypeId = 1; sensorTypeId <= 6; sensorTypeId++) {
                insertPs.setString(1, deviceId);
                insertPs.setInt(2, sensorTypeId);
                try {
                    insertPs.executeUpdate();
                    System.out.println("[DeviceInfoDAO] Created DeviceSensor entry: device=" + deviceId + ", sensorTypeId=" + sensorTypeId);
                } catch (SQLException e) {
                    // Ignore duplicate key errors (entry might already exist from another thread)
                    if (!e.getMessage().contains("duplicate") && !e.getMessage().contains("UNIQUE")) {
                        System.err.println("[DeviceInfoDAO] Error creating DeviceSensor entry for device=" + deviceId + ", sensorTypeId=" + sensorTypeId);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Ensure all required SensorType entries exist in the database (1-6).
     * This is required for the foreign key constraint on SensorData.
     * Sensor types: 1=Temperature, 2=Humidity, 3=Gas MQ1, 4=Gas MQ2, 5=Gas MQ3, 6=Dust Density
     */
    private void ensureSensorTypesExist(Connection conn) throws SQLException {
        // Define all required sensor types
        Object[][] sensorTypes = {
            {1, "TEMP", "Temperature", "°C", "FLOAT"},
            {2, "HUM", "Humidity", "%", "FLOAT"},
            {3, "MQ1", "Gas MQ1", "ppm", "FLOAT"},
            {4, "MQ2", "Gas MQ2", "ppm", "FLOAT"},
            {5, "MQ3", "Gas MQ3", "ppm", "FLOAT"},
            {6, "DUST", "Dust Density", "µg/m³", "FLOAT"}
        };
        
        String checkSql = "SELECT COUNT(*) FROM SensorType WHERE sensor_type_id = ?";
        String insertSql = "SET IDENTITY_INSERT SensorType ON; " +
                          "INSERT INTO SensorType (sensor_type_id, sensor_code, name, unit, data_type, is_active, created_at) " +
                          "VALUES (?, ?, ?, ?, ?, 1, GETDATE()); " +
                          "SET IDENTITY_INSERT SensorType OFF;";
        
        for (Object[] sensorType : sensorTypes) {
            int sensorTypeId = (Integer) sensorType[0];
            String sensorCode = (String) sensorType[1];
            String name = (String) sensorType[2];
            String unit = (String) sensorType[3];
            String dataType = (String) sensorType[4];
            
            // Check if sensor type exists
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, sensorTypeId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Sensor type already exists
                        continue;
                    }
                }
            }
            
            // Insert sensor type with IDENTITY_INSERT
            // SQL Server requires IDENTITY_INSERT to be set per statement
            try (Statement stmt = conn.createStatement()) {
                // Enable IDENTITY_INSERT
                stmt.execute("SET IDENTITY_INSERT SensorType ON");
                
                // Insert the sensor type
                String insertStatement = String.format(
                    "INSERT INTO SensorType (sensor_type_id, sensor_code, name, unit, data_type, is_active, created_at) " +
                    "VALUES (%d, '%s', '%s', '%s', '%s', 1, GETDATE())",
                    sensorTypeId, sensorCode.replace("'", "''"), name.replace("'", "''"), 
                    unit.replace("'", "''"), dataType.replace("'", "''")
                );
                stmt.executeUpdate(insertStatement);
                
                // Disable IDENTITY_INSERT
                stmt.execute("SET IDENTITY_INSERT SensorType OFF");
                
                System.out.println("[DeviceInfoDAO] Created SensorType: id=" + sensorTypeId + ", name=" + name);
            } catch (SQLException e) {
                // Disable IDENTITY_INSERT in case of error
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET IDENTITY_INSERT SensorType OFF");
                } catch (SQLException ignored) {}
                
                // Ignore duplicate key errors or IDENTITY_INSERT errors
                if (!e.getMessage().contains("duplicate") && 
                    !e.getMessage().contains("UNIQUE") &&
                    !e.getMessage().contains("IDENTITY_INSERT") &&
                    !e.getMessage().contains("Violation of PRIMARY KEY")) {
                    System.err.println("[DeviceInfoDAO] Error creating SensorType: id=" + sensorTypeId + ", name=" + name + ", error=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete device by ID.
     */
    public boolean delete(Integer deviceId) {
        String sql = "DELETE FROM DeviceInfo WHERE device_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting device: " + deviceId, e);
        }
    }

    /**
     * Map ResultSet to DeviceInfo object.
     */
    private DeviceInfo mapResultSetToDeviceInfo(ResultSet rs) throws SQLException {
        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(rs.getInt("device_id"));
        device.setDeviceName(rs.getString("device_name"));
        device.setDeviceType(rs.getString("device_type"));
        device.setMacAddress(rs.getString("mac_address"));
        device.setIpAddress(rs.getString("ip_address"));
        device.setLocation(rs.getString("location"));
        device.setStatus(rs.getString("status"));
        
        Timestamp registeredAt = rs.getTimestamp("registered_at");
        if (registeredAt != null) {
            device.setRegisteredAt(registeredAt.toLocalDateTime());
        }
        
        Timestamp lastSeen = rs.getTimestamp("last_seen");
        if (lastSeen != null) {
            device.setLastSeen(lastSeen.toLocalDateTime());
        }
        
        return device;
    }
}
