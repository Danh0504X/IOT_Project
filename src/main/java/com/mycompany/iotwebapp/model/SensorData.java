package com.mycompany.iotwebapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SensorData")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DataID")
    private Long dataId;

    @Column(name = "DeviceID", nullable = false)
    private Integer deviceId;

    @Column(name = "SensorTypeID", nullable = false)
    private Integer sensorTypeId;

    @Column(name = "Value", nullable = false)
    private Double value;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    // Không map DB
    @Transient
    private String sensorName;

    @Transient
    private String unit;

    public SensorData() {
    }

    // Dùng trong controller để tạo nhanh
    public SensorData(Integer deviceId, Integer sensorTypeId, Double value) {
        this.deviceId = deviceId;
        this.sensorTypeId = sensorTypeId;
        this.value = value;
        this.createdAt = LocalDateTime.now();
    }

    // Getter/setter
    public Long getDataId() { return dataId; }
    public void setDataId(Long dataId) { this.dataId = dataId; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public Integer getSensorTypeId() { return sensorTypeId; }
    public void setSensorTypeId(Integer sensorTypeId) { this.sensorTypeId = sensorTypeId; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Convenience method for backward compatibility
    public Double getSensorValue() { return value; }
    public void setSensorValue(Double sensorValue) { this.value = sensorValue; }
    
    // Convenience method for backward compatibility
    public LocalDateTime getTimestamp() { return createdAt; }
    public void setTimestamp(LocalDateTime timestamp) { this.createdAt = timestamp; }

    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String toString() {
        return "SensorData{" +
                "dataId=" + dataId +
                ", deviceId=" + deviceId +
                ", sensorTypeId=" + sensorTypeId +
                ", value=" + value +
                ", createdAt=" + createdAt +
                '}';
    }
}
