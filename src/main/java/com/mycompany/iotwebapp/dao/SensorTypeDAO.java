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
        String sql = "SELECT SensorTypeID, SensorName, SensorCode, Unit, Description FROM SensorType WHERE SensorTypeID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SensorType st = new SensorType();
                    st.setSensorTypeId(rs.getInt("SensorTypeID"));
                    st.setSensorCode(rs.getString("SensorCode"));
                    st.setSensorName(rs.getString("SensorName"));
                    st.setUnit(rs.getString("Unit"));
                    st.setDescription(rs.getString("Description"));
                    return st;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensor type by ID: " + sensorTypeId, e);
        }
        return null;
    }

    /**
     * Find sensor type by code.
     */
    public SensorType findByCode(String sensorCode) {
        String sql = "SELECT SensorTypeID, SensorName, SensorCode, Unit, Description FROM SensorType WHERE SensorCode = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sensorCode);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SensorType st = new SensorType();
                    st.setSensorTypeId(rs.getInt("SensorTypeID"));
                    st.setSensorCode(rs.getString("SensorCode"));
                    st.setSensorName(rs.getString("SensorName"));
                    st.setUnit(rs.getString("Unit"));
                    st.setDescription(rs.getString("Description"));
                    return st;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sensor type by code: " + sensorCode, e);
        }
        return null;
    }

    /**
     * Find sensor type by name.
     */
    public SensorType findByName(String sensorName) {
        String sql = "SELECT SensorTypeID, SensorName, SensorCode, Unit, Description FROM SensorType WHERE SensorName = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sensorName);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SensorType st = new SensorType();
                    st.setSensorTypeId(rs.getInt("SensorTypeID"));
                    st.setSensorCode(rs.getString("SensorCode"));
                    st.setSensorName(rs.getString("SensorName"));
                    st.setUnit(rs.getString("Unit"));
                    st.setDescription(rs.getString("Description"));
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
        String sql = "SELECT SensorTypeID, SensorName, SensorCode, Unit, Description FROM SensorType ORDER BY SensorName";
        
        List<SensorType> sensorTypes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                SensorType st = new SensorType();
                st.setSensorTypeId(rs.getInt("SensorTypeID"));
                st.setSensorCode(rs.getString("SensorCode"));
                st.setSensorName(rs.getString("SensorName"));
                st.setUnit(rs.getString("Unit"));
                st.setDescription(rs.getString("Description"));
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
        String sql = "INSERT INTO SensorType (SensorName, SensorCode, Unit, Description) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, sensorType.getSensorName());
            ps.setString(2, sensorType.getSensorCode());
            ps.setString(3, sensorType.getUnit());
            ps.setString(4, sensorType.getDescription());
            
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
        String sql = "UPDATE SensorType SET SensorName = ?, SensorCode = ?, Unit = ?, Description = ? WHERE SensorTypeID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sensorType.getSensorName());
            ps.setString(2, sensorType.getSensorCode());
            ps.setString(3, sensorType.getUnit());
            ps.setString(4, sensorType.getDescription());
            ps.setInt(5, sensorType.getSensorTypeId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating sensor type", e);
        }
    }

    /**
     * Delete sensor type by ID.
     */
    public boolean delete(Integer sensorTypeId) {
        String sql = "DELETE FROM SensorType WHERE SensorTypeID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sensorTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sensor type: " + sensorTypeId, e);
        }
    }
}
