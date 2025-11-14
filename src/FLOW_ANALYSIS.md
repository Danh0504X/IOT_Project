# 📊 PHÂN TÍCH FLOW: ESP32 → Database → Web Dashboard

## 🔍 PHÂN TÍCH CODE ESP32 (code_aduno.txt)

### Cấu trúc ESP32:
```cpp
// PIN MAPPING:
MQ1_PIN = 32 → MQ-135 sensor → Gửi là "mq135"
MQ2_PIN = 33 → MQ-7 sensor   → Gửi là "mq7"
MQ3_PIN = 34 → MQ-2 sensor   → Gửi là "mq2"
```

### JSON ESP32 gửi:
```json
{
  "deviceId": "DEVICE001",
  "temperature": 25.8,
  "humidity": 60.0,
  "mq135": 300,    // ← MQ1_PIN (32) MQ-135 sensor
  "mq7": 250,      // ← MQ2_PIN (33) MQ-7 sensor
  "mq2": 600,      // ← MQ3_PIN (34) MQ-2 sensor
  "dust": 50.5
}
```

### Endpoint:
- **URL**: `POST http://192.168.137.1:8080/IoTWebApp/api/data`
- **Frequency**: Mỗi 5 giây (SEND_INTERVAL = 5000ms)

---

## ✅ FLOW 1: ESP32 → Server → Database

### Bước 1: ESP32 gửi dữ liệu
```
ESP32 readAndSend()
  ↓
Đọc sensors:
  - DHT11: temperature, humidity
  - MQ1_PIN (32): analogRead() → mq1 value → gửi là "mq135"
  - MQ2_PIN (33): analogRead() → mq2 value → gửi là "mq7"
  - MQ3_PIN (34): analogRead() → mq3 value → gửi là "mq2"
  - Dust: GP2Y1010 → dustDensity
  ↓
Tạo JSON với fields: deviceId, temperature, humidity, mq135, mq7, mq2, dust
  ↓
POST http://192.168.137.1:8080/IoTWebApp/api/data
```

### Bước 2: Server nhận và xử lý

**DataApiServlet** (đang được gọi - endpoint `/api/data-legacy`):
```java
POST /api/data-legacy
  ↓
Parse JSON → JSONObject
  ↓
DeviceInfoDAO.updateLastSeen("DEVICE001") → Tự động tạo device nếu chưa có
  ↓
Lưu từng sensor:
  - json.getDouble("temperature") → sensor_type_id = 1
  - json.getDouble("humidity")    → sensor_type_id = 2
  - json.getDouble("mq135")       → sensor_type_id = 3  ✅
  - json.getDouble("mq7")         → sensor_type_id = 4  ✅
  - json.getDouble("mq2")         → sensor_type_id = 5  ✅
  - json.getDouble("dust")        → sensor_type_id = 6
  ↓
SensorDataDAO.insert() → Lưu vào SQL Server
```

**DataServlet** (endpoint `/api/data` - nên dùng):
```java
POST /api/data
  ↓
Parse JSON → ESP32SensorPayload (Gson)
  ↓
SensorService.saveSensorDataFromESP32(payload)
  ↓
Map ESP32 fields → Database sensor names:
  - payload.getTemperature() → "Temperature" (sensor_type_id = 1)
  - payload.getHumidity()    → "Humidity" (sensor_type_id = 2)
  - payload.getMq135()       → "Gas MQ1" (sensor_type_id = 3)  ✅
  - payload.getMq7()         → "Gas MQ2" (sensor_type_id = 4)  ✅
  - payload.getMq2()         → "Gas MQ3" (sensor_type_id = 5)  ✅
  - payload.getDust()        → "Dust Density" (sensor_type_id = 6)
  ↓
SensorDataDAO.insertBatch() → Lưu vào SQL Server
  ↓
DeviceInfoDAO.updateLastSeen() → Update device
```

### Bước 3: Lưu vào Database

**Tables:**
```sql
-- DeviceInfo
device_id = "DEVICE001"
status = "ACTIVE"
last_seen = GETDATE()

-- SensorData (6 records được tạo)
Record 1: device_id="DEVICE001", sensor_type_id=1, sensor_value=25.8  -- Temperature
Record 2: device_id="DEVICE001", sensor_type_id=2, sensor_value=60.0  -- Humidity
Record 3: device_id="DEVICE001", sensor_type_id=3, sensor_value=300   -- Gas MQ1 (mq135)
Record 4: device_id="DEVICE001", sensor_type_id=4, sensor_value=250   -- Gas MQ2 (mq7)
Record 5: device_id="DEVICE001", sensor_type_id=5, sensor_value=600   -- Gas MQ3 (mq2)
Record 6: device_id="DEVICE001", sensor_type_id=6, sensor_value=50.5  -- Dust Density
```

---

## ✅ FLOW 2: Database → Web Dashboard

