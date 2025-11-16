# 📊 ESP32 IoT System - Simple Flow Chart (ESP32 Focused)

## 🎯 ESP32 System Overview

```mermaid
graph TB
    Start([ESP32 Power On]) --> Init[Initialize System]
    Init --> LoadDefaults[Load Default Thresholds]
    LoadDefaults --> ConnectWiFi[Connect WiFi]
    ConnectWiFi --> WiFiOK{WiFi<br/>OK?}
    WiFiOK -->|No| Retry[Retry]
    Retry --> ConnectWiFi
    WiFiOK -->|Yes| FetchThresholds[Fetch Thresholds<br/>from Server]
    FetchThresholds --> MainLoop[Enter Main Loop]
    
    MainLoop --> Check5s{5 seconds<br/>elapsed?}
    Check5s -->|Yes| ReadSensors[Read All Sensors]
    Check5s -->|No| Check10s
    
    ReadSensors --> CheckThresholds[Check Thresholds]
    CheckThresholds --> ControlDevices[Control FAN & BUZZER]
    ControlDevices --> SendData[POST /api/data<br/>Send to Server]
    SendData --> Check10s
    
    Check10s{10 seconds<br/>elapsed?}
    Check10s -->|Yes| GetThresholds[GET /api/poll<br/>Get Thresholds]
    Check10s -->|No| CheckWiFi
    GetThresholds --> UpdateThresholds[Update Local<br/>Thresholds]
    UpdateThresholds --> CheckWiFi
    
    CheckWiFi{WiFi<br/>Connected?}
    CheckWiFi -->|No| Reconnect[Reconnect WiFi]
    Reconnect --> MainLoop
    CheckWiFi -->|Yes| Delay[Delay 200ms]
    Delay --> MainLoop
    
    style Start fill:#90EE90
    style MainLoop fill:#FFD700
    style CheckThresholds fill:#87CEEB
    style ControlDevices fill:#FF6B6B
    style SendData fill:#4ECDC4
    style GetThresholds fill:#95E1D3
```

---

## 🔍 ESP32 Setup Phase

```mermaid
flowchart TD
    Start([Power On]) --> Serial[Serial.begin 115200]
    Serial --> Pins[Configure GPIO Pins]
    Pins --> DHT[Initialize DHT Sensor]
    DHT --> Defaults[Load Default Thresholds]
    
    Defaults --> Temp[Temperature: 4 levels]
    Defaults --> Hum[Humidity: 3 levels]
    Defaults --> MQ135[MQ135: 4 levels]
    Defaults --> MQ2[MQ2 Gas: 4 levels]
    Defaults --> CO[CO MQ7: 4 levels]
    Defaults --> PM25[PM2.5: 5 levels]
    
    Temp --> WiFi[Connect WiFi]
    Hum --> WiFi
    MQ135 --> WiFi
    MQ2 --> WiFi
    CO --> WiFi
    PM25 --> WiFi
    
    WiFi --> Check{WiFi<br/>Connected?}
    Check -->|No| Retry[Retry 30 times]
    Retry --> WiFi
    Check -->|Yes| Poll[Fetch Thresholds<br/>First Time]
    Poll --> Loop[Enter Main Loop]
    
    style Start fill:#90EE90
    style Defaults fill:#FFD700
    style Loop fill:#87CEEB
```

---

## 🔄 ESP32 Main Loop - Detailed

