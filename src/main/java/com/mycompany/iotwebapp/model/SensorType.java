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
    @Column(name = "SensorTypeID")
    private Integer sensorTypeId;
    
    @Column(name = "SensorCode", length = 20)
    private String sensorCode;
    
    @Column(name = "SensorName", nullable = false, length = 50)
    private String sensorName;
    
    @Column(name = "Unit", length = 20)
    private String unit;
    
    @Column(name = "Description", length = 200)
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


    @Override
    public String toString() {
        return "SensorType{" +
                "sensorTypeId=" + sensorTypeId +
                ", sensorName='" + sensorName + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
