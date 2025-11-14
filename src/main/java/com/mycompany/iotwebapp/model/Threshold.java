package com.mycompany.iotwebapp.model;

public class Threshold {

    private int id;
    private int deviceId;
    private float tempLimit;
    private float humidityLimit;
    private float mq1Limit;
    private float mq2Limit;
    private float mq3Limit;
    private float dustLimit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public float getTempLimit() {
        return tempLimit;
    }

    public void setTempLimit(float tempLimit) {
        this.tempLimit = tempLimit;
    }

    public float getHumidityLimit() {
        return humidityLimit;
    }

    public void setHumidityLimit(float humidityLimit) {
        this.humidityLimit = humidityLimit;
    }

    public float getMq1Limit() {
        return mq1Limit;
    }

    public void setMq1Limit(float mq1Limit) {
        this.mq1Limit = mq1Limit;
    }

    public float getMq2Limit() {
        return mq2Limit;
    }

    public void setMq2Limit(float mq2Limit) {
        this.mq2Limit = mq2Limit;
    }

    public float getMq3Limit() {
        return mq3Limit;
    }

    public void setMq3Limit(float mq3Limit) {
        this.mq3Limit = mq3Limit;
    }

    public float getDustLimit() {
        return dustLimit;
    }

    public void setDustLimit(float dustLimit) {
        this.dustLimit = dustLimit;
    }
}

