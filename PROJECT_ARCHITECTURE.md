# 📚 TÀI LIỆU KIẾN TRÚC DỰ ÁN IoT WEB APPLICATION

## 🎯 TỔNG QUAN DỰ ÁN

Dự án này là một **hệ thống IoT quản lý chất lượng không khí** sử dụng:
- **ESP32** làm thiết bị cảm biến (firmware)
- **Java Web Application** (Servlet + JSP + JPA/Hibernate) làm backend
- **SQL Server** làm database
- **REST API** để giao tiếp giữa ESP32 và Web Server

---

## 🏗️ KIẾN TRÚC TỔNG THỂ

```
┌─────────────────┐
│   ESP32 Device  │
│  (Firmware)     │
│  - Đọc cảm biến │
│  - Gửi dữ liệu  │
│  - Nhận ngưỡng  │
│  - Điều khiển   │
│    (Quạt/Chuông)│
└────────┬────────┘
         │ HTTP REST API
         │ POST /api/data (gửi dữ liệu)
         │ GET  /api/poll (lấy ngưỡng)
         ▼
┌─────────────────────────────────────┐
│   Java Web Application              │
│   (Tomcat Server)                   │
│                                     │
│  ┌──────────────┐  ┌─────────────┐ │
│  │  Controller  │  │   Service   │ │
│  │  (Servlet)   │→ │   Layer     │ │
│  └──────┬───────┘  └──────┬──────┘ │
│         │                  │        │
│         ▼                  ▼        │
│  ┌──────────────┐  ┌─────────────┐ │
│  │     DAO      │  │    Model    │ │
│  │   (JPA)      │→ │   (Entity)  │ │
│  └──────┬───────┘  └─────────────┘ │
└─────────┼──────────────────────────┘
          │ JPA/Hibernate
          ▼
┌─────────────────────────────────────┐
│      SQL Server Database            │
│  - Device, SensorType, Threshold    │
│  - SensorData, AQI_Result           │
│  - Alert, User, DeviceCommand       │
└─────────────────────────────────────┘
```

---

## 📊 CẤU TRÚC DATABASE (SQL Server)

### 1. **Bảng [User]**
Quản lý người dùng hệ thống.
```sql
- UserID (PK, INT, IDENTITY)
- Username (NVARCHAR(50), UNIQUE)
- Email (NVARCHAR(100), UNIQUE)
- PasswordHash (NVARCHAR(255))
- FullName, Phone, Role (Admin/User)
- IsActive (BIT)
```

### 2. **Bảng Device**
Thông tin thiết bị IoT (ESP32).
```sql
- DeviceID (PK, INT, IDENTITY)
- DeviceName (NVARCHAR(50)) - VD: "DEVICE001"
- Location, Latitude, Longitude
- Status (Active/Inactive)
- BatteryLevel, InstallDate, LastUpdate
```

### 3. **Bảng SensorType**
Định nghĩa các loại cảm biến.
```sql
- SensorTypeID (PK, INT, IDENTITY)
- SensorName (NVARCHAR(50)) - VD: "DHT11 - Nhiệt độ"
- SensorCode (NVARCHAR(20), UNIQUE) - VD: "TEMP_DHT11"
- Unit (NVARCHAR(20)) - VD: "°C", "%", "ppm"
- Description
```

**Các SensorType mặc định:**
- `TEMP_DHT11` (ID=1): Nhiệt độ (°C)
- `HUM_DHT11` (ID=2): Độ ẩm (%)
- `MQ2` (ID=3): Gas/LPG (ppm)
- `MQ7` (ID=4): Khí CO (ppm)
- `MQ135` (ID=5): Chất lượng không khí (ppm)
- `GP2Y10` (ID=6): Bụi mịn PM2.5 (µg/m³)

