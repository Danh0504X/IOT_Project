package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing DeviceCommand table.
 * Commands to be sent to ESP32 devices.
 */
public class DeviceCommand {
    private Integer commandId;
    private String deviceId;
    private String commandType;
    private String commandValue;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;

    // Constructors
    public DeviceCommand() {
    }

    public DeviceCommand(String deviceId, String commandType, String commandValue) {
        this.deviceId = deviceId;
        this.commandType = commandType;
        this.commandValue = commandValue;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandValue() {
        return commandValue;
    }

    public void setCommandValue(String commandValue) {
        this.commandValue = commandValue;
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

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    @Override
    public String toString() {
        return "DeviceCommand{" +
                "commandId=" + commandId +
                ", deviceId=" + deviceId +
                ", commandType='" + commandType + '\'' +
                ", commandValue='" + commandValue + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
