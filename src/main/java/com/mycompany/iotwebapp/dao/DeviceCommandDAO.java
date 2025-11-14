package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.DeviceCommand;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for DeviceCommand table - JDBC implementation.
 * Manages commands to be sent to ESP32 devices.
 */
public class DeviceCommandDAO {

    /**
     * Find command by ID.
     */
    public DeviceCommand findById(Integer commandId) {
        String sql = "SELECT command_id, device_id, command_type, command_value, status, created_at, executed_at " +
                     "FROM DeviceCommand WHERE command_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, commandId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDeviceCommand(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding command by ID: " + commandId, e);
        }
        return null;
    }

    /**
     * Find all commands for a device.
     */
    public List<DeviceCommand> findByDeviceId(String deviceId) {
        String sql = "SELECT command_id, device_id, command_type, command_value, status, created_at, executed_at " +
                     "FROM DeviceCommand WHERE device_id = ? ORDER BY created_at DESC";
        
        List<DeviceCommand> commands = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commands.add(mapResultSetToDeviceCommand(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding commands for device: " + deviceId, e);
        }
        
        return commands;
    }

    /**
     * Find pending commands for a device (to be sent to ESP32).
     */
    public List<DeviceCommand> findPendingByDeviceId(String deviceId) {
        String sql = "SELECT command_id, device_id, command_type, command_value, status, created_at, executed_at " +
                     "FROM DeviceCommand WHERE device_id = ? AND status = 'PENDING' ORDER BY created_at ASC";
        
        List<DeviceCommand> commands = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commands.add(mapResultSetToDeviceCommand(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending commands for device: " + deviceId, e);
        }
        
        return commands;
    }

    /**
     * Find commands by status.
     */
    public List<DeviceCommand> findByStatus(String status) {
        String sql = "SELECT command_id, device_id, command_type, command_value, status, created_at, executed_at " +
                     "FROM DeviceCommand WHERE status = ? ORDER BY created_at DESC";
        
        List<DeviceCommand> commands = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commands.add(mapResultSetToDeviceCommand(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding commands by status: " + status, e);
        }
        
        return commands;
    }

    /**
     * Insert new command.
     */
    public Integer insert(DeviceCommand command) {
        String sql = "INSERT INTO DeviceCommand (device_id, command_type, command_value, status, created_at) " +
                     "VALUES (?, ?, ?, ?, GETDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, command.getDeviceId());
            ps.setString(2, command.getCommandType());
            ps.setString(3, command.getCommandValue());
            ps.setString(4, command.getStatus() != null ? command.getStatus() : "PENDING");
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting command", e);
        }
        
        return null;
    }

    /**
     * Update command status to EXECUTED.
     */
    public boolean markAsExecuted(Integer commandId) {
        String sql = "UPDATE DeviceCommand SET status = 'EXECUTED', executed_at = GETDATE() WHERE command_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, commandId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error marking command as executed: " + commandId, e);
        }
    }

    /**
     * Update command status.
     */
    public boolean updateStatus(Integer commandId, String status) {
        String sql = "UPDATE DeviceCommand SET status = ? WHERE command_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, commandId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating command status", e);
        }
    }

    /**
     * Update command.
     */
    public boolean update(DeviceCommand command) {
        String sql = "UPDATE DeviceCommand SET device_id = ?, command_type = ?, command_value = ?, status = ? " +
                     "WHERE command_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, command.getDeviceId());
            ps.setString(2, command.getCommandType());
            ps.setString(3, command.getCommandValue());
            ps.setString(4, command.getStatus());
            ps.setInt(5, command.getCommandId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating command", e);
        }
    }

    /**
     * Delete command by ID.
     */
    public boolean delete(Integer commandId) {
        String sql = "DELETE FROM DeviceCommand WHERE command_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, commandId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting command: " + commandId, e);
        }
    }

    /**
     * Delete old executed commands (cleanup).
     */
    public int deleteExecutedOlderThan(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM DeviceCommand WHERE status = 'EXECUTED' AND executed_at < ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(cutoffDate));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting old executed commands", e);
        }
    }

    /**
     * Map ResultSet to DeviceCommand object.
     */
    private DeviceCommand mapResultSetToDeviceCommand(ResultSet rs) throws SQLException {
        DeviceCommand command = new DeviceCommand();
        command.setCommandId(rs.getInt("command_id"));
        command.setDeviceId(rs.getString("device_id"));
        command.setCommandType(rs.getString("command_type"));
        command.setCommandValue(rs.getString("command_value"));
        command.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            command.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp executedAt = rs.getTimestamp("executed_at");
        if (executedAt != null) {
            command.setExecutedAt(executedAt.toLocalDateTime());
        }
        
        return command;
    }
}