### 4. **Bảng Threshold** ⭐ (Multi-Level Thresholds)
Ngưỡng cảnh báo **nhiều mức** cho mỗi cảm biến.
```sql
- ThresholdID (PK, INT, IDENTITY)
- SensorTypeID (FK → SensorType)
- LevelName (NVARCHAR(50)) - VD: "Bình thường", "Nguy hiểm"
- MinValue, MaxValue (FLOAT)
- AlertLevel (INT, 0-3):
  * 0 = OK/Bình thường
  * 1 = Chú ý
  * 2 = Nguy hiểm
  * 3 = Khẩn cấp
- Message (NVARCHAR(300))
```

**Ví dụ ngưỡng PM2.5:**
- Level 1: "Tốt" (0-12, AlertLevel=0)
- Level 2: "Trung bình" (12-35, AlertLevel=1)
- Level 3: "Kém" (35-55, AlertLevel=2)
- Level 4: "Xấu" (55-150, AlertLevel=2)
- Level 5: "Nguy hiểm" (150-500, AlertLevel=3)

### 5. **Bảng SensorData**
Lưu trữ dữ liệu cảm biến từ ESP32.
```sql
- DataID (PK, BIGINT, IDENTITY)
- DeviceID (FK → Device)
- SensorTypeID (FK → SensorType)
- Value (FLOAT) - Giá trị đọc được
- CreatedAt (DATETIME, DEFAULT GETDATE())
```

**Lưu ý:** Mỗi lần ESP32 gửi dữ liệu, tạo **6 bản ghi** (1 cho mỗi sensor) với **cùng timestamp**.

### 6. **Bảng AQI_Result**
Kết quả tính toán AQI (Air Quality Index).
```sql
- ID (PK, INT, IDENTITY)
- DeviceID (FK → Device)
- PM25, CO, GAS, Temperature, Humidity (FLOAT)
- AQI (FLOAT) - Chỉ số chất lượng không khí
- AQI_Level (NVARCHAR(50)) - VD: "Tốt", "Trung bình"
- AQI_Color (NVARCHAR(20)) - VD: "#10b981" (màu xanh)
- MainPollutant (NVARCHAR(50)) - Chất ô nhiễm chính
- CreatedAt (DATETIME)
```

### 7. **Bảng Alert**
Cảnh báo khi vượt ngưỡng.
```sql
- AlertID (PK, INT, IDENTITY)
- DeviceID, SensorTypeID
- AlertLevel (1-3)
- Message, SensorValue
- Status (Mới/Đã xem/Đã xử lý)
- CreatedAt, ResolvedAt, ResolvedBy
```

### 8. **Bảng ThresholdUpdateLog**
Lịch sử cập nhật ngưỡng (audit trail).
```sql
- LogID (PK, INT, IDENTITY)
- ThresholdID, UpdatedBy (FK → User)
- OldMinValue, OldMaxValue, OldLevelName, ...
- NewMinValue, NewMaxValue, NewLevelName, ...
- UpdatedAt
```

### 9. **Stored Procedure: UpdateThreshold**
Cập nhật ngưỡng và tự động ghi log.
```sql
EXEC UpdateThreshold
    @ThresholdID, @LevelName, @MinValue, @MaxValue,
    @AlertLevel, @Message, @UpdatedBy
```

---

## 🔄 LUỒNG HOẠT ĐỘNG CHÍNH

### **LUỒNG 1: ESP32 Gửi Dữ Liệu Cảm Biến**

