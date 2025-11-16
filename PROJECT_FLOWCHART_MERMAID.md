# 📊 IoT Web Application - Mermaid Flow Charts

## 🎯 System Overview

```mermaid
graph TB
    subgraph ESP32["ESP32 Device"]
        Sensors["Sensors<br/>DHT11, MQ-135, MQ-7, MQ-2, GP2Y10"]
        Control["Control Devices<br/>FAN, BUZZER"]
        Logic["Threshold Logic<br/>& AQI Calculation"]
    end
    
    subgraph Server["Java Web Server"]
        Controllers["Controllers<br/>DataServlet, PollServlet<br/>DashboardServlet, ThresholdAdminServlet"]
        Services["Services<br/>SensorService<br/>ThresholdService"]
        DAOs["DAOs<br/>SensorDataDAO<br/>ThresholdDAO<br/>DeviceInfoDAO"]
    end
    
    subgraph DB["SQL Server Database"]
        Tables["Tables<br/>DeviceInfo, SensorData<br/>Threshold, AQIResult"]
    end
    
    subgraph Browser["Web Browser"]
        Dashboard["Dashboard<br/>Real-time Monitoring"]
        Admin["Admin UI<br/>Threshold Management"]
    end
    
    Sensors -->|Read Values| Logic
    Logic -->|Control| Control
    Logic -->|POST /api/data| Controllers
    Controllers -->|GET /api/poll| Logic
    Controllers --> Services
    Services --> DAOs
    DAOs -->|JDBC| Tables
    Browser -->|HTTP| Controllers
    Controllers -->|Render| Dashboard
    Controllers -->|Render| Admin
```

---

## 🔄 Flow 1: ESP32 Startup & Initialization

```mermaid
flowchart TD
    Start([ESP32 Power On]) --> InitSerial[Initialize Serial]
    InitSerial --> InitPins[Configure GPIO Pins]
    InitPins --> InitDHT[Initialize DHT Sensor]
    InitDHT --> InitDefaults[Load Default Thresholds]
    InitDefaults --> ConnectWiFi[Connect to WiFi]
    ConnectWiFi --> WiFiConnected{WiFi<br/>Connected?}
    WiFiConnected -->|Yes| FetchThresholds[Fetch Thresholds from Server]
    WiFiConnected -->|No| Retry[Retry Connection]
    Retry --> ConnectWiFi
    FetchThresholds --> MainLoop[Enter Main Loop]
    MainLoop --> LoopStart[Loop Start]
```

---

## 🔄 Flow 2: ESP32 Main Loop - Data Collection

```mermaid
flowchart TD
    LoopStart[Loop Start] --> CheckSend{Time to<br/>Send Data?<br/>5s elapsed?}
    CheckSend -->|Yes| ReadSensors[Read All Sensors]
    CheckSend -->|No| CheckPoll
    
    ReadSensors --> CheckThresholds[Check Thresholds]
    CheckThresholds --> CalculateAQI[Calculate AQI]
    CalculateAQI --> ControlFan[Control FAN]
    ControlFan --> ControlBuzzer[Control BUZZER]
    ControlBuzzer --> CreateJSON[Create JSON Payload]
    CreateJSON --> PostData[POST /api/data]
    PostData --> CheckPoll
    
    CheckPoll{Time to<br/>Poll?<br/>10s elapsed?} -->|Yes| GetThresholds[GET /api/poll]
    GetThresholds --> ParseResponse[Parse Threshold Response]
    ParseResponse --> UpdateThresholds[Update Local Thresholds]
    UpdateThresholds --> CheckWiFi
    
    CheckPoll -->|No| CheckWiFi
    CheckWiFi{WiFi<br/>Connected?} -->|No| Reconnect[Reconnect WiFi]
    Reconnect --> LoopStart
    CheckWiFi -->|Yes| Delay[Delay 200ms]
    Delay --> LoopStart
```

---

## 🔄 Flow 3: ESP32 → Server (Data Transmission)

