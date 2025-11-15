package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.Threshold;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Threshold table - JDBC implementation.
 */
public class ThresholdDAO {

    /**
     * Find threshold by ID.
     */
    public Threshold findById(Integer thresholdId) {
        String sql = "SELECT ThresholdID, SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message " +
                     "FROM Threshold WHERE ThresholdID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, thresholdId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToThreshold(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding threshold by ID: " + thresholdId, e);
        }
        return null;
    }

    /**
     * Find all thresholds for a sensor type.
     */
    public List<Threshold> findBySensorTypeId(Integer sensorTypeId) {
        String sql = "SELECT ThresholdID, SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message " +
                     "FROM Threshold WHERE SensorTypeID = ? ORDER BY MinValue";
        
        List<Threshold> thresholds = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    thresholds.add(mapResultSetToThreshold(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding thresholds by sensor type: " + sensorTypeId, e);
        }
        
        return thresholds;
    }

    /**
     * Find all thresholds.
     */
    public List<Threshold> findAll() {
        String sql = "SELECT ThresholdID, SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message " +
                     "FROM Threshold ORDER BY SensorTypeID, MinValue";
        
        List<Threshold> thresholds = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                thresholds.add(mapResultSetToThreshold(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all thresholds", e);
        }
        
        return thresholds;
    }

    /**
     * Find threshold that matches a value for a sensor type.
     * Returns the threshold where value is between MinValue and MaxValue.
     */
    public Threshold findMatchingThreshold(Integer sensorTypeId, Float value) {
        String sql = "SELECT ThresholdID, SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message " +
                     "FROM Threshold WHERE SensorTypeID = ? AND ? >= MinValue AND ? < MaxValue";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            ps.setFloat(2, value);
            ps.setFloat(3, value);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToThreshold(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding matching threshold for sensor type: " + sensorTypeId + ", value: " + value, e);
        }
        return null;
    }

    /**
     * Insert new threshold.
     */
    public Integer insert(Threshold threshold) {
        String sql = "INSERT INTO Threshold (SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, threshold.getSensorTypeId());
            ps.setString(2, threshold.getLevelName());
            ps.setFloat(3, threshold.getMinValue());
            ps.setFloat(4, threshold.getMaxValue());
            ps.setInt(5, threshold.getAlertLevel());
            ps.setString(6, threshold.getMessage());
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting threshold", e);
        }
        
        return null;
    }

    /**
     * Update threshold using stored procedure (which logs changes).
     */
    public boolean update(Threshold threshold, Integer updatedBy) {
        String sql = "EXEC UpdateThreshold ?, ?, ?, ?, ?, ?, ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, threshold.getThresholdId());
            ps.setString(2, threshold.getLevelName());
            ps.setFloat(3, threshold.getMinValue());
            ps.setFloat(4, threshold.getMaxValue());
            ps.setInt(5, threshold.getAlertLevel());
            ps.setString(6, threshold.getMessage());
            ps.setInt(7, updatedBy);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating threshold", e);
        }
    }

    /**
     * Update threshold without logging (direct update).
     */
    public boolean updateDirect(Threshold threshold) {
        String sql = "UPDATE Threshold SET SensorTypeID = ?, LevelName = ?, MinValue = ?, MaxValue = ?, " +
                     "AlertLevel = ?, Message = ? WHERE ThresholdID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, threshold.getSensorTypeId());
            ps.setString(2, threshold.getLevelName());
            ps.setFloat(3, threshold.getMinValue());
            ps.setFloat(4, threshold.getMaxValue());
            ps.setInt(5, threshold.getAlertLevel());
            ps.setString(6, threshold.getMessage());
            ps.setInt(7, threshold.getThresholdId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating threshold", e);
        }
    }

    /**
     * Delete threshold by ID.
     */
    public boolean delete(Integer thresholdId) {
        String sql = "DELETE FROM Threshold WHERE ThresholdID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, thresholdId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting threshold: " + thresholdId, e);
        }
    }

    /**
     * Map ResultSet to Threshold object.
     */
    private Threshold mapResultSetToThreshold(ResultSet rs) throws SQLException {
        Threshold threshold = new Threshold();
        threshold.setThresholdId(rs.getInt("ThresholdID"));
        threshold.setSensorTypeId(rs.getInt("SensorTypeID"));
        threshold.setLevelName(rs.getString("LevelName"));
        threshold.setMinValue(rs.getFloat("MinValue"));
        threshold.setMaxValue(rs.getFloat("MaxValue"));
        threshold.setAlertLevel(rs.getInt("AlertLevel"));
        threshold.setMessage(rs.getString("Message"));
        return threshold;
    }
}