```
1. ESP32 đọc cảm biến (mỗi 5 giây)
   ├─ DHT11: Nhiệt độ, Độ ẩm
   ├─ MQ-135: Chất lượng không khí
   ├─ MQ-7: Khí CO
   ├─ MQ-2: Gas/LPG
   └─ GP2Y10: PM2.5

2. ESP32 tạo JSON payload:
   {
     "deviceId": "DEVICE001",
     "temperature": 25.5,
     "humidity": 60.0,
     "mq135": 250.0,
     "mq7": 180.0,
     "mq2": 300.0,
     "dust": 12.5
   }

3. ESP32 POST đến: http://server:8080/IoTWebApp/api/data

4. DataServlet nhận request:
   ├─ Parse JSON → ESP32SensorPayload
   ├─ Gọi SensorService.saveSensorDataFromESP32()
   └─ Trả về: {"status":"ok","received":true}

5. SensorService xử lý:
   ├─ Tìm/Create Device (deviceId String → Integer)
   ├─ Map ESP32 fields → SensorCode:
   │   ├─ temperature → TEMP_DHT11
   │   ├─ humidity → HUM_DHT11
   │   ├─ mq135 → MQ135
   │   ├─ mq7 → MQ7
   │   ├─ mq2 → MQ2
   │   └─ dust → GP2Y10
   ├─ Tạo 6 SensorData records (cùng timestamp)
   ├─ Lưu batch vào database
   ├─ Tính AQI và lưu vào AQI_Result
   └─ Update Device.LastUpdate

6. Database:
   └─ 6 rows trong SensorData
   └─ 1 row trong AQI_Result
```

### **LUỒNG 2: ESP32 Lấy Ngưỡng (Polling)**

```
1. ESP32 gọi mỗi 10 giây:
   GET http://server:8080/IoTWebApp/api/poll?deviceId=DEVICE001

2. PollServlet xử lý:
   ├─ Parse deviceId (String → Integer hoặc tìm theo tên)
   ├─ Gọi ThresholdService.getThresholdMapForESP32()
   ├─ Gọi getMultiLevelThresholds() để lấy multi-level
   ├─ Lấy pending commands (nếu có)
   └─ Trả về JSON:

   {
     "update": true,
     "settings": {
       "temperature": 32.0,
       "humidity": 70.0,
       "dust": 12.0,
       "mq3": 300.0,  // MQ2 Gas/LPG
       "mq2": 50.0    // MQ7 CO
     },
     "thresholds": {
       "temperature": [
         {"levelName": "Lạnh", "minValue": -40, "maxValue": 18, "alertLevel": 0, ...},
         {"levelName": "Bình thường", "minValue": 18, "maxValue": 32, "alertLevel": 0, ...},
         ...
       ],
       "humidity": [...],
       "pm25": [...],
       "mq135": [...],
       "mq3": [...],  // Gas/LPG
       "mq2": [...]   // CO
     },
     "commands": []
   }

3. ESP32 parse response:
   ├─ Ưu tiên parse "thresholds" (multi-level)
   ├─ Nếu không có, parse "settings" và convert sang multi-level
   └─ Cập nhật thresholds trong memory

4. ESP32 sử dụng thresholds:
   └─ checkThresholds() để quyết định bật/tắt Quạt/Chuông
```

### **LUỒNG 3: ESP32 Kiểm Tra Ngưỡng và Điều Khiển**

```
1. Sau khi đọc cảm biến, ESP32 gọi checkThresholds():

2. Với mỗi sensor, tìm AlertLevel:
   ├─ findAlertLevel(value, thresholds.temperature) → AlertLevel
   ├─ findAlertLevel(value, thresholds.humidity) → AlertLevel
   ├─ findAlertLevel(value, thresholds.mq135) → AlertLevel
   ├─ findAlertLevel(value, thresholds.co) → AlertLevel
   ├─ findAlertLevel(value, thresholds.mq2) → AlertLevel
   └─ findAlertLevel(value, thresholds.pm25) → AlertLevel

3. Tính AQI:
   └─ calculateAQI(pm25, co, gas) = (pm25/500)*500 + (co/2000)*200 + (gas/10000)*150

4. Logic bật QUẠT (FAN):
   ├─ AQI >= 75
   ├─ Bất kỳ sensor AlertLevel >= 2 (trừ độ ẩm)
   └─ Độ ẩm AlertLevel >= 1

5. Logic bật CHUÔNG (BUZZER):
   ├─ Bất kỳ sensor AlertLevel = 3 (Khẩn cấp)
   ├─ Độ ẩm AlertLevel >= 1
   ├─ PM2.5 XẤU + MQ XẤU
   ├─ CO Nguy hiểm (AlertLevel >= 2)
   ├─ Gas + Độ ẩm cao
   └─ Nhiệt độ quá nóng + PM2.5 cao + AQI >= 100

6. Kích hoạt thiết bị:
   └─ digitalWrite(FAN_PIN, LOW/HIGH)
   └─ digitalWrite(BUZZER_PIN, LOW/HIGH)
```

