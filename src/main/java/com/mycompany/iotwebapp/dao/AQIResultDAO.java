package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.AQIResult;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for AQI_Result table - JDBC implementation.
 */
public class AQIResultDAO {

    /**
     * Insert a new AQI result.
     */
    public Integer insert(AQIResult aqiResult) {
        String sql = "INSERT INTO AQI_Result (DeviceID, PM25, CO, GAS, Temperature, Humidity, AQI, AQI_Level, AQI_Color, MainPollutant, CreatedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, aqiResult.getDeviceId());
            setFloatOrNull(ps, 2, aqiResult.getPm25());
            setFloatOrNull(ps, 3, aqiResult.getCo());
            setFloatOrNull(ps, 4, aqiResult.getGas());
            setFloatOrNull(ps, 5, aqiResult.getTemperature());
            setFloatOrNull(ps, 6, aqiResult.getHumidity());
            ps.setFloat(7, aqiResult.getAqi());
            setStringOrNull(ps, 8, aqiResult.getAqiLevel());
            setStringOrNull(ps, 9, aqiResult.getAqiColor());
            setStringOrNull(ps, 10, aqiResult.getMainPollutant());
            
            if (aqiResult.getCreatedAt() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(aqiResult.getCreatedAt()));
            } else {
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            }
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        aqiResult.setId(id);
                        System.out.println("[AQIResultDAO] Successfully inserted AQI result with ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AQIResultDAO] Error inserting AQI result: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inserting AQI result", e);
        }
        
        return null;
    }

    /**
     * Find latest AQI result for a device.
     */
    public AQIResult findLatestByDevice(Integer deviceId) {
        String sql = "SELECT TOP 1 ID, DeviceID, PM25, CO, GAS, Temperature, Humidity, AQI, AQI_Level, AQI_Color, MainPollutant, CreatedAt " +
                     "FROM AQI_Result WHERE DeviceID = ? ORDER BY CreatedAt DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAQIResult(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AQIResultDAO] Error finding latest AQI result: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error finding latest AQI result", e);
        }
        
        return null;
    }

    /**
     * Find AQI results for a device within a limit.
     */
    public List<AQIResult> findLatestByDevice(Integer deviceId, int limit) {
        String sql = "SELECT TOP (?) ID, DeviceID, PM25, CO, GAS, Temperature, Humidity, AQI, AQI_Level, AQI_Color, MainPollutant, CreatedAt " +
                     "FROM AQI_Result WHERE DeviceID = ? ORDER BY CreatedAt DESC";
        
        List<AQIResult> results = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limit);
            ps.setInt(2, deviceId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToAQIResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AQIResultDAO] Error finding AQI results: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error finding AQI results", e);
        }
        
        return results;
    }

    /**
     * Helper method to map ResultSet to AQIResult object.
     */
    private AQIResult mapResultSetToAQIResult(ResultSet rs) throws SQLException {
        AQIResult result = new AQIResult();
        result.setId(rs.getInt("ID"));
        result.setDeviceId(rs.getInt("DeviceID"));
        result.setPm25(getFloatOrNull(rs, "PM25"));
        result.setCo(getFloatOrNull(rs, "CO"));
        result.setGas(getFloatOrNull(rs, "GAS"));
        result.setTemperature(getFloatOrNull(rs, "Temperature"));
        result.setHumidity(getFloatOrNull(rs, "Humidity"));
        result.setAqi(rs.getFloat("AQI"));
        result.setAqiLevel(rs.getString("AQI_Level"));
        result.setAqiColor(rs.getString("AQI_Color"));
        result.setMainPollutant(rs.getString("MainPollutant"));
        
        Timestamp timestamp = rs.getTimestamp("CreatedAt");
        if (timestamp != null) {
            result.setCreatedAt(timestamp.toLocalDateTime());
        }
        
        return result;
    }

    /**
     * Helper method to set Float or NULL in PreparedStatement.
     */
    private void setFloatOrNull(PreparedStatement ps, int index, Float value) throws SQLException {
        if (value != null) {
            ps.setFloat(index, value);
        } else {
            ps.setNull(index, Types.FLOAT);
        }
    }

    /**
     * Helper method to set String or NULL in PreparedStatement.
     */
    private void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isEmpty()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.NVARCHAR);
        }
    }

    /**
     * Helper method to get Float or NULL from ResultSet.
     */
    private Float getFloatOrNull(ResultSet rs, String columnName) throws SQLException {
        float value = rs.getFloat(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }
}