```mermaid
sequenceDiagram
    participant ESP32
    participant DataServlet
    participant SensorService
    participant SensorDataDAO
    participant AQIResultDAO
    participant Database
    
    ESP32->>DataServlet: POST /api/data (JSON)
    DataServlet->>DataServlet: Parse JSON Payload
    DataServlet->>SensorService: saveSensorDataFromESP32()
    SensorService->>SensorService: Get/Create DeviceInfo
    SensorService->>SensorDataDAO: insert() for each sensor
    SensorDataDAO->>Database: INSERT INTO SensorData
    SensorService->>SensorService: Calculate AQI
    SensorService->>AQIResultDAO: insert()
    AQIResultDAO->>Database: INSERT INTO AQIResult
    SensorService-->>DataServlet: Success
    DataServlet-->>ESP32: {"status":"ok","received":true}
```

---

## 🔄 Flow 4: ESP32 ← Server (Poll Thresholds)

```mermaid
sequenceDiagram
    participant ESP32
    participant PollServlet
    participant ThresholdService
    participant ThresholdDAO
    participant SensorTypeDAO
    participant Database
    
    ESP32->>PollServlet: GET /api/poll?deviceId=DEVICE001
    PollServlet->>ThresholdService: getThresholdMapForESP32()
    ThresholdService->>Database: Query Simple Thresholds
    PollServlet->>PollServlet: getMultiLevelThresholds()
    PollServlet->>SensorTypeDAO: findAll()
    PollServlet->>ThresholdDAO: findBySensorTypeId() for each sensor
    ThresholdDAO->>Database: SELECT * FROM Threshold
    PollServlet->>PollServlet: Build JSON Response
    PollServlet-->>ESP32: JSON with thresholds & commands
    ESP32->>ESP32: Parse & Update Local Thresholds
```

---

## 🔄 Flow 5: Threshold Checking & Device Control

```mermaid
flowchart TD
    Start[checkThresholds Called] --> ReadValues[Read Sensor Values]
    ReadValues --> ForEachSensor[For Each Sensor]
    
    ForEachSensor --> FindLevel[findAlertLevel]
    FindLevel --> LoopLevels{Loop through<br/>Threshold Levels}
    LoopLevels --> CheckRange{Value in<br/>Range?<br/>min <= val < max}
    CheckRange -->|Yes| ReturnLevel[Return: alertLevel,<br/>levelName, message]
    CheckRange -->|No| NextLevel[Next Level]
    NextLevel --> LoopLevels
    
    ReturnLevel --> CalculateAQI[Calculate AQI]
    CalculateAQI --> CheckFanRules[Check FAN Rules]
    
    CheckFanRules --> Rule1{AQI >= 75?}
    Rule1 -->|Yes| FanON[FAN ON]
    Rule1 -->|No| Rule2{Any Sensor<br/>Alert >= 2?}
    Rule2 -->|Yes| FanON
    Rule2 -->|No| Rule3{Humidity<br/>Alert >= 1?}
    Rule3 -->|Yes| FanON
    Rule3 -->|No| FanOFF[FAN OFF]
    
    FanON --> CheckBuzzerRules
    FanOFF --> CheckBuzzerRules
    
    CheckBuzzerRules --> BuzzerRule1{Any Sensor<br/>Alert == 3?}
    BuzzerRule1 -->|Yes| BuzzerON[BUZZER ON]
    BuzzerRule1 -->|No| BuzzerRule2{PM2.5 Alert >= 2<br/>AND MQ Alert >= 2?}
    BuzzerRule2 -->|Yes| BuzzerON
    BuzzerRule2 -->|No| BuzzerRule3{CO Alert >= 2?}
    BuzzerRule3 -->|Yes| BuzzerON
    BuzzerRule3 -->|No| BuzzerOFF[BUZZER OFF]
    
    BuzzerON --> End[End]
    BuzzerOFF --> End
```

---

## 🔄 Flow 6: Dashboard Access & Display