### **LUỒNG 4: Web Dashboard Hiển Thị Dữ Liệu**

```
1. User truy cập: http://server:8080/IoTWebApp/dashboard?deviceId=1

2. DashboardServlet xử lý:
   ├─ Parse deviceId (có thể là số hoặc tên "DEVICE001")
   ├─ Gọi SensorDataDAO.findLatestByDevice(deviceId, 100)
   ├─ Gọi AQIResultDAO.findLatestByDevice(deviceId, 100)
   ├─ Convert SensorData → DashboardData (group by timestamp)
   ├─ Map AQI data vào DashboardData (match theo timestamp)
   └─ Forward đến dashboard.jsp

3. dashboard.jsp render:
   ├─ Metric Cards: Nhiệt độ, Độ ẩm, AQI Index, Wi-Fi Signal
   ├─ Charts: Nhiệt độ & Độ ẩm, MQ Gas Sensors (Chart.js)
   ├─ AI Prediction Section: Khung dự đoán 1 giờ tới (placeholder)
   └─ Table: Bản ghi cảm biến gần nhất (10 mục)

4. JavaScript (Real-time updates):
   ├─ setInterval() gọi AJAX mỗi 5 giây
   ├─ GET /dashboard?deviceId=1&format=json
   ├─ Update metric cards, charts, table
   └─ Apply CSS classes cho AQI (màu sắc theo level)
```

### **LUỒNG 5: Admin Cập Nhật Ngưỡng**

```
1. Admin truy cập: http://server:8080/IoTWebApp/admin/thresholds?deviceId=1

2. ThresholdAdminServlet (GET):
   ├─ Load current thresholds từ database
   ├─ Forward đến thresholds.jsp

3. thresholds.jsp hiển thị form:
   └─ Input fields cho từng sensor (temperature, humidity, mq1, mq2, mq3, dust)

4. Admin submit form (POST):
   └─ ThresholdAdminServlet nhận form data

5. ThresholdAdminServlet (POST):
   ├─ Parse form parameters
   ├─ Gọi ThresholdService.updateThresholds()
   └─ Redirect về GET với message success

6. ThresholdService.updateThresholds():
   ├─ Với mỗi sensor, tìm Threshold có LevelName="Bình thường" (hoặc tương đương)
   ├─ Update MaxValue của threshold đó
   ├─ Gọi ThresholdDAO.update() → Stored Procedure UpdateThreshold
   └─ Stored Procedure tự động ghi log vào ThresholdUpdateLog

7. ESP32 sẽ nhận ngưỡng mới ở lần poll tiếp theo
```

---

## 📁 CẤU TRÚC CODE JAVA

### **1. Model Layer (POJO/Entity)**

#### **ESP32SensorPayload.java**
DTO nhận dữ liệu từ ESP32.
```java
- deviceId (String) - VD: "DEVICE001"
- temperature, humidity (Double)
- mq135, mq7, mq2 (Double) - Giá trị cảm biến MQ
- dust (Double) - PM2.5
- wifiSignal, uptime (Double) - Optional
```

#### **SensorData.java**
Entity mapping bảng `SensorData`.
```java
- dataId (Long, PK)
- deviceId (Integer, FK)
- sensorTypeId (Integer, FK)
- value (Float)
- createdAt (LocalDateTime)
```

#### **Threshold.java**
Entity mapping bảng `Threshold`.
```java
- thresholdId (Integer, PK)
- sensorTypeId (Integer, FK)
- levelName (String) - VD: "Bình thường"
- minValue, maxValue (Float)
- alertLevel (Integer, 0-3)
- message (String)
```

