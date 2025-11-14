package com.mycompany.iotwebapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SensorData")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long dataId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "sensor_type_id", nullable = false)
    private Integer sensorTypeId;

    @Column(name = "sensor_index", nullable = false)
    private Integer sensorIndex;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "is_validated", nullable = false)
    private Boolean isValidated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sensor_value", nullable = false)
    private Double sensorValue;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Không map DB
    @Transient
    private String sensorName;

    @Transient
    private String unit;

    public SensorData() {
    }

    // Dùng trong controller để tạo nhanh
    public SensorData(String deviceId, Integer sensorTypeId, Double sensorValue) {
        this.deviceId    = deviceId;
        this.sensorTypeId = sensorTypeId;
        this.sensorValue = sensorValue;
        this.value       = sensorValue;
        this.sensorIndex = 0;
        this.isValidated = true;
    }

    // Getter/setter
    public Long getDataId() { return dataId; }
    public void setDataId(Long dataId) { this.dataId = dataId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Integer getSensorTypeId() { return sensorTypeId; }
    public void setSensorTypeId(Integer sensorTypeId) { this.sensorTypeId = sensorTypeId; }

    public Integer getSensorIndex() { return sensorIndex; }
    public void setSensorIndex(Integer sensorIndex) { this.sensorIndex = sensorIndex; }

    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Boolean getIsValidated() { return isValidated; }
    public void setIsValidated(Boolean isValidated) { this.isValidated = isValidated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getSensorValue() { return sensorValue; }
    public void setSensorValue(Double sensorValue) { this.sensorValue = sensorValue; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String toString() {
        return "SensorData{" +
                "dataId=" + dataId +
                ", deviceId='" + deviceId + '\'' +
                ", sensorTypeId=" + sensorTypeId +
                ", sensorValue=" + sensorValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