```mermaid
flowchart TD
    User[User Browser] --> AuthFilter[AuthenticationFilter]
    AuthFilter --> CheckSession{Session<br/>Exists?}
    CheckSession -->|No| Login[Redirect to /login]
    CheckSession -->|Yes| DashboardServlet[DashboardServlet.doGet]
    
    DashboardServlet --> ParseDeviceId[Parse deviceId Parameter]
    ParseDeviceId --> LoadSensorData[SensorService.getRecentData]
    LoadSensorData --> SensorDataDAO[SensorDataDAO.findLatestByDevice]
    SensorDataDAO --> DB1[(Database)]
    DB1 --> LoadAQI[AQIResultDAO.findLatestByDevice]
    LoadAQI --> DB2[(Database)]
    
    DB2 --> ConvertData[convertToDashboardData]
    ConvertData --> GroupByTime[Group by Timestamp]
    GroupByTime --> MapSensors[Map Sensor Names]
    MapSensors --> AggregateValues[Aggregate Values]
    
    AggregateValues --> CalculateForecast[Calculate Forecast]
    CalculateForecast --> SetAttributes[Set Request Attributes]
    SetAttributes --> ForwardJSP[Forward to dashboard.jsp]
    
    ForwardJSP --> RenderCards[Render Metric Cards]
    RenderCards --> RenderCharts[Render Charts]
    RenderCharts --> RenderForecast[Render Forecast Section]
    RenderForecast --> RenderTable[Render Data Table]
    RenderTable --> AutoRefresh[Start Auto-Refresh<br/>Every 5s]
```

---

## 🔄 Flow 7: Dashboard Auto-Refresh

```mermaid
sequenceDiagram
    participant Browser
    participant DashboardServlet
    participant Database
    participant Charts
    
    Browser->>Browser: Page Load Complete
    Browser->>Browser: Initialize Charts (Chart.js)
    Browser->>Browser: setInterval(fetchDashboardData, 5000ms)
    
    loop Every 5 seconds
        Browser->>DashboardServlet: GET /dashboard?format=json
        DashboardServlet->>Database: Query Latest 100 Records
        Database-->>DashboardServlet: Sensor Data + AQI
        DashboardServlet->>DashboardServlet: Convert to DashboardData
        DashboardServlet-->>Browser: JSON Response
        Browser->>Charts: Update Charts
        Browser->>Browser: Update Metric Cards
        Browser->>Browser: Update Data Table
    end
```

---

## 🔄 Flow 8: Threshold Management (Admin)

```mermaid
flowchart TD
    Admin[Admin User] --> GET[GET /admin/thresholds]
    GET --> AuthCheck[AuthenticationFilter]
    AuthCheck --> ThresholdAdminServlet[ThresholdAdminServlet.doGet]
    
    ThresholdAdminServlet --> LoadLevels[ThresholdService.getAllThresholdLevels]
    LoadLevels --> ForEachSensor[For Each Sensor Type]
    ForEachSensor --> QueryDB{Data in<br/>Database?}
    QueryDB -->|Yes| LoadFromDB[ThresholdDAO.findBySensorTypeId]
    QueryDB -->|No| UseDefaults[Return Default Thresholds]
    LoadFromDB --> RenderForm
    UseDefaults --> RenderForm
    
    RenderForm[Render thresholds.jsp] --> DisplayForm[Display Multi-Level Form]
    DisplayForm --> UserInput[User Inputs Values]
    UserInput --> Submit[POST /admin/thresholds]
    
    Submit --> ParseForm[Parse Form Parameters]
    ParseForm --> Validate[Validate Values]
    Validate --> ForEachLevel[For Each Threshold Level]
    ForEachLevel --> CheckID{thresholdId<br/>exists?}
    CheckID -->|Yes| Update[ThresholdDAO.update]
    CheckID -->|No| Insert[ThresholdDAO.insert]
    
    Update --> TrySP{Try Stored<br/>Procedure?}
    TrySP -->|Success| LogUpdate[Log to ThresholdUpdateLog]
    TrySP -->|Fail| DirectUpdate[Direct UPDATE]
    DirectUpdate --> LogUpdate
    
    Insert --> DBInsert[INSERT INTO Threshold]
    LogUpdate --> Success[Success]
    DBInsert --> Success
    Success --> Redirect[Redirect with Success Message]
```

---

## 🔄 Flow 9: Complete Data Flow (End-to-End)