#### **DashboardData.java**
DTO để hiển thị trên dashboard (grouped by timestamp).
```java
- deviceId (Integer)
- timestamp (LocalDateTime)
- sensorValues (Map<String, Double>):
  * "temperature", "humidity", "mq1", "mq2", "mq3", "dust", "wifiSignal", "uptime"
- aqi, aqiLevel, aqiColor, mainPollutant (AQI fields)
```

#### **AQIResult.java**
Entity mapping bảng `AQI_Result`.
```java
- id (Integer, PK)
- deviceId (Integer, FK)
- pm25, co, gas, temperature, humidity (Float)
- aqi (Float)
- aqiLevel, aqiColor, mainPollutant (String)
- createdAt (LocalDateTime)
```

### **2. DAO Layer (Data Access Object)**

Tất cả DAO sử dụng **JPA/Hibernate** (EntityManager).

#### **SensorDataDAO.java**
```java
- insertBatch(List<SensorData>) - Lưu nhiều records cùng lúc
- findLatestByDevice(deviceId, limit) - Lấy N records mới nhất
- countByDevice(deviceId) - Đếm số records
```

#### **ThresholdDAO.java**
```java
- findBySensorTypeId(sensorTypeId) - Lấy tất cả levels của 1 sensor
- findById(thresholdId)
- update(threshold) - Gọi Stored Procedure UpdateThreshold
- insert(threshold), delete(thresholdId)
```

#### **AQIResultDAO.java**
```java
- insert(AQIResult) - Lưu kết quả AQI
- findLatestByDevice(deviceId, limit) - Lấy N records mới nhất
```

#### **DeviceInfoDAO.java**
```java
- findAll() - Lấy tất cả devices
- findById(deviceId)
- findByName(deviceName)
- insert(DeviceInfo), update(DeviceInfo)
- updateLastSeen(deviceId)
```

#### **SensorTypeDAO.java**
```java
- findAll() - Lấy tất cả sensor types
- findByCode(sensorCode) - VD: "TEMP_DHT11"
- findById(sensorTypeId)
```

### **3. Service Layer (Business Logic)**

#### **SensorService.java**
```java
saveSensorDataFromESP32(ESP32SensorPayload payload):
  1. Parse deviceId (String → Integer hoặc tìm theo tên)
  2. Map ESP32 fields → SensorCode:
     - temperature → TEMP_DHT11
     - humidity → HUM_DHT11
     - mq135 → MQ135
     - mq7 → MQ7
     - mq2 → MQ2
     - dust → GP2Y10
  3. Tạo 6 SensorData records (cùng timestamp)
  4. Lưu batch vào database
  5. Tính AQI và lưu vào AQI_Result
  6. Update Device.LastUpdate

calculateAndSaveAQI(deviceId, payload, timestamp):
  1. Tính AQI = (PM25/500)*500 + (CO/2000)*200 + (Gas/10000)*150
  2. Xác định AQI_Level, AQI_Color, MainPollutant
  3. Lưu vào AQI_Result
```

#### **ThresholdService.java**
```java
getThresholdMapForESP32(deviceId):
  - Trả về Map<String, Double>:
    * "temperature" → MaxValue của level "Bình thường"
    * "humidity" → MaxValue của level "Bình thường"
    * "mq3" → MaxValue của MQ2 (Gas/LPG) level "An toàn"
    * "mq2" → MaxValue của MQ7 (CO) level "An toàn"
    * "dust" → MaxValue của GP2Y10 (PM2.5) level "Tốt"

updateThresholds(deviceId, thresholdMap):
  - Với mỗi sensor, tìm threshold có LevelName phù hợp
  - Update MaxValue
  - Gọi ThresholdDAO.update() → Stored Procedure
```

### **4. Controller Layer (Servlet)**