```mermaid
flowchart TD
    LoopStart[Loop Start] --> GetTime[Get Current Time]
    GetTime --> Check5s{5s elapsed?}
    
    Check5s -->|Yes| ReadSensors[Read Sensors]
    Check5s -->|No| Check10s
    
    ReadSensors --> ReadDHT[Read DHT11<br/>Temp & Humidity]
    ReadSensors --> ReadMQ1[Read MQ1_PIN 32<br/>MQ-135]
    ReadSensors --> ReadMQ2[Read MQ2_PIN 33<br/>MQ-7 CO]
    ReadSensors --> ReadMQ3[Read MQ3_PIN 34<br/>MQ-2 Gas]
    ReadSensors --> ReadDust[Read GP2Y10<br/>PM2.5]
    
    ReadDHT --> CheckThresholds
    ReadMQ1 --> CheckThresholds
    ReadMQ2 --> CheckThresholds
    ReadMQ3 --> CheckThresholds
    ReadDust --> CheckThresholds
    
    CheckThresholds[checkThresholds Function] --> FindLevels[Find Alert Level<br/>for Each Sensor]
    FindLevels --> CalcAQI[Calculate AQI]
    CalcAQI --> FanLogic[FAN Control Logic]
    FanLogic --> BuzzerLogic[BUZZER Control Logic]
    BuzzerLogic --> CreateJSON[Create JSON Payload]
    CreateJSON --> PostData[POST /api/data]
    PostData --> Check10s
    
    Check10s{10s elapsed?}
    Check10s -->|Yes| GetThresholds[GET /api/poll]
    Check10s -->|No| CheckWiFi
    GetThresholds --> ParseResponse[Parse Response]
    ParseResponse --> UpdateThresholds[Update Thresholds]
    UpdateThresholds --> CheckWiFi
    
    CheckWiFi{WiFi OK?}
    CheckWiFi -->|No| Reconnect[Reconnect]
    Reconnect --> LoopStart
    CheckWiFi -->|Yes| Delay[Delay 200ms]
    Delay --> LoopStart
    
    style CheckThresholds fill:#87CEEB
    style FanLogic fill:#FF6B6B
    style BuzzerLogic fill:#FF0000
    style PostData fill:#4ECDC4
```

---

## 🎛️ Threshold Checking & Control Logic

```mermaid
flowchart TD
    Start[checkThresholds Called] --> ReadValues[Get Sensor Values]
    ReadValues --> ForEach[For Each Sensor]
    
    ForEach --> FindLevel[findAlertLevel]
    FindLevel --> LoopLevels{Loop Levels}
    LoopLevels --> CheckRange{Value in<br/>Range?}
    CheckRange -->|Yes| ReturnLevel[Return Alert Level]
    CheckRange -->|No| NextLevel[Next Level]
    NextLevel --> LoopLevels
    
    ReturnLevel --> TempLevel[Temp Alert]
    ReturnLevel --> HumLevel[Hum Alert]
    ReturnLevel --> MQ135Level[MQ135 Alert]
    ReturnLevel --> MQ2Level[MQ2 Alert]
    ReturnLevel --> COLevel[CO Alert]
    ReturnLevel --> PM25Level[PM2.5 Alert]
    
    TempLevel --> CalcAQI[Calculate AQI]
    HumLevel --> CalcAQI
    MQ135Level --> CalcAQI
    MQ2Level --> CalcAQI
    COLevel --> CalcAQI
    PM25Level --> CalcAQI
    
    CalcAQI --> FanDecision[FAN Decision]
    FanDecision --> FanRule1{AQI >= 75?}
    FanRule1 -->|Yes| FanON[FAN ON]
    FanRule1 -->|No| FanRule2{Any Alert >= 2?}
    FanRule2 -->|Yes| FanON
    FanRule2 -->|No| FanRule3{Hum >= 1?}
    FanRule3 -->|Yes| FanON
    FanRule3 -->|No| FanOFF[FAN OFF]
    
    FanON --> BuzzerDecision
    FanOFF --> BuzzerDecision
    
    BuzzerDecision[BUZZER Decision] --> BuzzerRule1{Any Alert == 3?}
    BuzzerRule1 -->|Yes| BuzzerON[BUZZER ON]
    BuzzerRule1 -->|No| BuzzerRule2{PM2.5 >= 2<br/>AND MQ >= 2?}
    BuzzerRule2 -->|Yes| BuzzerON
    BuzzerRule2 -->|No| BuzzerRule3{CO >= 2?}
    BuzzerRule3 -->|Yes| BuzzerON
    BuzzerRule3 -->|No| BuzzerRule4{Gas >= 2<br/>AND Hum >= 1?}
    BuzzerRule4 -->|Yes| BuzzerON
    BuzzerRule4 -->|No| BuzzerRule5{Temp >= 2<br/>AND PM2.5 >= 1<br/>AND AQI >= 100?}
    BuzzerRule5 -->|Yes| BuzzerON
    BuzzerRule5 -->|No| BuzzerOFF[BUZZER OFF]
    
    BuzzerON --> Execute[Execute Control]
    BuzzerOFF --> Execute
    Execute --> End[End]
    
    style FanON fill:#FF6B6B
    style FanOFF fill:#51CF66
    style BuzzerON fill:#FF0000
    style BuzzerOFF fill:#51CF66
```

