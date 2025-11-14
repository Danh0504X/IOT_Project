package com.mycompany.iotwebapp.model;

/**
 * DTO for ESP32 sensor data payload.
 * Receives all sensor readings in one JSON object.
 */
public class ESP32SensorPayload {
    private String deviceId;
    private Double temperature;
    private Double humidity;
    private Double mq135;  // MQ-135 sensor (mapped to mq1 in database)
    private Double mq7;    // MQ-7 sensor (mapped to mq2 in database)
    private Double mq2;    // MQ-2 sensor (mapped to mq3 in database)
    private Double dust;
    private Integer wifiSignal;
    private Long uptime;

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getMq135() {
        return mq135;
    }

    public void setMq135(Double mq135) {
        this.mq135 = mq135;
    }

    public Double getMq7() {
        return mq7;
    }

    public void setMq7(Double mq7) {
        this.mq7 = mq7;
    }

    public Double getMq2() {
        return mq2;
    }

    public void setMq2(Double mq2) {
        this.mq2 = mq2;
    }

    public Double getDust() {
        return dust;
    }

    public void setDust(Double dust) {
        this.dust = dust;
    }

    public Integer getWifiSignal() {
        return wifiSignal;
    }

    public void setWifiSignal(Integer wifiSignal) {
        this.wifiSignal = wifiSignal;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    @Override
    public String toString() {
        return "ESP32SensorPayload{" +
                "deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", mq135=" + mq135 +
                ", mq7=" + mq7 +
                ", mq2=" + mq2 +
                ", dust=" + dust +
                '}';
    }
}
