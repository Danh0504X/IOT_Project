package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.SensorType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for SensorType table - JDBC implementation.
 */
public class SensorTypeDAO {

    /**
     * Find sensor type by ID.
     */
    public SensorType findById(Integer sensorTypeId) {
        String sql = "SELECT sensor_type_id, sensor_code, name, unit, data_type, is_active, description FROM SensorType WHERE sensor_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SensorType st = new SensorType();
                    st.setSensorTypeId(rs.getInt("sensor_type_id"));
                    st.setSensorCode(rs.getString("sensor_code"));
                    st.setSensorName(rs.getString("name"));  // Read from 'name' column
                    st.setUnit(rs.getString("unit"));
                    st.setDataType(rs.getString("data_type"));
                    st.setIsActive(rs.getBoolean("is_active"));
                    st.setDescription(rs.getString("description"));
                    return st;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensor type by ID: " + sensorTypeId, e);
        }
        return null;
    }

    /**
     * Find sensor type by name.
     */
    public SensorType findByName(String sensorName) {
        String sql = "SELECT sensor_type_id, sensor_code, name, unit, data_type, is_active, description FROM SensorType WHERE name = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sensorName);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SensorType st = new SensorType();
                    st.setSensorTypeId(rs.getInt("sensor_type_id"));
                    st.setSensorCode(rs.getString("sensor_code"));
                    st.setSensorName(rs.getString("name"));  // Read from 'name' column
                    st.setUnit(rs.getString("unit"));
                    st.setDataType(rs.getString("data_type"));
                    st.setIsActive(rs.getBoolean("is_active"));
                    st.setDescription(rs.getString("description"));
                    return st;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensor type by name: " + sensorName, e);
        }
        return null;
    }

    /**
     * Find all sensor types.
     */
    public List<SensorType> findAll() {
        String sql = "SELECT sensor_type_id, sensor_code, name, unit, data_type, is_active, description FROM SensorType ORDER BY name";
        
        List<SensorType> sensorTypes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                SensorType st = new SensorType();
                st.setSensorTypeId(rs.getInt("sensor_type_id"));
                st.setSensorCode(rs.getString("sensor_code"));
                st.setSensorName(rs.getString("name"));  // Read from 'name' column
                st.setUnit(rs.getString("unit"));
                st.setDataType(rs.getString("data_type"));
                st.setIsActive(rs.getBoolean("is_active"));
                st.setDescription(rs.getString("description"));
                sensorTypes.add(st);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all sensor types", e);
        }
        
        return sensorTypes;
    }

    /**
     * Insert new sensor type.
     */
    public Integer insert(SensorType sensorType) {
        String sql = "INSERT INTO SensorType (sensor_name, unit, description) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, sensorType.getSensorName());
            ps.setString(2, sensorType.getUnit());
            ps.setString(3, sensorType.getDescription());
            
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting sensor type", e);
        }
        
        return null;
    }

    /**
     * Update sensor type.
     */
    public boolean update(SensorType sensorType) {
        String sql = "UPDATE SensorType SET sensor_name = ?, unit = ?, description = ? WHERE sensor_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sensorType.getSensorName());
            ps.setString(2, sensorType.getUnit());
            ps.setString(3, sensorType.getDescription());
            ps.setInt(4, sensorType.getSensorTypeId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating sensor type", e);
        }
    }

    /**
     * Delete sensor type by ID.
     */
    public boolean delete(Integer sensorTypeId) {
        String sql = "DELETE FROM SensorType WHERE sensor_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sensor type: " + sensorTypeId, e);
        }
    }
}
