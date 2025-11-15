package com.mycompany.iotwebapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing AQI_Result table.
 * Stores Air Quality Index calculation results.
 */
public class AQIResult {
    private Integer id;
    private Integer deviceId;
    private Float pm25;
    private Float co;
    private Float gas;
    private Float temperature;
    private Float humidity;
    private Float aqi;
    private String aqiLevel;
    private String aqiColor;
    private String mainPollutant;
    private LocalDateTime createdAt;

    // Constructors
    public AQIResult() {
    }

    public AQIResult(Integer deviceId, Float pm25, Float co, Float gas, Float temperature, Float humidity, Float aqi) {
        this.deviceId = deviceId;
        this.pm25 = pm25;
        this.co = co;
        this.gas = gas;
        this.temperature = temperature;
        this.humidity = humidity;
        this.aqi = aqi;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Float getPm25() {
        return pm25;
    }

    public void setPm25(Float pm25) {
        this.pm25 = pm25;
    }

    public Float getCo() {
        return co;
    }

    public void setCo(Float co) {
        this.co = co;
    }

    public Float getGas() {
        return gas;
    }

    public void setGas(Float gas) {
        this.gas = gas;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getAqi() {
        return aqi;
    }

    public void setAqi(Float aqi) {
        this.aqi = aqi;
    }

    public String getAqiLevel() {
        return aqiLevel;
    }

    public void setAqiLevel(String aqiLevel) {
        this.aqiLevel = aqiLevel;
    }

    public String getAqiColor() {
        return aqiColor;
    }

    public void setAqiColor(String aqiColor) {
        this.aqiColor = aqiColor;
    }

    public String getMainPollutant() {
        return mainPollutant;
    }

    public void setMainPollutant(String mainPollutant) {
        this.mainPollutant = mainPollutant;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AQIResult{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", aqi=" + aqi +
                ", aqiLevel='" + aqiLevel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