#### **DataServlet.java** - `POST /api/data`
Nhận dữ liệu từ ESP32.
```java
doPost():
  1. Parse JSON → ESP32SensorPayload
  2. Validate deviceId
  3. Gọi SensorService.saveSensorDataFromESP32()
  4. Trả về JSON: {"status":"ok","received":true}
```

#### **PollServlet.java** - `GET /api/poll?deviceId=...`
ESP32 lấy ngưỡng và commands.
```java
doGet():
  1. Parse deviceId (String → Integer)
  2. Lấy simple thresholds (backward compatibility)
  3. Lấy multi-level thresholds (getMultiLevelThresholds())
  4. Lấy pending commands
  5. Trả về JSON với "thresholds" và "settings"

getMultiLevelThresholds():
  - Query tất cả SensorType
  - Với mỗi SensorType, lấy tất cả Threshold
  - Map SensorCode → standard name:
    * TEMP_DHT11 → "temperature"
    * HUM_DHT11 → "humidity"
    * MQ2 → "mq3" (Gas/LPG)
    * MQ7 → "mq2" (CO)
    * MQ135 → "mq135"
    * GP2Y10 → "pm25"
  - Trả về Map<String, List<Map<String, Object>>>
```

#### **DashboardServlet.java** - `GET /dashboard?deviceId=...`
Hiển thị dashboard.
```java
doGet():
  1. Parse deviceId (có thể là số hoặc tên)
  2. Load SensorData (100 records mới nhất)
  3. Load AQIResult (100 records mới nhất)
  4. Convert SensorData → DashboardData (group by timestamp)
  5. Map AQI data vào DashboardData
  6. Forward đến dashboard.jsp hoặc trả về JSON (nếu ?format=json)
```

#### **ThresholdAdminServlet.java** - `GET/POST /admin/thresholds`
Quản lý ngưỡng.
```java
doGet():
  1. Load current thresholds
  2. Forward đến thresholds.jsp

doPost():
  1. Parse form parameters
  2. Gọi ThresholdService.updateThresholds()
  3. Redirect về GET với message
```

---

## 🔌 API ENDPOINTS

### **1. POST /api/data**
ESP32 gửi dữ liệu cảm biến.

**Request:**
```json
{
  "deviceId": "DEVICE001",
  "temperature": 25.5,
  "humidity": 60.0,
  "mq135": 250.0,
  "mq7": 180.0,
  "mq2": 300.0,
  "dust": 12.5
}
```

**Response:**
```json
{
  "status": "ok",
  "received": true,
  "message": "Data saved successfully"
}
```

### **2. GET /api/poll?deviceId=DEVICE001**
ESP32 lấy ngưỡng và commands.

**Response:**
```json
{
  "update": true,
  "settings": {
    "temperature": 32.0,
    "humidity": 70.0,
    "dust": 12.0,
    "mq3": 300.0,
    "mq2": 50.0
  },
  "thresholds": {
    "temperature": [
      {
        "levelName": "Lạnh",
        "minValue": -40.0,
        "maxValue": 18.0,
        "alertLevel": 0,
        "message": "Nhiệt độ thấp"
      },
      {
        "levelName": "Bình thường",
        "minValue": 18.0,
        "maxValue": 32.0,
        "alertLevel": 0,
        "message": "Nhiệt độ thoải mái"
      },
      ...
    ],
    "humidity": [...],
    "pm25": [...],
    "mq135": [...],
    "mq3": [...],
    "mq2": [...]
  },
  "commands": []
}
```

### **3. GET /dashboard?deviceId=1**
Hiển thị dashboard (JSP).

### **4. GET /dashboard?deviceId=1&format=json**
Lấy dữ liệu dashboard dạng JSON (cho AJAX).

