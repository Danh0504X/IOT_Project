package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing Alert table.
 */
public class Alert {
    private Integer alertId;
    private Integer deviceId;
    private Integer sensorTypeId;
    private Integer alertLevel;
    private String message;
    private Float sensorValue;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer resolvedBy;
    
    // Transient fields for display
    private String sensorName;
    private String deviceName;

    // Constructors
    public Alert() {
    }

    public Alert(Integer deviceId, Integer sensorTypeId, Integer alertLevel, String message, Float sensorValue) {
        this.deviceId = deviceId;
        this.sensorTypeId = sensorTypeId;
        this.alertLevel = alertLevel;
        this.message = message;
        this.sensorValue = sensorValue;
        this.status = "Mới";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(Integer sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public Integer getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Float getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(Float sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "alertId=" + alertId +
                ", deviceId=" + deviceId +
                ", sensorTypeId=" + sensorTypeId +
                ", alertLevel=" + alertLevel +
                ", message='" + message + '\'' +
                ", sensorValue=" + sensorValue +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

