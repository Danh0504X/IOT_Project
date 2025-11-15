package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.Threshold;
import com.mycompany.iotwebapp.util.FileLogger;

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
            
            System.out.println("[ThresholdDAO] Inserting new threshold: SensorTypeID=" + threshold.getSensorTypeId() + 
                             ", LevelName='" + threshold.getLevelName() + 
                             "', MinValue=" + threshold.getMinValue() + 
                             ", MaxValue=" + threshold.getMaxValue());
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer newId = rs.getInt(1);
                        System.out.println("[ThresholdDAO] ✅ Successfully inserted threshold with ID: " + newId);
                        return newId;
                    }
                }
            } else {
                System.err.println("[ThresholdDAO] ✗ Insert returned 0 rows affected");
            }
        } catch (SQLException e) {
            System.err.println("[ThresholdDAO] ✗ SQLException inserting threshold: " + e.getMessage());
            System.err.println("[ThresholdDAO] SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("Error inserting threshold. SensorTypeID: " + threshold.getSensorTypeId() + 
                                     ", LevelName: " + threshold.getLevelName() + 
                                     ", Error: " + e.getMessage(), e);
        }
        
        return null;
    }

    /**
     * Update threshold using stored procedure (which logs changes).
     * Falls back to direct update if stored procedure fails.
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
            
            int result = ps.executeUpdate();
            if (result > 0) {
                String successMsg = "✅ Successfully updated threshold ID: " + threshold.getThresholdId() + " using stored procedure";
                FileLogger.info("[ThresholdDAO] " + successMsg);
                System.out.println("[ThresholdDAO] " + successMsg);
                return true;
            } else {
                String warnMsg = "⚠ Stored procedure returned 0 rows affected for threshold ID: " + threshold.getThresholdId();
                FileLogger.warn("[ThresholdDAO] " + warnMsg);
                System.out.println("[ThresholdDAO] " + warnMsg);
                // Fallback to direct update
                return updateDirect(threshold);
            }
        } catch (SQLException e) {
            String errorMsg = "Error updating threshold using stored procedure (ID: " + threshold.getThresholdId() + "): " + e.getMessage();
            FileLogger.error("[ThresholdDAO] ✗ " + errorMsg, e);
            FileLogger.error("[ThresholdDAO] SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            System.err.println("[ThresholdDAO] ✗ " + errorMsg);
            System.err.println("[ThresholdDAO] SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            e.printStackTrace();
            
            // Fallback to direct update if stored procedure fails
            FileLogger.warn("[ThresholdDAO] Attempting fallback to direct update...");
            System.out.println("[ThresholdDAO] Attempting fallback to direct update...");
            try {
                boolean result = updateDirect(threshold);
                if (result) {
                    String successMsg = "✅ Fallback direct update succeeded for threshold ID: " + threshold.getThresholdId();
                    FileLogger.info("[ThresholdDAO] " + successMsg);
                    System.out.println("[ThresholdDAO] " + successMsg);
                } else {
                    String fallbackFailedMsg = "✗ Fallback direct update also failed for threshold ID: " + threshold.getThresholdId();
                    FileLogger.error("[ThresholdDAO] " + fallbackFailedMsg);
                    System.err.println("[ThresholdDAO] " + fallbackFailedMsg);
                }
                return result;
            } catch (Exception e2) {
                String fallbackErrorMsg = "Fallback direct update failed: " + e2.getMessage();
                FileLogger.error("[ThresholdDAO] ✗ " + fallbackErrorMsg, e2);
                System.err.println("[ThresholdDAO] ✗ " + fallbackErrorMsg);
                e2.printStackTrace();
                throw new RuntimeException("Error updating threshold (both stored procedure and direct update failed). " +
                                         "Original error: " + e.getMessage() + ". Fallback error: " + e2.getMessage(), e);
            }
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
            
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("[ThresholdDAO] ✅ Direct update succeeded for threshold ID: " + threshold.getThresholdId());
                return true;
            } else {
                System.err.println("[ThresholdDAO] ✗ Direct update returned 0 rows affected for threshold ID: " + threshold.getThresholdId() + 
                                 ". Threshold may not exist in database.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[ThresholdDAO] ✗ SQLException in direct update for threshold ID: " + threshold.getThresholdId());
            System.err.println("[ThresholdDAO] SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            System.err.println("[ThresholdDAO] Error Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating threshold directly. ThresholdID: " + threshold.getThresholdId() + 
                                     ", Error: " + e.getMessage(), e);
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

