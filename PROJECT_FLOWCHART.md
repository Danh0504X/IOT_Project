# 📊 IoT Web Application - Complete Flow Chart

## 🎯 Tổng quan hệ thống

Hệ thống IoT Web Application bao gồm 3 thành phần chính:
1. **ESP32 Device** - Thu thập dữ liệu cảm biến và điều khiển thiết bị
2. **Java Web Server** - Xử lý dữ liệu, quản lý ngưỡng, hiển thị dashboard
3. **SQL Server Database** - Lưu trữ dữ liệu và cấu hình

---

## 🔄 FLOW CHART TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         IOT WEB APPLICATION SYSTEM                       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
            ┌───────▼────────┐              ┌───────▼────────┐
            │   ESP32 DEVICE │              │  WEB BROWSER   │
            │   (Hardware)   │              │   (Frontend)   │
            └───────┬────────┘              └───────┬────────┘
                    │                               │
                    │ HTTP POST                     │ HTTP GET/POST
                    │ /api/data                     │ /dashboard, /admin/*
                    │                               │
            ┌───────▼───────────────────────────────▼────────┐
            │         JAVA WEB SERVER (Tomcat)               │
            │  ┌──────────────────────────────────────────┐  │
            │  │  Controllers (Servlets)                  │  │
            │  │  - DataServlet (/api/data)               │  │
            │  │  - PollServlet (/api/poll)               │  │
            │  │  - DashboardServlet (/dashboard)         │  │
            │  │  - ThresholdAdminServlet (/admin/*)      │  │
            │  │  - LoginServlet (/login)                 │  │
            │  └──────────────┬───────────────────────────┘  │
            │                 │                              │
            │  ┌──────────────▼───────────────────────────┐  │
            │  │  Services (Business Logic)               │  │
            │  │  - SensorService                         │  │
            │  │  - ThresholdService                      │  │
            │  └──────────────┬───────────────────────────┘  │
            │                 │                              │
            │  ┌──────────────▼───────────────────────────┐  │
            │  │  DAO (Data Access Objects)               │  │
            │  │  - SensorDataDAO                         │  │
            │  │  - ThresholdDAO                          │  │
            │  │  - DeviceInfoDAO                         │  │
            │  │  - AQIResultDAO                          │  │
            │  └──────────────┬───────────────────────────┘  │
            └─────────────────┼──────────────────────────────┘
                              │
                              │ JDBC
                              │
                    ┌─────────▼─────────┐
                    │  SQL SERVER DB    │
                    │  - DeviceInfo     │
                    │  - SensorData     │
                    │  - Threshold      │
                    │  - AQIResult      │
                    │  - DeviceCommand  │
                    └───────────────────┘
```

---

## 📱 FLOW 1: ESP32 INITIALIZATION & SETUP

```
┌─────────────────────────────────────────────────────────────────┐
│                    ESP32 STARTUP SEQUENCE                        │
└─────────────────────────────────────────────────────────────────┘

START
  │
  ├─► Serial.begin(115200)
  │
  ├─► PinMode Configuration
  │   ├─► DUST_LED_PIN (12) → OUTPUT
  │   ├─► DUST_VOUT_PIN (35) → INPUT
  │   ├─► MQ1_PIN (32) → INPUT (MQ-135)
  │   ├─► MQ2_PIN (33) → INPUT (MQ-7)
  │   ├─► MQ3_PIN (34) → INPUT (MQ-2)
  │   ├─► FAN_PIN (18) → OUTPUT
  │   └─► BUZZER_PIN (19) → OUTPUT
  │
  ├─► Initialize DHT Sensor
  │   └─► dht.begin()
  │
  ├─► Initialize Default Thresholds
  │   └─► initDefaultThresholds()
  │       ├─► Temperature: 4 levels (Lạnh, Bình thường, Nóng, Rất nguy hiểm)
  │       ├─► Humidity: 3 levels (Khô, Bình thường, Ẩm)
  │       ├─► MQ135: 4 levels (Tốt, Trung bình, Nguy hiểm, Rất nguy hiểm)
  │       ├─► MQ2 (Gas/LPG): 4 levels
  │       ├─► CO (MQ-7): 4 levels
  │       └─► PM2.5: 5 levels
  │
  ├─► Connect WiFi
  │   └─► connectWiFi()
  │       ├─► WiFi.mode(WIFI_STA)
  │       ├─► WiFi.begin(SSID, PASSWORD)
  │       └─► Wait for connection (max 30 attempts)
  │
  ├─► IF WiFi Connected:
  │   └─► fetchThresholds() [First time]
  │       └─► GET /api/poll?deviceId=DEVICE001
  │
  └─► ENTER MAIN LOOP
```

---

## 🔄 FLOW 2: ESP32 MAIN LOOP (Continuous Operation)

```
┌─────────────────────────────────────────────────────────────────┐
│                    ESP32 MAIN LOOP (loop())                      │
└─────────────────────────────────────────────────────────────────┘

LOOP START
  │
  ├─► Check: currentMillis - lastSend >= SEND_INTERVAL (5000ms)?
  │   │
  │   └─► YES → readAndSend()
  │       │
  │       ├─► Read Sensors
  │       │   ├─► DHT11: temperature, humidity
  │       │   ├─► MQ1_PIN (32): mq135 (analogRead)
  │       │   ├─► MQ2_PIN (33): mq7 (analogRead)
  │       │   ├─► MQ3_PIN (34): mq2 (analogRead)
  │       │   └─► GP2Y10: pm25 (dust sensor)
  │       │
  │       ├─► checkThresholds()
  │       │   │
  │       │   ├─► For each sensor:
  │       │   │   └─► findAlertLevel(value, sensorThresholds)
  │       │   │       └─► Returns: alertLevel (0-3), levelName, message
  │       │   │
  │       │   ├─► Calculate AQI
  │       │   │   └─► calculateAQI(pm25, mq7, mq135)
  │       │   │
  │       │   ├─► FAN Control Logic
  │       │   │   ├─► IF AQI >= 75 → FAN ON
  │       │   │   ├─► IF any sensor AlertLevel >= 2 (except humidity) → FAN ON
  │       │   │   └─► IF humidity AlertLevel >= 1 → FAN ON
  │       │   │
  │       │   └─► BUZZER Control Logic
  │       │       ├─► IF any sensor AlertLevel == 3 → BUZZER ON
  │       │       ├─► IF humidity AlertLevel >= 1 → BUZZER ON
  │       │       ├─► IF PM2.5 Alert >= 2 AND any MQ Alert >= 2 → BUZZER ON
  │       │       ├─► IF CO Alert >= 2 → BUZZER ON
  │       │       ├─► IF (MQ135 OR MQ2) Alert >= 2 AND humidity Alert >= 1 → BUZZER ON
  │       │       └─► IF temp Alert >= 2 AND PM2.5 Alert >= 1 AND AQI >= 100 → BUZZER ON
  │       │
  │       ├─► Create JSON Payload
  │       │   {
  │       │     "deviceId": "DEVICE001",
  │       │     "temperature": 25.5,
  │       │     "humidity": 60.0,
  │       │     "mq135": 250,
  │       │     "mq7": 280,
  │       │     "mq2": 550,
  │       │     "dust": 35.0
  │       │   }
  │       │
  │       └─► POST /api/data
  │           └─► sendToServer(jsonPayload)
  │
  ├─► Check: currentMillis - lastPoll >= POLL_INTERVAL (10000ms)?
  │   │
  │   └─► YES → fetchThresholds()
  │       │
  │       ├─► GET /api/poll?deviceId=DEVICE001
  │       │
  │       ├─► Parse Response JSON
  │       │   {
  │       │     "update": true,
  │       │     "thresholds": {
  │       │       "temperature": [{levelName, minValue, maxValue, alertLevel, message}, ...],
  │       │       "humidity": [...],
  │       │       "mq135": [...],
  │       │       "mq2": [...],
  │       │       "mq3": [...],
  │       │       "pm25": [...]
  │       │     },
  │       │     "settings": {...},  // Backward compatibility
  │       │     "commands": [...]
  │       │   }
  │       │
  │       └─► Update thresholds.loaded = true
  │
  ├─► Check: WiFi Disconnected?
  │   │
  │   └─► YES → connectWiFi() [Reconnect]
  │
  └─► delay(200ms) → LOOP AGAIN
```

---

## 📤 FLOW 3: ESP32 → SERVER (Data Transmission)

```
┌─────────────────────────────────────────────────────────────────┐
│         ESP32 SEND DATA → SERVER (POST /api/data)                │
└─────────────────────────────────────────────────────────────────┘

ESP32: sendToServer(jsonPayload)
  │
  ├─► Check WiFi Connection
  │   └─► IF NOT Connected → Return (skip)
  │
  ├─► HTTP POST Request
  │   ├─► URL: http://192.168.137.1:8080/IoTWebApp/api/data
  │   ├─► Content-Type: application/json
  │   └─► Body: JSON payload
  │
  └─► SERVER: DataServlet.doPost()
      │
      ├─► Read Request Body
      │   └─► Parse JSON → ESP32SensorPayload object
      │
      ├─► SensorService.saveSensorData()
      │   │
      │   ├─► Get/Create DeviceInfo
      │   │   └─► DeviceInfoDAO.findByName() or createNew()
      │   │
      │   ├─► Save Individual Sensor Readings
      │   │   ├─► For each sensor value:
      │   │   │   ├─► Create SensorData object
      │   │   │   ├─► Map ESP32 field → SensorTypeID
      │   │   │   │   ├─► temperature → SensorTypeID 1 (TEMP_DHT11)
      │   │   │   │   ├─► humidity → SensorTypeID 2 (HUM_DHT11)
      │   │   │   │   ├─► mq135 → SensorTypeID 5 (MQ135)
      │   │   │   │   ├─► mq7 → SensorTypeID 4 (MQ7)
      │   │   │   │   ├─► mq2 → SensorTypeID 3 (MQ2)
      │   │   │   │   └─► dust → SensorTypeID 6 (GP2Y10)
      │   │   │   └─► SensorDataDAO.insert()
      │   │   │       └─► INSERT INTO SensorData
      │   │   │
      │   │   └─► Update DeviceInfo.last_seen
      │   │
      │   └─► Calculate & Save AQI
      │       ├─► Calculate AQI = (PM25/500)*500 + (CO/2000)*200 + (Gas/10000)*150
      │       ├─► Determine AQI Level & Color
      │       └─► AQIResultDAO.insert()
      │           └─► INSERT INTO AQIResult
      │
      └─► Return Response
          └─► {"status": "ok", "received": true}
```

---

## 📥 FLOW 4: ESP32 ← SERVER (Poll Thresholds & Commands)

```
┌─────────────────────────────────────────────────────────────────┐
│    ESP32 POLL SERVER (GET /api/poll?deviceId=DEVICE001)         │
└─────────────────────────────────────────────────────────────────┘

ESP32: fetchThresholds()
  │
  ├─► HTTP GET Request
  │   └─► URL: http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001
  │
  └─► SERVER: PollServlet.doGet()
      │
      ├─► Parse deviceId Parameter
      │   └─► Convert "DEVICE001" → DeviceID (Integer)
      │
      ├─► Get Simple Thresholds (Backward Compatibility)
      │   └─► ThresholdService.getThresholdMapForESP32(deviceId)
      │       ├─► For each sensor type:
      │       │   ├─► Find "Bình thường" threshold level
      │       │   └─► Return MaxValue
      │       └─► Returns: {"mq1": 300.0, "mq2": 300.0, ...}
      │
      ├─► Get Multi-Level Thresholds
      │   └─► getMultiLevelThresholds()
      │       ├─► For each SensorType:
      │       │   ├─► ThresholdDAO.findBySensorTypeId()
      │       │   ├─► Convert to ESP32 format
      │       │   └─► Map sensor codes:
      │       │       ├─► TEMP_DHT11 → "temperature"
      │       │       ├─► HUM_DHT11 → "humidity"
      │       │       ├─► MQ135 → "mq135"
      │       │       ├─► MQ7 → "mq2" (CO)
      │       │       ├─► MQ2 → "mq3" (Gas/LPG)
      │       │       └─► GP2Y10 → "pm25"
      │       └─► Returns: {"temperature": [...], "humidity": [...], ...}
      │
      ├─► Get Pending Commands
      │   └─► ThresholdService.getPendingCommands(deviceId)
      │       └─► DeviceCommandDAO.findPendingByDeviceId()
      │
      └─► Build & Return JSON Response
          {
            "update": true,
            "thresholds": {
              "temperature": [{levelName, minValue, maxValue, alertLevel, message}, ...],
              "humidity": [...],
              "mq135": [...],
              "mq2": [...],
              "mq3": [...],
              "pm25": [...]
            },
            "settings": {"mq1": 300.0, ...},  // Simple format
            "commands": [{command_type, command_value, command_id}, ...]
          }
          │
          └─► ESP32: Parse Response
              ├─► IF update == true AND thresholds exists:
              │   └─► Update thresholds structure
              │       ├─► Parse temperature levels
              │       ├─► Parse humidity levels
              │       ├─► Parse mq135 levels
              │       ├─► Parse mq2 (CO) levels
              │       ├─► Parse mq3 (Gas/LPG) levels
              │       └─► Parse pm25 levels
              │
              └─► IF commands exists:
                  └─► Process commands (future implementation)
```

---

## 🖥️ FLOW 5: WEB DASHBOARD (User Access)

```
┌─────────────────────────────────────────────────────────────────┐
│              USER ACCESS DASHBOARD (/dashboard)                  │
└─────────────────────────────────────────────────────────────────┘

USER: Browser → GET /dashboard?deviceId=2
  │
  ├─► AuthenticationFilter
  │   └─► Check Session
  │       ├─► IF NOT logged in → Redirect to /login
  │       └─► IF logged in → Continue
  │
  └─► DashboardServlet.doGet()
      │
      ├─► Parse deviceId Parameter
      │   └─► Convert to Integer (or find by device name)
      │
      ├─► Load Sensor Data
      │   └─► SensorService.getRecentData(deviceId, 100)
      │       └─► SensorDataDAO.findLatestByDevice()
      │           └─► SELECT * FROM SensorData WHERE deviceId = ? ORDER BY createdAt DESC LIMIT 100
      │
      ├─► Load AQI Results
      │   └─► AQIResultDAO.findLatestByDevice(deviceId, 100)
      │
      ├─► Convert to DashboardData
      │   └─► convertToDashboardData()
      │       ├─► Group by timestamp
      │       ├─► Map sensor names to standard names
      │       ├─► Aggregate sensor values per timestamp
      │       └─► Map AQI results to dashboard data
      │
      ├─► Calculate Forecast (1 hour ahead)
      │   └─► calculateForecast()
      │       ├─► Get latest values (tempNow, humNow, mq2Now)
      │       ├─► Get previous values (tempBefore, humBefore, mq2Before)
      │       └─► Calculate: forecast = now + (now - before) * 2
      │
      ├─► Set Request Attributes
      │   ├─► sensorData (List<DashboardData>)
      │   ├─► deviceId
      │   ├─► forecastTemp
      │   ├─► forecastHum
      │   └─► forecastMQ2
      │
      └─► Forward to dashboard.jsp
          │
          └─► JSP Rendering
              ├─► Display Metric Cards
              │   ├─► Temperature
              │   ├─► Humidity
              │   ├─► PM2.5
              │   ├─► AQI Index
              │   └─► WiFi Signal
              │
              ├─► Render Charts
              │   ├─► Line Chart: Temperature & Humidity (10 readings)
              │   └─► Radar Chart: MQ1, MQ2, MQ3 (Latest vs Average)
              │
              ├─► Display Forecast Section
              │   └─► Show predicted values for 1 hour ahead
              │
              └─► Display Data Table
                  └─► Show last 10 sensor readings
```

---

## 🔄 FLOW 6: DASHBOARD AUTO-REFRESH (Real-time Updates)

```
┌─────────────────────────────────────────────────────────────────┐
│         DASHBOARD AUTO-REFRESH (JavaScript)                      │
└─────────────────────────────────────────────────────────────────┘

Page Load Complete
  │
  ├─► Initialize Charts
  │   ├─► Climate Chart (Line Chart)
  │   └─► MQ Chart (Radar Chart)
  │
  ├─► Start Real-time Update Loop
  │   └─► setInterval(fetchDashboardData, 5000ms)
  │       │
  │       └─► fetchDashboardData()
  │           │
  │           ├─► GET /dashboard?deviceId=X&format=json&_t=timestamp
  │           │
  │           ├─► Parse JSON Response
  │           │   {
  │           │     "success": true,
  │           │     "data": [
  │           │       {
  │           │         "deviceId": 2,
  │           │         "timestamp": "2024-01-15 10:30:45",
  │           │         "temperature": 25.5,
  │           │         "humidity": 60.0,
  │           │         "mq1": 250,
  │           │         "mq2": 280,
  │           │         "mq3": 550,
  │           │         "dust": 35.0,
  │           │         "aqi": 75.5,
  │           │         "aqiLevel": "Trung bình",
  │           │         ...
  │           │       },
  │           │       ...
  │           │     ]
  │           │   }
  │           │
  │           ├─► Update Metric Cards
  │           │   └─► updateMetricCards(data)
  │           │
  │           ├─► Update Data Table
  │           │   └─► updateTable(data)
  │           │
  │           └─► Update Charts
  │               └─► updateCharts(data)
  │                   ├─► Update Climate Chart (temps, humidities)
  │                   └─► Update MQ Radar Chart
  │                       ├─► Latest Reading: [mq1, mq2, mq3]
  │                       └─► Average (last 10): [avgMq1, avgMq2, avgMq3]
  │
  └─► Repeat every 5 seconds
```

---

## ⚙️ FLOW 7: THRESHOLD MANAGEMENT (Admin)

```
┌─────────────────────────────────────────────────────────────────┐
│        ADMIN: THRESHOLD MANAGEMENT (/admin/thresholds)           │
└─────────────────────────────────────────────────────────────────┘

ADMIN: GET /admin/thresholds?deviceId=2
  │
  ├─► AuthenticationFilter → Check Admin Role
  │
  └─► ThresholdAdminServlet.doGet()
      │
      ├─► Parse deviceId
      │
      ├─► Load All Threshold Levels
      │   └─► ThresholdService.getAllThresholdLevels()
      │       ├─► For each sensor type:
      │       │   ├─► Try to load from database
      │       │   │   └─► ThresholdDAO.findBySensorTypeId()
      │       │   └─► IF no data in DB:
      │       │       └─► Return default thresholds (for form display)
      │       └─► Returns: Map<String, List<Threshold>>
      │           {
      │             "temperature": [Threshold1, Threshold2, ...],
      │             "humidity": [...],
      │             "mq1": [...],
      │             "mq2": [...],
      │             "mq3": [...],
      │             "dust": [...]
      │           }
      │
      ├─► Load Simple Thresholds (for display)
      │   └─► ThresholdService.getThresholdMapForESP32(deviceId)
      │
      └─► Forward to thresholds.jsp
          │
          └─► JSP Rendering
              ├─► Display Current Values Summary
              ├─► Display Multi-Level Threshold Form
              │   └─► For each sensor:
              │       └─► For each level:
              │           ├─► Input: minValue
              │           └─► Input: maxValue
              │
              └─► Submit Button → POST /admin/thresholds

─────────────────────────────────────────────────────────────────

ADMIN: POST /admin/thresholds (Update Thresholds)
  │
  └─► ThresholdAdminServlet.doPost()
      │
      ├─► Parse Form Parameters
      │   └─► Format: sensorName_levelIndex_minValue, sensorName_levelIndex_maxValue
      │       Example: temperature_0_minValue=-40, temperature_0_maxValue=18
      │
      ├─► For each sensor (temperature, humidity, mq1, mq2, mq3, dust):
      │   │
      │   ├─► Get threshold levels from getAllThresholdLevels()
      │   │
      │   ├─► For each level:
      │   │   ├─► Read form parameters
      │   │   ├─► Validate: minValue < maxValue
      │   │   ├─► Create Threshold copy with new values
      │   │   └─► Add to updatedThresholds list
      │   │
      │   └─► Update All Levels
      │       └─► ThresholdService.updateAllThresholdLevels()
      │           │
      │           ├─► For each threshold:
      │           │   ├─► IF thresholdId != null:
      │           │   │   └─► UPDATE existing
      │           │   │       └─► ThresholdDAO.update()
      │           │   │           ├─► Try: EXEC UpdateThreshold (stored procedure)
      │           │   │           └─► IF fails → Fallback: Direct UPDATE
      │           │   │
      │           │   └─► IF thresholdId == null:
      │           │       └─► INSERT new
      │           │           └─► ThresholdDAO.insert()
      │           │               └─► INSERT INTO Threshold
      │           │
      │           └─► Log to FileLogger
      │
      └─► Redirect to /admin/thresholds?deviceId=X&message=success
```

---

## 🔐 FLOW 8: AUTHENTICATION & AUTHORIZATION

```
┌─────────────────────────────────────────────────────────────────┐
│              USER LOGIN & AUTHENTICATION                         │
└─────────────────────────────────────────────────────────────────┘

USER: GET /login
  │
  └─► LoginServlet.doGet()
      │
      ├─► Check Session
      │   └─► IF already logged in → Redirect to /dashboard
      │
      └─► Forward to login.jsp

─────────────────────────────────────────────────────────────────

USER: POST /login (username, password)
  │
  └─► LoginServlet.doPost()
      │
      ├─► Validate Input
      │
      ├─► UserDAO.authenticate(username, password)
      │   └─► SELECT * FROM [User] WHERE Username = ? AND IsActive = 1
      │       └─► Compare password hash
      │
      ├─► IF Authentication Success:
      │   ├─► Create Session
      │   ├─► Set Session Attributes:
      │   │   ├─► user (User object)
      │   │   ├─► username
      │   │   ├─► fullName
      │   │   └─► role
      │   └─► Redirect to /dashboard
      │
      └─► IF Authentication Failed:
          └─► Forward to login.jsp with error message

─────────────────────────────────────────────────────────────────

PROTECTED ROUTES: /dashboard, /admin/*
  │
  └─► AuthenticationFilter
      │
      ├─► Check Session
      │   └─► IF no session or user == null:
      │       └─► Redirect to /login
      │
      └─► IF session exists:
          └─► Continue to requested servlet
```

---

## 🗄️ FLOW 9: DATABASE OPERATIONS

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE OPERATIONS                           │
└─────────────────────────────────────────────────────────────────┘

INSERT SENSOR DATA
  │
  ├─► SensorDataDAO.insert()
  │   └─► INSERT INTO SensorData (deviceId, sensorTypeId, value, timestamp)
  │       └─► Returns: Generated data_id
  │
  └─► AQIResultDAO.insert()
      └─► INSERT INTO AQIResult (deviceId, aqi, aqiLevel, pm25, co, gas, ...)

─────────────────────────────────────────────────────────────────

QUERY SENSOR DATA
  │
  ├─► SensorDataDAO.findLatestByDevice(deviceId, limit)
  │   └─► SELECT * FROM SensorData 
  │       WHERE deviceId = ? 
  │       ORDER BY createdAt DESC 
  │       LIMIT ?
  │
  └─► AQIResultDAO.findLatestByDevice(deviceId, limit)
      └─► SELECT * FROM AQIResult 
          WHERE deviceId = ? 
          ORDER BY createdAt DESC 
          LIMIT ?

─────────────────────────────────────────────────────────────────

THRESHOLD OPERATIONS
  │
  ├─► ThresholdDAO.findBySensorTypeId(sensorTypeId)
  │   └─► SELECT * FROM Threshold 
  │       WHERE SensorTypeID = ? 
  │       ORDER BY MinValue
  │
  ├─► ThresholdDAO.update(threshold, updatedBy)
  │   └─► EXEC UpdateThreshold @ThresholdID, @LevelName, @MinValue, @MaxValue, ...
  │       ├─► Stored Procedure:
  │       │   ├─► Check threshold exists
  │       │   ├─► Get old values
  │       │   ├─► INSERT INTO ThresholdUpdateLog (log changes)
  │       │   └─► UPDATE Threshold
  │       └─► IF fails → Fallback to direct UPDATE
  │
  └─► ThresholdDAO.insert(threshold)
      └─► INSERT INTO Threshold (SensorTypeID, LevelName, MinValue, MaxValue, ...)
          └─► Returns: Generated ThresholdID
```

---

## 🎛️ FLOW 10: THRESHOLD CHECKING & DEVICE CONTROL (ESP32)

```
┌─────────────────────────────────────────────────────────────────┐
│      ESP32: CHECK THRESHOLDS & CONTROL DEVICES                  │
└─────────────────────────────────────────────────────────────────┘

ESP32: checkThresholds(temperature, humidity, mq135, mq7, mq2, pm25)
  │
  ├─► For Each Sensor: Find Alert Level
  │   ├─► findAlertLevel(temperature, thresholds.temperature)
  │   │   └─► Loop through levels
  │   │       └─► IF value >= minValue AND value < maxValue:
  │   │           └─► Return: alertLevel, levelName, message
  │   │
  │   ├─► findAlertLevel(humidity, thresholds.humidity)
  │   ├─► findAlertLevel(mq135, thresholds.mq135)
  │   ├─► findAlertLevel(mq2, thresholds.mq2)
  │   ├─► findAlertLevel(mq7, thresholds.co)
  │   └─► findAlertLevel(pm25, thresholds.pm25)
  │
  ├─► Calculate AQI
  │   └─► calculateAQI(pm25, mq7, mq135)
  │       └─► AQI = (pm25/500)*500 + (mq7/2000)*200 + (mq135/10000)*150
  │
  ├─► FAN Control Decision
  │   ├─► Rule 1: IF AQI >= 75 → FAN ON
  │   ├─► Rule 2: IF any sensor AlertLevel >= 2 (except humidity) → FAN ON
  │   └─► Rule 3: IF humidity AlertLevel >= 1 → FAN ON
  │   │
  │   └─► Execute: digitalWrite(FAN_PIN, LOW) // ON
  │       OR: digitalWrite(FAN_PIN, HIGH) // OFF
  │
  └─► BUZZER Control Decision
      ├─► Rule 1: IF any sensor AlertLevel == 3 → BUZZER ON
      ├─► Rule 2: IF humidity AlertLevel >= 1 → BUZZER ON
      ├─► Rule 3: IF PM2.5 Alert >= 2 AND any MQ Alert >= 2 → BUZZER ON
      ├─► Rule 4: IF CO Alert >= 2 → BUZZER ON
      ├─► Rule 5: IF (MQ135 OR MQ2) Alert >= 2 AND humidity Alert >= 1 → BUZZER ON
      └─► Rule 6: IF temp Alert >= 2 AND PM2.5 Alert >= 1 AND AQI >= 100 → BUZZER ON
      │
      └─► Execute: digitalWrite(BUZZER_PIN, LOW) // ON
          OR: digitalWrite(BUZZER_PIN, HIGH) // OFF
```

---

## 📊 COMPLETE SYSTEM ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SYSTEM ARCHITECTURE                                 │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐
│   ESP32 DEVICE   │
│                  │
│  ┌────────────┐  │
│  │  Sensors   │  │
│  │  - DHT11   │  │
│  │  - MQ-135  │  │
│  │  - MQ-7    │  │
│  │  - MQ-2    │  │
│  │  - GP2Y10  │  │
│  └─────┬──────┘  │
│        │         │
│  ┌─────▼──────┐  │
│  │  Control   │  │
│  │  - FAN     │  │
│  │  - BUZZER  │  │
│  └────────────┘  │
└────────┬─────────┘
         │
         │ HTTP POST /api/data (every 5s)
         │ HTTP GET /api/poll (every 10s)
         │
┌────────▼──────────────────────────────────────────────────────────┐
│                    JAVA WEB SERVER (Tomcat)                       │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  CONTROLLER LAYER (Servlets)                                │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │  │
│  │  │ DataServlet  │  │ PollServlet  │  │DashboardServlet│   │  │
│  │  │ /api/data    │  │ /api/poll    │  │ /dashboard    │     │  │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │  │
│  │         │                 │                  │              │  │
│  │  ┌──────▼─────────────────▼──────────────────▼───────┐     │  │
│  │  │ ThresholdAdminServlet  │  LoginServlet            │     │  │
│  │  │ /admin/thresholds      │  /login                  │     │  │
│  │  └────────────────────────┴──────────────────────────┘     │  │
│  └───────────────────────────┬────────────────────────────────┘  │
│                              │                                    │
│  ┌───────────────────────────▼────────────────────────────────┐  │
│  │  SERVICE LAYER (Business Logic)                            │  │
│  │  ┌──────────────┐              ┌──────────────┐           │  │
│  │  │SensorService │              │ThresholdService│         │  │
│  │  │- saveData()  │              │- getAllLevels()│         │  │
│  │  │- getRecent() │              │- updateLevels()│         │  │
│  │  └──────┬───────┘              └──────┬───────┘           │  │
│  └─────────┼──────────────────────────────┼───────────────────┘  │
│            │                              │                       │
│  ┌─────────▼──────────────────────────────▼───────────────────┐  │
│  │  DAO LAYER (Data Access)                                   │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │  │
│  │  │SensorDataDAO │  │ThresholdDAO  │  │DeviceInfoDAO │     │  │
│  │  │AQIResultDAO  │  │SensorTypeDAO │  │UserDAO       │     │  │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │  │
│  └─────────┼──────────────────┼──────────────────┼────────────┘  │
└────────────┼──────────────────┼──────────────────┼───────────────┘
             │                  │                  │
             │ JDBC             │ JDBC             │ JDBC
             │                  │                  │
┌────────────▼──────────────────▼──────────────────▼───────────────┐
│                    SQL SERVER DATABASE                           │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  DeviceInfo  │  │  SensorData  │  │  Threshold   │          │
│  │  - device_id │  │  - data_id   │  │  - threshold_id│        │
│  │  - name      │  │  - value     │  │  - levels    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  AQIResult   │  │ SensorType   │  │  [User]      │          │
│  │  - aqi       │  │  - type_id   │  │  - user_id   │          │
│  │  - level     │  │  - name      │  │  - username  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                    │
│  ┌──────────────┐                                                │
│  │ThresholdUpdateLog│                                            │
│  │  - log_id    │                                                │
│  │  - old/new   │                                                │
│  └──────────────┘                                                │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔄 DATA FLOW SUMMARY

### 1. **ESP32 → Server (Data Collection)**
```
ESP32 Sensors → Read Values → Check Thresholds → Control Devices → POST /api/data → Server → Database
```
- **Frequency**: Every 5 seconds
- **Data**: temperature, humidity, mq135, mq7, mq2, dust
- **Processing**: Calculate AQI, check thresholds, control fan/buzzer

### 2. **ESP32 ← Server (Threshold Updates)**
```
ESP32 → GET /api/poll → Server → Database → Multi-level Thresholds → ESP32 → Update Local Thresholds
```
- **Frequency**: Every 10 seconds
- **Data**: Multi-level thresholds for all sensors
- **Processing**: Parse JSON, update threshold structure

### 3. **User → Dashboard (Monitoring)**
```
Browser → GET /dashboard → Server → Database → Aggregate Data → Calculate Forecast → JSP → Display
```
- **Auto-refresh**: Every 5 seconds (JavaScript)
- **Data**: Last 100 sensor readings, AQI results
- **Display**: Charts, tables, forecast, metrics

### 4. **Admin → Threshold Management (Configuration)**
```
Browser → GET /admin/thresholds → Load Thresholds → Display Form → POST /admin/thresholds → Update Database
```
- **Action**: Update multi-level thresholds
- **Processing**: Validate, update/insert thresholds, log changes

---

## 🎯 KEY COMPONENTS INTERACTION

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   ESP32     │◄───────►│   SERVER    │◄───────►│  DATABASE   │
│             │  HTTP   │             │   JDBC  │             │
│ - Sensors   │         │ - Servlets  │         │ - Tables    │
│ - Control   │         │ - Services  │         │ - Views     │
│ - Logic     │         │ - DAOs      │         │ - Procedures│
└─────────────┘         └──────┬──────┘         └─────────────┘
                               │
                               │ HTTP
                               │
                        ┌──────▼──────┐
                        │   BROWSER   │
                        │             │
                        │ - Dashboard │
                        │ - Admin UI  │
                        └─────────────┘
```

---

## 📝 NOTES

1. **ESP32 Multi-Level Thresholds**: Mỗi sensor có nhiều mức (levels) với minValue, maxValue, alertLevel, message
2. **Fallback Mechanism**: ESP32 sử dụng default thresholds nếu không fetch được từ server
3. **Real-time Updates**: Dashboard tự động refresh mỗi 5 giây
4. **Error Handling**: Mọi operation đều có try-catch và logging
5. **Authentication**: Tất cả protected routes đều qua AuthenticationFilter
6. **Database Logging**: Threshold updates được log vào ThresholdUpdateLog table

---

## 🔧 TECHNICAL STACK

- **ESP32**: Arduino Framework, WiFi, HTTPClient, ArduinoJson
- **Backend**: Java Servlet, JSP, JDBC, SQL Server
- **Frontend**: Bootstrap 5, Chart.js, JavaScript (ES6+)
- **Database**: SQL Server với stored procedures
- **Communication**: HTTP REST API (JSON)

---

*Flow chart này mô tả đầy đủ luồng hoạt động của toàn bộ hệ thống IoT Web Application.*