**Response:**
```json
{
  "deviceId": 1,
  "data": [
    {
      "deviceId": 1,
      "timestamp": "2025-11-15T21:22:00",
      "temperature": 25.5,
      "humidity": 60.0,
      "mq1": 250.0,
      "mq2": 180.0,
      "mq3": 300.0,
      "dust": 12.5,
      "wifiSignal": -65,
      "uptime": 123456,
      "aqi": 65.5,
      "aqiLevel": "Trung bình",
      "aqiColor": "#f59e0b",
      "mainPollutant": "PM2.5"
    },
    ...
  ]
}
```

### **5. GET /admin/thresholds?deviceId=1**
Hiển thị form quản lý ngưỡng (JSP).

### **6. POST /admin/thresholds**
Cập nhật ngưỡng từ form.

---

## 🗺️ MAPPING GIỮA ESP32 VÀ DATABASE

### **Sensor Name Mapping:**

| ESP32 Field | Database SensorCode | SensorTypeID | Mô tả |
|-------------|---------------------|--------------|-------|
| `temperature` | `TEMP_DHT11` | 1 | Nhiệt độ |
| `humidity` | `HUM_DHT11` | 2 | Độ ẩm |
| `mq135` | `MQ135` | 5 | Chất lượng không khí |
| `mq7` | `MQ7` | 4 | Khí CO |
| `mq2` | `MQ2` | 3 | Gas/LPG |
| `dust` | `GP2Y10` | 6 | PM2.5 |

### **Threshold Response Mapping (PollServlet → ESP32):**

| Database SensorCode | Response Key (in "thresholds") | ESP32 Variable |
|---------------------|-------------------------------|----------------|
| `TEMP_DHT11` | `"temperature"` | `thresholds.temperature` |
| `HUM_DHT11` | `"humidity"` | `thresholds.humidity` |
| `MQ2` | `"mq3"` | `thresholds.mq2` (Gas/LPG) |
| `MQ7` | `"mq2"` | `thresholds.co` (CO) |
| `MQ135` | `"mq135"` | `thresholds.mq135` |
| `GP2Y10` | `"pm25"` | `thresholds.pm25` |

**Lưu ý:** ESP32 sử dụng `mq2` cho CO (MQ-7) và `mq3` cho Gas/LPG (MQ-2), nhưng database dùng `MQ7` và `MQ2`. PollServlet phải map đúng.

---

## 🎨 FRONTEND (JSP + JavaScript)

### **dashboard.jsp**
- **Metric Cards:** Hiển thị giá trị hiện tại (Nhiệt độ, Độ ẩm, AQI, Wi-Fi)
- **Charts:** Chart.js để vẽ biểu đồ (Nhiệt độ & Độ ẩm, MQ Sensors)
- **AI Prediction Section:** Khung dự đoán 1 giờ tới (placeholder, sẵn sàng tích hợp AI)
- **Table:** Bảng dữ liệu cảm biến gần nhất (10 mục)
- **Real-time Updates:** JavaScript AJAX mỗi 5 giây

### **thresholds.jsp**
- Form để admin cập nhật ngưỡng cho từng sensor
- Hiển thị giá trị hiện tại
- Submit POST đến `/admin/thresholds`

---

## 🔐 AUTHENTICATION & AUTHORIZATION

- **AuthenticationFilter:** Kiểm tra session trước khi vào dashboard/admin
- **LoginServlet:** Xử lý đăng nhập
- **UserDAO:** Kiểm tra username/password (hash)

---

## 📝 CÁC CHỨC NĂNG CHÍNH

### ✅ **1. Thu thập dữ liệu cảm biến (Data Collection)**
- ESP32 đọc cảm biến mỗi 5 giây
- Gửi JSON đến `/api/data`
- Web server lưu vào `SensorData` (6 records/lần)
- Tính AQI và lưu vào `AQI_Result`

### ✅ **2. Quản lý ngưỡng (Threshold Management)**
- Multi-level thresholds (nhiều mức cho mỗi sensor)
- Admin cập nhật qua web interface
- ESP32 polling mỗi 10 giây để lấy ngưỡng mới
- Lịch sử cập nhật được ghi vào `ThresholdUpdateLog`