---

## 📡 Network Communication (Simplified)

```mermaid
sequenceDiagram
    participant ESP32
    participant Server as Web Server
    participant DB as Database
    
    Note over ESP32: Startup
    ESP32->>Server: Connect WiFi
    ESP32->>Server: GET /api/poll (First time)
    Server->>DB: Query Thresholds
    DB-->>Server: Threshold Data
    Server-->>ESP32: Multi-level Thresholds JSON
    
    Note over ESP32: Main Loop (Every 5s)
    loop Every 5 seconds
        ESP32->>ESP32: Read Sensors
        ESP32->>ESP32: Check Thresholds
        ESP32->>ESP32: Control FAN/BUZZER
        ESP32->>Server: POST /api/data (JSON)
        Server->>DB: Save Sensor Data
        Server-->>ESP32: {"status":"ok"}
    end
    
    Note over ESP32: Poll Loop (Every 10s)
    loop Every 10 seconds
        ESP32->>Server: GET /api/poll
        Server->>DB: Query Latest Thresholds
        DB-->>Server: Threshold Data
        Server-->>ESP32: Updated Thresholds JSON
        ESP32->>ESP32: Update Local Thresholds
    end
```

---

## ⏱️ Timing Diagram

```
Time (seconds)
│
0s ──────────────────────────────────────────────────────────────
   │ ESP32 Startup
   │ ├─► Initialize
   │ ├─► Load Default Thresholds
   │ └─► Connect WiFi
   │
   │ IF WiFi Connected:
   │ └─► GET /api/poll (First time)
   │
5s ──────────────────────────────────────────────────────────────
   │ ├─► Read Sensors
   │ ├─► Check Thresholds
   │ ├─► Control FAN/BUZZER
   │ └─► POST /api/data
   │
10s ─────────────────────────────────────────────────────────────
   │ ├─► Read Sensors
   │ ├─► Check Thresholds
   │ ├─► Control FAN/BUZZER
   │ ├─► POST /api/data
   │ └─► GET /api/poll (Update thresholds)
   │
15s ─────────────────────────────────────────────────────────────
   │ ├─► Read Sensors
   │ ├─► Check Thresholds
   │ ├─► Control FAN/BUZZER
   │ └─► POST /api/data
   │
20s ─────────────────────────────────────────────────────────────
   │ ├─► Read Sensors
   │ ├─► Check Thresholds
   │ ├─► Control FAN/BUZZER
   │ ├─► POST /api/data
   │ └─► GET /api/poll (Update thresholds)
   │
... (Repeats every 5s for data, every 10s for polling)
```

---

## 🎯 ESP32 Key Functions

### 1. **setup()** - Initialization
- Configure GPIO pins
- Initialize DHT sensor
- Load default thresholds
- Connect WiFi
- Fetch thresholds from server (first time)

### 2. **loop()** - Main Loop
- Every 5s: Read sensors → Check thresholds → Control devices → Send data
- Every 10s: Poll server for threshold updates
- Continuously: Monitor WiFi connection

### 3. **readAndSend()** - Sensor Reading & Transmission
- Read all sensors (DHT11, MQ sensors, Dust sensor)
- Call checkThresholds()
- Create JSON payload
- Send to server via POST /api/data