### Bước 1: DashboardServlet load dữ liệu
```
GET /dashboard?deviceId=DEVICE001
  ↓
DashboardServlet.doGet()
  ↓
SensorService.getRecentData("DEVICE001", 100)
  ↓
SensorDataDAO.findLatestByDevice()
  ↓
SQL Query:
  SELECT sd FROM SensorData sd 
  WHERE sd.deviceId = 'DEVICE001' 
  ORDER BY sd.timestamp DESC
  LIMIT 100
  ↓
Load SensorType info:
  - sensor_type_id = 1 → sensorName = "Temperature"
  - sensor_type_id = 2 → sensorName = "Humidity"
  - sensor_type_id = 3 → sensorName = "Gas MQ1"
  - sensor_type_id = 4 → sensorName = "Gas MQ2"
  - sensor_type_id = 5 → sensorName = "Gas MQ3"
  - sensor_type_id = 6 → sensorName = "Dust Density"
```

### Bước 2: Convert sang DashboardData
```
List<SensorData> rawData (100 records, mỗi record 1 sensor)
  ↓
Group by timestamp
  ↓
Convert từng group:
  - Group 1 (timestamp = T1):
    • "Temperature" (25.8) → mapDatabaseNameToStandard() → "temperature" = 25.8
    • "Humidity" (60.0)    → "humidity" = 60.0
    • "Gas MQ1" (300)      → "mq1" = 300
    • "Gas MQ2" (250)      → "mq2" = 250
    • "Gas MQ3" (600)      → "mq3" = 600
    • "Dust Density" (50.5)→ "dust" = 50.5
    ↓
  DashboardData {
    deviceId: "DEVICE001",
    timestamp: T1,
    sensorValues: {
      "temperature": 25.8,
      "humidity": 60.0,
      "mq1": 300,
      "mq2": 250,
      "mq3": 600,
      "dust": 50.5
    }
  }
```

### Bước 3: Render lên JSP
```
dashboard.jsp
  ↓
Hiển thị:
  - Metric Cards: ${sensorData[0].temperature}, ${sensorData[0].humidity}...
  - Charts: temps[], humidities[], mq1[], mq2[], mq3[]
  - Table: <c:forEach items="${sensorData}">...
  ↓
Auto-refresh mỗi 5 giây → Reload dashboard → Load dữ liệu mới từ database
```

---

## ✅ MAPPING HOÀN CHỈNH

| ESP32 Pin | ESP32 Code Variable | ESP32 JSON Field | Database Sensor Name | Database sensor_type_id | Dashboard Display |
|-----------|---------------------|------------------|----------------------|------------------------|-------------------|
| Pin 13 (DHT11) | temperature | `temperature` | "Temperature" | 1 | temperature |
| Pin 13 (DHT11) | humidity | `humidity` | "Humidity" | 2 | humidity |
| Pin 32 (MQ1_PIN) | mq1 | `mq135` | "Gas MQ1" | 3 | mq1 |
| Pin 33 (MQ2_PIN) | mq2 | `mq7` | "Gas MQ2" | 4 | mq2 |
| Pin 34 (MQ3_PIN) | mq3 | `mq2` | "Gas MQ3" | 5 | mq3 |
| Pin 35 (GP2Y1010) | dustDensity | `dust` | "Dust Density" | 6 | dust |

---

## ✅ XÁC NHẬN FLOW ĐÚNG

### ✅ Flow 1: ESP32 → Database
1. ✅ ESP32 đọc sensors đúng
2. ✅ ESP32 gửi JSON đúng format (mq135, mq7, mq2)
3. ✅ Server nhận đúng endpoint `/api/data`
4. ✅ DataApiServlet/DataServlet parse đúng
5. ✅ Device được tự động tạo nếu chưa có
6. ✅ Mapping sensor names đúng:
   - mq135 → sensor_type_id = 3 ("Gas MQ1")
   - mq7 → sensor_type_id = 4 ("Gas MQ2")
   - mq2 → sensor_type_id = 5 ("Gas MQ3")
7. ✅ Dữ liệu được lưu vào SensorData table

### ✅ Flow 2: Database → Web Dashboard
1. ✅ DashboardServlet load từ database đúng
2. ✅ SensorDataDAO query đúng
3. ✅ Load SensorType names đúng
4. ✅ Mapping database names → standard names đúng:
   - "Gas MQ1" → "mq1"
   - "Gas MQ2" → "mq2"
   - "Gas MQ3" → "mq3"
5. ✅ Group by timestamp đúng
6. ✅ Dashboard hiển thị đúng
7. ✅ Auto-refresh mỗi 5 giây hoạt động

---

## 🎯 KẾT LUẬN

**FLOW CỦA BẠN HOÀN TOÀN ĐÚNG:**

1. ✅ **ESP32 đo đạc** → Đọc sensors, tạo JSON
2. ✅ **Gửi lên Server** → POST /api/data với JSON
3. ✅ **Server lưu Database** → Parse JSON, map names, insert vào SQL Server
4. ✅ **Database lưu trữ** → SensorData table với đầy đủ thông tin
5. ✅ **Dashboard load Database** → Query dữ liệu mới nhất
6. ✅ **Hiển thị lên Web** → Render JSP với charts và tables
7. ✅ **Auto-refresh** → Tự động cập nhật mỗi 5 giây

**Tất cả mapping đều đúng và khớp với nhau!** 🎉


