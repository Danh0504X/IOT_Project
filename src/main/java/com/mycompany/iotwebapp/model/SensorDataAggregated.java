package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing SensorDataAggregated table.
 * Stores aggregated sensor data (hourly/daily averages).
 */
public class SensorDataAggregated {
    private Integer aggId;
    private Integer deviceId;
    private Integer sensorTypeId;
    private String aggregationType;
    private Double avgValue;
    private Double minValue;
    private Double maxValue;
    private Integer recordCount;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Constructors
    public SensorDataAggregated() {
    }

    // Getters and Setters
    public Integer getAggId() {
        return aggId;
    }

    public void setAggId(Integer aggId) {
        this.aggId = aggId;
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

    public String getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    @Override
    public String toString() {
        return "SensorDataAggregated{" +
                "aggId=" + aggId +
                ", deviceId=" + deviceId +
                ", sensorTypeId=" + sensorTypeId +
                ", aggregationType='" + aggregationType + '\'' +
                ", avgValue=" + avgValue +
                '}';
    }
}
