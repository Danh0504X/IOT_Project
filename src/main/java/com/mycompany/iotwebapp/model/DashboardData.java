package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for dashboard display.
 * Aggregates sensor readings into a flat structure for easy JSP rendering.
 */
public class DashboardData {
    private Integer deviceId;
    private LocalDateTime timestamp;
    private Map<String, Double> sensorValues;

    public DashboardData() {
        this.sensorValues = new HashMap<>();
    }

    public DashboardData(Integer deviceId, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.sensorValues = new HashMap<>();
    }

    // Getters and Setters
    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    
    // Convenience method for backward compatibility
    public void setDeviceId(String deviceId) {
        try {
            this.deviceId = Integer.parseInt(deviceId);
        } catch (NumberFormatException e) {
            this.deviceId = null;
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Double> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(Map<String, Double> sensorValues) {
        this.sensorValues = sensorValues;
    }

    // Convenience methods for specific sensors
    public void addSensorValue(String sensorName, Double value) {
        this.sensorValues.put(sensorName, value);
    }

    public Double getTemperature() {
        return sensorValues.getOrDefault("temperature", 0.0);
    }

    public Double getHumidity() {
        return sensorValues.getOrDefault("humidity", 0.0);
    }

    public Double getMq1() {
        return sensorValues.getOrDefault("mq1", 0.0);
    }

    public Double getMq2() {
        return sensorValues.getOrDefault("mq2", 0.0);
    }

    public Double getMq3() {
        return sensorValues.getOrDefault("mq3", 0.0);
    }

    public Double getDust() {
        return sensorValues.getOrDefault("dust", 0.0);
    }

    public Integer getWifiSignal() {
        Double value = sensorValues.get("wifi_signal");
        return value != null ? value.intValue() : 0;
    }

    public Integer getUptime() {
        Double value = sensorValues.get("uptime");
        return value != null ? value.intValue() : 0;
    }

    /**
     * Convert LocalDateTime to java.util.Date for JSP fmt:formatDate tag compatibility.
     */
    public java.util.Date getTimestampAsDate() {
        if (timestamp == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(timestamp);
    }

    @Override
    public String toString() {
        return "DashboardData{" +
                "deviceId=" + deviceId +
                ", timestamp=" + timestamp +
                ", sensorValues=" + sensorValues +
                '}';
    }
}
