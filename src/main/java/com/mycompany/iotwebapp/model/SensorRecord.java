package com.mycompany.iotwebapp.model;

import java.util.Date;

public class SensorRecord {
    private int deviceId;
    private double temperature;
    private double humidity;
    private double mq1, mq2, mq3, dust, aqi;
    private String aqiLevel, aqiColor;
    private int wifiSignal, uptime;
    private Date timestampAsDate;

    // GETTER + SETTER đầy đủ

    public SensorRecord(int deviceId, double temperature, double humidity, double mq1, double mq2, double mq3, double dust, double aqi, String aqiLevel, String aqiColor, int wifiSignal, int uptime, Date timestampAsDate) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.mq1 = mq1;
        this.mq2 = mq2;
        this.mq3 = mq3;
        this.dust = dust;
        this.aqi = aqi;
        this.aqiLevel = aqiLevel;
        this.aqiColor = aqiColor;
        this.wifiSignal = wifiSignal;
        this.uptime = uptime;
        this.timestampAsDate = timestampAsDate;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getMq1() {
        return mq1;
    }

    public void setMq1(double mq1) {
        this.mq1 = mq1;
    }

    public double getMq2() {
        return mq2;
    }

    public void setMq2(double mq2) {
        this.mq2 = mq2;
    }

    public double getMq3() {
        return mq3;
    }

    public void setMq3(double mq3) {
        this.mq3 = mq3;
    }

    public double getDust() {
        return dust;
    }

    public void setDust(double dust) {
        this.dust = dust;
    }

    public double getAqi() {
        return aqi;
    }

    public void setAqi(double aqi) {
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

    public int getWifiSignal() {
        return wifiSignal;
    }

    public void setWifiSignal(int wifiSignal) {
        this.wifiSignal = wifiSignal;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }

    public Date getTimestampAsDate() {
        return timestampAsDate;
    }

    public void setTimestampAsDate(Date timestampAsDate) {
        this.timestampAsDate = timestampAsDate;
    }
    
}