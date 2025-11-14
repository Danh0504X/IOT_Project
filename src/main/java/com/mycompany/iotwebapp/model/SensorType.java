package com.mycompany.iotwebapp.model;

import jakarta.persistence.*;

/**
 * Model class representing SensorType table.
 */
@Entity
@Table(name = "SensorType")
public class SensorType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_type_id")
    private Integer sensorTypeId;
    
    @Column(name = "sensor_code", length = 32)
    private String sensorCode;
    
    @Column(name = "name", nullable = false, length = 128)
    private String sensorName;
    
    @Column(name = "unit", length = 20)
    private String unit;
    
    @Column(name = "data_type", length = 16)
    private String dataType;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "description", length = 255)
    private String description;

    // Constructors
    public SensorType() {
    }

    public SensorType(Integer sensorTypeId, String sensorName, String unit) {
        this.sensorTypeId = sensorTypeId;
        this.sensorName = sensorName;
        this.unit = unit;
    }

    // Getters and Setters
    public Integer getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(Integer sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSensorCode() {
        return sensorCode;
    }

    public void setSensorCode(String sensorCode) {
        this.sensorCode = sensorCode;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "SensorType{" +
                "sensorTypeId=" + sensorTypeId +
                ", sensorName='" + sensorName + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