### 4. **checkThresholds()** - Core Logic
- Find alert level for each sensor
- Calculate AQI
- Apply FAN control rules
- Apply BUZZER control rules
- Execute device control

### 5. **findAlertLevel()** - Threshold Matching
- Loop through threshold levels
- Find matching range for sensor value
- Return alert level, level name, and message

### 6. **calculateAQI()** - Air Quality Index
- Formula: AQI = (pm25/500)*500 + (mq7/2000)*200 + (mq135/10000)*150

### 7. **fetchThresholds()** - Get Updates from Server
- GET /api/poll?deviceId=DEVICE001
- Parse JSON response
- Update local threshold structure

### 8. **sendToServer()** - Send Data
- POST /api/data with JSON payload
- Handle WiFi disconnection gracefully

---

## 📊 ESP32 Data Structures

```
ThresholdLevel {
  String levelName;      // "Bình thường", "Nguy hiểm"
  float minValue;        // Minimum value
  float maxValue;        // Maximum value
  int alertLevel;        // 0=OK, 1=Chú ý, 2=Nguy hiểm, 3=Khẩn cấp
  String message;        // Alert message
}

SensorThresholds {
  ThresholdLevel levels[5];  // Max 5 levels per sensor
  int levelCount;            // Actual number of levels
}

ThresholdSettings {
  SensorThresholds temperature;  // 4 levels
  SensorThresholds humidity;     // 3 levels
  SensorThresholds mq2;          // 4 levels (Gas/LPG)
  SensorThresholds co;           // 4 levels (CO)
  SensorThresholds mq135;        // 4 levels (Air Quality)
  SensorThresholds pm25;         // 5 levels (Dust)
  bool loaded;                   // Loaded from server?
}
```

---

## 🔧 ESP32 Pin Configuration

| Pin | Function | Type | Description |
|-----|----------|------|-------------|
| 12  | DUST_LED_PIN | OUTPUT | Dust sensor LED control |
| 13  | DHTPIN | INPUT | DHT11 sensor data |
| 18  | FAN_PIN | OUTPUT | Fan control (LOW=ON, HIGH=OFF) |
| 19  | BUZZER_PIN | OUTPUT | Buzzer control (LOW=ON, HIGH=OFF) |
| 32  | MQ1_PIN | INPUT | MQ-135 (Air Quality) analog |
| 33  | MQ2_PIN | INPUT | MQ-7 (CO) analog |
| 34  | MQ3_PIN | INPUT | MQ-2 (Gas/LPG) analog |
| 35  | DUST_VOUT_PIN | INPUT | Dust sensor analog output |

---

## 🎯 Control Rules Summary

### FAN Control Rules:
1. **AQI >= 75** → FAN ON
2. **Any sensor AlertLevel >= 2** (except humidity) → FAN ON
3. **Humidity AlertLevel >= 1** → FAN ON

### BUZZER Control Rules:
1. **Any sensor AlertLevel == 3** (Emergency) → BUZZER ON
2. **Humidity AlertLevel >= 1** → BUZZER ON
3. **PM2.5 Alert >= 2 AND any MQ Alert >= 2** → BUZZER ON
4. **CO Alert >= 2** → BUZZER ON
5. **(MQ135 OR MQ2) Alert >= 2 AND humidity Alert >= 1** → BUZZER ON
6. **Temp Alert >= 2 AND PM2.5 Alert >= 1 AND AQI >= 100** → BUZZER ON

---

## 📝 Notes

- **ESP32 operates autonomously**: Can work without server connection using default thresholds
- **Server role**: Only for data storage and threshold updates (optional)
- **Real-time control**: All threshold checking and device control happens on ESP32
- **Network resilience**: ESP32 continues operation even if WiFi disconnects
- **Default thresholds**: Ensured system always has thresholds to work with

---

*Flow chart này tập trung hoàn toàn vào ESP32, với server và database chỉ là phần hỗ trợ đơn giản.*

