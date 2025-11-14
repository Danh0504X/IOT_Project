package com.mycompany.iotwebapp.model;

/**
 * Model class representing DeviceSensor table.
 * Maps which sensors are attached to which devices.
 */
public class DeviceSensor {
    private Integer deviceSensorId;
    private Integer deviceId;
    private Integer sensorTypeId;
    private String sensorPin;
    private String status;

    // Constructors
    public DeviceSensor() {
    }

    public DeviceSensor(Integer deviceId, Integer sensorTypeId) {
        this.deviceId = deviceId;
        this.sensorTypeId = sensorTypeId;
    }

    // Getters and Setters
    public Integer getDeviceSensorId() {
        return deviceSensorId;
    }

    public void setDeviceSensorId(Integer deviceSensorId) {
        this.deviceSensorId = deviceSensorId;
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

    public String getSensorPin() {
        return sensorPin;
    }

    public void setSensorPin(String sensorPin) {
        this.sensorPin = sensorPin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeviceSensor{" +
                "deviceSensorId=" + deviceSensorId +
                ", deviceId=" + deviceId +
                ", sensorTypeId=" + sensorTypeId +
                ", status='" + status + '\'' +
                '}';
    }
}
