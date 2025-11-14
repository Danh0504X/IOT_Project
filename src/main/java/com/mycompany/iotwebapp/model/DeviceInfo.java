package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing DeviceInfo table.
 */
public class DeviceInfo {
    private Integer deviceId;
    private String deviceName;
    private String deviceType;
    private String macAddress;
    private String ipAddress;
    private String location;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastSeen;

    // Constructors
    public DeviceInfo() {
    }

    public DeviceInfo(Integer deviceId, String deviceName, String deviceType) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    // Getters and Setters
    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceId=" + deviceId +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
