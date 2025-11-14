package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing SensorSettings table.
 * Contains threshold values for each sensor type per device.
 */
public class SensorSettings {
    private Long settingId;
    private String deviceId;
    private Integer sensorTypeId;
    private Double sensitivity;
    private Double calibrationFactor;
    private Double thresholdValue;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // Constructors
    public SensorSettings() {
    }

    public SensorSettings(String deviceId, Integer sensorTypeId, Double thresholdValue) {
        this.deviceId = deviceId;
        this.sensorTypeId = sensorTypeId;
        this.thresholdValue = thresholdValue;
    }

    // Getters and Setters
    public Long getSettingId() {
        return settingId;
    }

    public void setSettingId(Long settingId) {
        this.settingId = settingId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(Integer sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public Double getCalibrationFactor() {
        return calibrationFactor;
    }

    public void setCalibrationFactor(Double calibrationFactor) {
        this.calibrationFactor = calibrationFactor;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SensorSettings{" +
                "settingId=" + settingId +
                ", deviceId='" + deviceId + '\'' +
                ", sensorTypeId=" + sensorTypeId +
                ", sensitivity=" + sensitivity +
                ", calibrationFactor=" + calibrationFactor +
                ", thresholdValue=" + thresholdValue +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
