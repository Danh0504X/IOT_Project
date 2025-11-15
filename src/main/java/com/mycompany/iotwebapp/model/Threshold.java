package com.mycompany.iotwebapp.model;

/**
 * Model class representing Threshold table.
 */
public class Threshold {
    private Integer thresholdId;
    private Integer sensorTypeId;
    private String levelName;
    private Float minValue;
    private Float maxValue;
    private Integer alertLevel;
    private String message;

    // Constructors
    public Threshold() {
    }

    public Threshold(Integer thresholdId, Integer sensorTypeId, String levelName, Float minValue, Float maxValue, Integer alertLevel, String message) {
        this.thresholdId = thresholdId;
        this.sensorTypeId = sensorTypeId;
        this.levelName = levelName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.alertLevel = alertLevel;
        this.message = message;
    }

    // Getters and Setters
    public Integer getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Integer thresholdId) {
        this.thresholdId = thresholdId;
    }

    public Integer getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(Integer sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
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

    @Override
    public String toString() {
        return "Threshold{" +
                "thresholdId=" + thresholdId +
                ", sensorTypeId=" + sensorTypeId +
                ", levelName='" + levelName + '\'' +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", alertLevel=" + alertLevel +
                '}';
    }
}

