package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.DeviceInfo;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Device table - JDBC implementation.
 */
public class DeviceInfoDAO {

    /**
     * Find device by ID.
     */
    public DeviceInfo findById(Integer deviceId) {
        String sql = "SELECT DeviceID, DeviceName, Location, Latitude, Longitude, Status, BatteryLevel, InstallDate, LastUpdate " +
                     "FROM Device WHERE DeviceID = ?";
        
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
        String sql = "SELECT DeviceID, DeviceName, Location, Latitude, Longitude, Status, BatteryLevel, InstallDate, LastUpdate " +
                     "FROM Device ORDER BY DeviceName";
        
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
        String sql = "SELECT DeviceID, DeviceName, Location, Latitude, Longitude, Status, BatteryLevel, InstallDate, LastUpdate " +
                     "FROM Device WHERE Status = ?";
        
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
        String sql = "INSERT INTO Device (DeviceName, Location, Latitude, Longitude, Status, BatteryLevel, InstallDate, LastUpdate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getLocation());
            if (device.getLatitude() != null) {
                ps.setBigDecimal(3, device.getLatitude());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (device.getLongitude() != null) {
                ps.setBigDecimal(4, device.getLongitude());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, device.getStatus() != null ? device.getStatus() : "Active");
            if (device.getBatteryLevel() != null) {
                ps.setFloat(6, device.getBatteryLevel());
            } else {
                ps.setNull(6, Types.FLOAT);
            }
            if (device.getInstallDate() != null) {
                ps.setDate(7, Date.valueOf(device.getInstallDate()));
            } else {
                ps.setDate(7, Date.valueOf(LocalDate.now()));
            }
            
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
        String sql = "UPDATE Device SET DeviceName = ?, Location = ?, Latitude = ?, Longitude = ?, " +
                     "Status = ?, BatteryLevel = ?, LastUpdate = GETDATE() WHERE DeviceID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, device.getDeviceName());
            ps.setString(2, device.getLocation());
            if (device.getLatitude() != null) {
                ps.setBigDecimal(3, device.getLatitude());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (device.getLongitude() != null) {
                ps.setBigDecimal(4, device.getLongitude());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, device.getStatus());
            if (device.getBatteryLevel() != null) {
                ps.setFloat(6, device.getBatteryLevel());
            } else {
                ps.setNull(6, Types.FLOAT);
            }
            ps.setInt(7, device.getDeviceId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating device", e);
        }
    }

    /**
     * Update device LastUpdate timestamp.
     */
    public boolean updateLastSeen(Integer deviceId) {
        String updateSql = "UPDATE Device SET LastUpdate = GETDATE() WHERE DeviceID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            
            ps.setInt(1, deviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DeviceInfoDAO] Error updating LastUpdate for device: " + deviceId);
            e.printStackTrace();
            throw new RuntimeException("Error updating LastUpdate for device: " + deviceId, e);
        }
    }

    /**
     * Delete device by ID.
     */
    public boolean delete(Integer deviceId) {
        String sql = "DELETE FROM Device WHERE DeviceID = ?";
        
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
        device.setDeviceId(rs.getInt("DeviceID"));
        device.setDeviceName(rs.getString("DeviceName"));
        device.setLocation(rs.getString("Location"));
        
        BigDecimal lat = rs.getBigDecimal("Latitude");
        if (rs.wasNull()) lat = null;
        device.setLatitude(lat);
        
        BigDecimal lon = rs.getBigDecimal("Longitude");
        if (rs.wasNull()) lon = null;
        device.setLongitude(lon);
        
        device.setStatus(rs.getString("Status"));
        
        Float battery = rs.getFloat("BatteryLevel");
        if (rs.wasNull()) battery = null;
        device.setBatteryLevel(battery);
        
        Date installDate = rs.getDate("InstallDate");
        if (installDate != null) {
            device.setInstallDate(installDate.toLocalDate());
        }
        
        Timestamp lastUpdate = rs.getTimestamp("LastUpdate");
        if (lastUpdate != null) {
            device.setLastUpdate(lastUpdate.toLocalDateTime());
        }
        
        return device;
    }
}