### ✅ **3. Điều khiển thiết bị (Device Control)**
- ESP32 tự động bật/tắt Quạt và Chuông dựa trên ngưỡng
- Logic phức tạp: AQI, AlertLevel, kết hợp nhiều sensor
- Có thể gửi commands từ web (chưa implement đầy đủ)

### ✅ **4. Dashboard hiển thị (Real-time Dashboard)**
- Metric cards: Giá trị hiện tại
- Charts: Biểu đồ 10 lần đọc gần nhất
- Table: Bảng dữ liệu chi tiết
- Real-time updates: AJAX mỗi 5 giây
- AQI display: Màu sắc và badge theo level

### ✅ **5. Tính toán AQI (Air Quality Index)**
- Công thức: `AQI = (PM25/500)*500 + (CO/2000)*200 + (Gas/10000)*150`
- Xác định level, color, main pollutant
- Lưu vào `AQI_Result` mỗi lần nhận dữ liệu

### ✅ **6. AI Prediction Section (Placeholder)**
- Khung hiển thị dự đoán 1 giờ tới
- CSS sẵn sàng, chờ tích hợp AI model
- Có example HTML structure trong comment

---

## 🚀 CÁCH CHẠY DỰ ÁN

### **1. Database Setup:**
```sql
-- Chạy sql_script.txt trong SQL Server
-- Tạo database AirQualityManagement
-- Tạo tables, insert default data
```

### **2. Cấu hình Database:**
```xml
<!-- src/main/resources/META-INF/persistence.xml -->
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:sqlserver://localhost:1433;databaseName=AirQualityManagement;..."/>
```

### **3. Build & Deploy:**
```bash
mvn clean package
# Copy WAR file vào Tomcat webapps/
```

### **4. ESP32 Configuration:**
```cpp
// ESP32_WITH_THRESHOLDS.ino
const char* serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data";
const char* pollUrl   = "http://192.168.137.1:8080/IoTWebApp/api/poll";
const char* deviceId  = "DEVICE001";
```

### **5. Truy cập Web:**
- Dashboard: `http://localhost:8080/IoTWebApp/dashboard`
- Admin: `http://localhost:8080/IoTWebApp/admin/thresholds`

---

## 📌 LƯU Ý QUAN TRỌNG

1. **DeviceId Type:** Database dùng `INT`, nhưng ESP32 gửi `String` ("DEVICE001"). Code phải convert hoặc tìm theo tên.

2. **Timestamp Synchronization:** Tất cả sensors từ cùng 1 payload phải có cùng `CreatedAt` để group đúng trên dashboard.

3. **Sensor Mapping:** ESP32 và Database dùng tên khác nhau (mq2/mq3 vs MQ7/MQ2). Phải map đúng ở PollServlet và SensorService.

4. **Multi-level Thresholds:** ESP32 hỗ trợ nhiều mức ngưỡng, nhưng vẫn có backward compatibility với single value ("settings").

5. **AQI Calculation:** Công thức AQI ở ESP32 và Java phải giống nhau để đồng bộ.

6. **UTF-8 Encoding:** JSON response phải set charset UTF-8 để hiển thị tiếng Việt đúng.

---

## 🎯 TÓM TẮT

Dự án này là một **hệ thống IoT hoàn chỉnh** với:
- ✅ ESP32 firmware đọc cảm biến và điều khiển thiết bị
- ✅ Java Web Application quản lý dữ liệu và ngưỡng
- ✅ SQL Server lưu trữ dữ liệu và cấu hình
- ✅ REST API giao tiếp giữa ESP32 và Web
- ✅ Dashboard real-time hiển thị dữ liệu
- ✅ Multi-level threshold system
- ✅ AQI calculation và display
- ✅ Admin interface để quản lý ngưỡng

**Kiến trúc:** MVC pattern với DAO, Service, Controller layers, sử dụng JPA/Hibernate cho database access.