```mermaid
graph LR
    subgraph ESP32["ESP32 Device"]
        A1[Read Sensors] --> A2[Check Thresholds]
        A2 --> A3[Control Devices]
        A3 --> A4[POST /api/data]
    end
    
    subgraph Server["Java Server"]
        B1[DataServlet] --> B2[SensorService]
        B2 --> B3[Save to DB]
        B4[PollServlet] --> B5[ThresholdService]
        B5 --> B6[Query Thresholds]
        B7[DashboardServlet] --> B8[Load Data]
        B8 --> B9[Calculate Forecast]
    end
    
    subgraph DB["Database"]
        C1[(SensorData)]
        C2[(Threshold)]
        C3[(AQIResult)]
    end
    
    subgraph Browser["Web Browser"]
        D1[Dashboard] --> D2[Auto-Refresh]
        D2 --> D3[Update Charts]
    end
    
    A4 -->|HTTP POST| B1
    B3 --> C1
    B3 --> C3
    B6 --> C2
    B6 -->|HTTP GET| A1
    B9 -->|Render| D1
    D2 -->|AJAX| B7
    B8 --> C1
    B8 --> C3
```

---

## 🗄️ Database Schema Relationships

```mermaid
erDiagram
    DeviceInfo ||--o{ SensorData : "has"
    DeviceInfo ||--o{ AQIResult : "generates"
    DeviceInfo ||--o{ DeviceCommand : "receives"
    SensorType ||--o{ SensorData : "defines"
    SensorType ||--o{ Threshold : "has"
    Threshold ||--o{ ThresholdUpdateLog : "logs"
    User ||--o{ ThresholdUpdateLog : "updates"
    
    DeviceInfo {
        int device_id PK
        string device_name
        string status
        datetime last_seen
    }
    
    SensorData {
        bigint data_id PK
        int device_id FK
        int sensor_type_id FK
        float sensor_value
        datetime timestamp
    }
    
    SensorType {
        int sensor_type_id PK
        string sensor_name
        string unit
    }
    
    Threshold {
        int threshold_id PK
        int sensor_type_id FK
        string level_name
        float min_value
        float max_value
        int alert_level
        string message
    }
    
    AQIResult {
        int aqi_id PK
        int device_id FK
        float aqi
        string aqi_level
        float pm25
        float co
        float gas
    }
    
    ThresholdUpdateLog {
        int log_id PK
        int threshold_id FK
        int updated_by FK
        float old_min_value
        float new_min_value
        datetime updated_at
    }
```

---

## ⏱️ Timing Diagram - Complete Cycle

```mermaid
sequenceDiagram
    autonumber
    participant ESP32
    participant Server
    participant DB
    participant Browser
    
    Note over ESP32: Startup
    ESP32->>Server: Connect WiFi
    ESP32->>Server: GET /api/poll (First time)
    Server->>DB: Query Thresholds
    DB-->>Server: Threshold Data
    Server-->>ESP32: Multi-level Thresholds
    
    Note over ESP32: Main Loop (Every 5s)
    loop Every 5 seconds
        ESP32->>ESP32: Read Sensors
        ESP32->>ESP32: Check Thresholds
        ESP32->>ESP32: Control FAN/BUZZER
        ESP32->>Server: POST /api/data
        Server->>DB: INSERT SensorData
        Server->>DB: INSERT AQIResult
        Server-->>ESP32: {"status":"ok"}
    end
    
    Note over ESP32: Poll Loop (Every 10s)
    loop Every 10 seconds
        ESP32->>Server: GET /api/poll
        Server->>DB: Query Latest Thresholds
        DB-->>Server: Threshold Data
        Server-->>ESP32: Updated Thresholds
        ESP32->>ESP32: Update Local Thresholds
    end
    
    Note over Browser: Dashboard (Every 5s)
    loop Every 5 seconds
        Browser->>Server: GET /dashboard?format=json
        Server->>DB: Query Latest 100 Records
        DB-->>Server: Sensor Data
        Server->>Server: Calculate Forecast
        Server-->>Browser: JSON Data
        Browser->>Browser: Update UI & Charts
    end
```

---

## 🎛️ Control Logic Flow (ESP32)

```mermaid
flowchart TD
    Start[checkThresholds] --> GetValues[Get All Sensor Values]
    GetValues --> FindLevels[Find Alert Level for Each Sensor]
    
    FindLevels --> TempLevel[Temperature Alert Level]
    FindLevels --> HumLevel[Humidity Alert Level]
    FindLevels --> MQ135Level[MQ135 Alert Level]
    FindLevels --> MQ2Level[MQ2 Alert Level]
    FindLevels --> COLevel[CO Alert Level]
    FindLevels --> PM25Level[PM2.5 Alert Level]
    
    TempLevel --> CalculateAQI[Calculate AQI]
    HumLevel --> CalculateAQI
    MQ135Level --> CalculateAQI
    MQ2Level --> CalculateAQI
    COLevel --> CalculateAQI
    PM25Level --> CalculateAQI
    
    CalculateAQI --> FanDecision[FAN Decision Logic]
    FanDecision --> FanRule1{AQI >= 75?}
    FanRule1 -->|Yes| FanON
    FanRule1 -->|No| FanRule2{Any Alert >= 2?}
    FanRule2 -->|Yes| FanON
    FanRule2 -->|No| FanRule3{Humidity >= 1?}
    FanRule3 -->|Yes| FanON
    FanRule3 -->|No| FanOFF[FAN OFF]
    
    FanON[FAN ON] --> BuzzerDecision
    FanOFF --> BuzzerDecision
    
    BuzzerDecision[BUZZER Decision Logic] --> BuzzerRule1{Any Alert == 3?}
    BuzzerRule1 -->|Yes| BuzzerON
    BuzzerRule1 -->|No| BuzzerRule2{PM2.5 >= 2<br/>AND MQ >= 2?}
    BuzzerRule2 -->|Yes| BuzzerON
    BuzzerRule2 -->|No| BuzzerRule3{CO >= 2?}
    BuzzerRule3 -->|Yes| BuzzerON
    BuzzerRule3 -->|No| BuzzerRule4{Gas >= 2<br/>AND Hum >= 1?}
    BuzzerRule4 -->|Yes| BuzzerON
    BuzzerRule4 -->|No| BuzzerRule5{Temp >= 2<br/>AND PM2.5 >= 1<br/>AND AQI >= 100?}
    BuzzerRule5 -->|Yes| BuzzerON
    BuzzerRule5 -->|No| BuzzerOFF[BUZZER OFF]
    
    BuzzerON[BUZZER ON] --> Execute[Execute Control]
    BuzzerOFF --> Execute
    Execute --> End[End]
```

---

## 📊 Component Interaction Matrix

| Component | ESP32 | DataServlet | PollServlet | DashboardServlet | ThresholdAdminServlet | Database |
|-----------|-------|-------------|-------------|------------------|----------------------|----------|
| **ESP32** | - | POST /api/data | GET /api/poll | - | - | - |
| **DataServlet** | Receives | - | - | - | - | INSERT SensorData |
| **PollServlet** | Sends | - | - | - | - | SELECT Threshold |
| **DashboardServlet** | - | - | - | - | - | SELECT SensorData |
| **ThresholdAdminServlet** | - | - | - | - | - | UPDATE Threshold |
| **Database** | - | Stores | Queries | Queries | Updates | - |

---

## 🔄 Request-Response Patterns

### Pattern 1: Data Collection
```
ESP32 → POST /api/data → Server → Database → Response
Frequency: Every 5 seconds
```

### Pattern 2: Threshold Polling
```
ESP32 → GET /api/poll → Server → Database → Response
Frequency: Every 10 seconds
```

### Pattern 3: Dashboard View
```
Browser → GET /dashboard → Server → Database → JSP → HTML
Auto-refresh: Every 5 seconds (AJAX)
```

### Pattern 4: Threshold Update
```
Browser → POST /admin/thresholds → Server → Database → Redirect
Trigger: User submits form
```

---

*Mermaid diagrams này có thể được render trực tiếp trên GitHub, GitLab, hoặc các Markdown viewers hỗ trợ Mermaid.*

