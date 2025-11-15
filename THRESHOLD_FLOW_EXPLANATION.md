# 🔄 GIẢI THÍCH LUỒNG THRESHOLD: ESP32 ↔ Database ↔ Web Server

## ❌ SAI LẦM THƯỜNG GẶP

**ESP32 KHÔNG tra dữ liệu mới cho threshold qua database!**

ESP32 **CHỈ ĐỌC** threshold từ web server, không ghi vào database.

---

## ✅ LUỒNG HOẠT ĐỘNG ĐÚNG

### **Sơ Đồ Tổng Quan:**

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Admin     │         │  Web Server  │         │  Database   │
│  (Browser)  │────────▶│  (Java App)  │────────▶│ (SQL Server)│
└─────────────┘         └──────┬───────┘         └─────────────┘
                               │
                               │ GET /api/poll
                               │ (JSON Response)
                               ▼
                          ┌─────────────┐
                          │    ESP32    │
                          │  (Firmware) │
                          └─────────────┘
```

---

## 📋 CHI TIẾT TỪNG BƯỚC

### **BƯỚC 1: Admin Cập Nhật Ngưỡng (Ghi vào Database)**

**Người dùng:**
1. Truy cập: `http://server:8080/IoTWebApp/admin/thresholds?deviceId=1`
2. Nhập giá trị mới (VD: `temperature = 35.0`)
3. Click "Lưu thay đổi"

**Web Server (ThresholdAdminServlet):**
```java
// POST /admin/thresholds
1. Nhận form data: {temperature: 35.0, humidity: 70.0, ...}
2. Gọi ThresholdService.updateThresholds(deviceId, thresholds)
3. ThresholdService tìm threshold có LevelName = "Bình thường"
4. Cập nhật MaxValue = 35.0
5. Gọi ThresholdDAO.update() → Stored Procedure UpdateThreshold
6. Database: UPDATE Threshold SET MaxValue = 35.0 WHERE ...
```

**Database:**
```sql
-- Giá trị mới được lưu vào bảng Threshold
UPDATE Threshold 
SET MaxValue = 35.0 
WHERE SensorTypeID = 1 AND LevelName = N'Bình thường';
```

**Kết quả:** Database đã có ngưỡng mới ✅

---

### **BƯỚC 2: ESP32 Polling Lấy Ngưỡng (Đọc từ Database qua Web Server)**

**ESP32 (mỗi 10 giây):**
```cpp
// ESP32_WITH_THRESHOLDS.ino
void loop() {
    if (currentMillis - lastPoll >= POLL_INTERVAL) {  // 10 giây
        fetchThresholds();  // Gọi hàm lấy ngưỡng
    }
}

void fetchThresholds() {
    // 1. Gửi HTTP GET request
    String url = "http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001";
    http.begin(client, url);
    int code = http.GET();  // ← ESP32 GỬI REQUEST
    
    // 2. Nhận JSON response
    String response = http.getString();
    // Response: {"update":true, "thresholds":{...}, "settings":{...}}
    
    // 3. Parse JSON và cập nhật thresholds trong memory
    if (doc.containsKey("thresholds")) {
        JsonObject thresholdsObj = doc["thresholds"];
        // Parse temperature, humidity, mq135, ...
        // Cập nhật vào biến thresholds (trong memory của ESP32)
    }
}
```

**Web Server (PollServlet):**
```java
// GET /api/poll?deviceId=DEVICE001
1. Nhận request từ ESP32
2. Gọi ThresholdService.getThresholdMapForESP32(deviceId)
3. ThresholdService đọc từ Database:
   - ThresholdDAO.findBySensorTypeId() → Query database
   - Lấy tất cả thresholds từ bảng Threshold
4. Convert sang format JSON
5. Trả về JSON response cho ESP32
```

**Database Query:**
```sql
-- PollServlet đọc từ database
SELECT ThresholdID, SensorTypeID, LevelName, MinValue, MaxValue, AlertLevel, Message
FROM Threshold
WHERE SensorTypeID IN (1, 2, 3, 4, 5, 6)  -- Tất cả sensors
ORDER BY SensorTypeID, MinValue;
```

**JSON Response gửi về ESP32:**
```json
{
  "update": true,
  "thresholds": {
    "temperature": [
      {"levelName": "Lạnh", "minValue": -40, "maxValue": 18, "alertLevel": 0, ...},
      {"levelName": "Bình thường", "minValue": 18, "maxValue": 35, "alertLevel": 0, ...},
      {"levelName": "Nóng", "minValue": 35, "maxValue": 39, "alertLevel": 1, ...},
      {"levelName": "Rất nóng", "minValue": 39, "maxValue": 80, "alertLevel": 2, ...}
    ],
    "humidity": [...],
    "pm25": [...],
    ...
  },
  "settings": {
    "temperature": 35.0,
    "humidity": 70.0,
    ...
  }
}
```

**ESP32 nhận và cập nhật:**
```cpp
// ESP32 parse JSON và cập nhật vào memory
thresholds.temperature.levels[1].maxValue = 35.0;  // ← Cập nhật trong RAM
thresholds.loaded = true;  // Đánh dấu đã load
```

**Kết quả:** ESP32 đã có ngưỡng mới trong memory ✅

---

### **BƯỚC 3: ESP32 Sử Dụng Ngưỡng Để Kiểm Tra**

**ESP32 (mỗi 5 giây):**
```cpp
void readAndSend() {
    // 1. Đọc cảm biến
    float temperature = dht.readTemperature();  // VD: 36.5°C
    
    // 2. Kiểm tra ngưỡng (sử dụng thresholds trong memory)
    checkThresholds(temperature, humidity, mq135, mq7, mq2, pm25);
}

void checkThresholds(...) {
    // Tìm AlertLevel dựa trên giá trị đọc được
    int tempAlert = findAlertLevel(temperature, thresholds.temperature, ...);
    // VD: temperature = 36.5 → nằm trong khoảng 35-39 → AlertLevel = 1 (Nóng)
    
    // 3. Quyết định bật/tắt Quạt/Chuông
    if (tempAlert >= 2) {
        digitalWrite(FAN_PIN, LOW);  // Bật quạt
    }
}
```

**Kết quả:** ESP32 sử dụng ngưỡng mới để điều khiển thiết bị ✅

---

## 🔍 TÓM TẮT LUỒNG

### **1. Admin Set Ngưỡng Mới:**
```
Admin (Browser)
    ↓ POST /admin/thresholds
Web Server (ThresholdAdminServlet)
    ↓ UPDATE Threshold
Database (Threshold table)
    ✅ Ngưỡng mới được lưu
```

### **2. ESP32 Lấy Ngưỡng:**
```
ESP32
    ↓ GET /api/poll?deviceId=DEVICE001
Web Server (PollServlet)
    ↓ SELECT FROM Threshold
Database (Threshold table)
    ↓ Trả về JSON
Web Server
    ↓ JSON Response
ESP32
    ✅ Ngưỡng mới được load vào memory
```

### **3. ESP32 Sử Dụng Ngưỡng:**
```
ESP32
    ↓ Đọc cảm biến
    ↓ So sánh với thresholds (trong memory)
    ↓ Quyết định bật/tắt Quạt/Chuông
    ✅ Điều khiển thiết bị
```

---

## ❓ TRẢ LỜI CÂU HỎI

### **Q1: ESP32 có đang tra dữ liệu mới cho threshold qua database không?**

**Trả lời: KHÔNG!**

- ESP32 **KHÔNG GHI** vào database
- ESP32 **CHỈ ĐỌC** threshold từ web server qua API
- ESP32 gửi HTTP GET request → Web server đọc database → Trả về JSON → ESP32 nhận và cập nhật trong memory

### **Q2: Set ngưỡng mới chỉ là gán giá trị vào database và ESP32 đọc nó?**

**Trả lời: ĐÚNG, nhưng có thêm bước trung gian!**

**Luồng đầy đủ:**
1. ✅ Admin set ngưỡng → **Gán giá trị vào Database**
2. ✅ Web Server đọc từ Database → **Convert sang JSON**
3. ✅ ESP32 polling → **Nhận JSON từ Web Server**
4. ✅ ESP32 parse JSON → **Cập nhật vào memory**
5. ✅ ESP32 sử dụng thresholds trong memory → **Kiểm tra và điều khiển**

**Lưu ý:**
- ESP32 **KHÔNG đọc trực tiếp từ database**
- ESP32 đọc qua **Web Server API** (`GET /api/poll`)
- Web Server là **trung gian** giữa ESP32 và Database

---

## 📊 SO SÁNH: ESP32 Gửi Dữ Liệu vs ESP32 Lấy Ngưỡng

### **ESP32 Gửi Dữ Liệu Cảm Biến:**
```
ESP32
    ↓ POST /api/data
    ↓ JSON: {"deviceId":"DEVICE001", "temperature":25.5, ...}
Web Server (DataServlet)
    ↓ Lưu vào SensorData
Database (SensorData table)
    ✅ Dữ liệu được lưu
```
**Hướng:** ESP32 → Web Server → Database (Ghi)

### **ESP32 Lấy Ngưỡng:**
```
ESP32
    ↓ GET /api/poll?deviceId=DEVICE001
Web Server (PollServlet)
    ↓ Đọc từ Threshold
Database (Threshold table)
    ↓ JSON Response
Web Server
    ↓ Trả về JSON
ESP32
    ✅ Ngưỡng được load vào memory
```
**Hướng:** ESP32 → Web Server → Database (Đọc) → Web Server → ESP32

---

## 🎯 KẾT LUẬN

1. **ESP32 KHÔNG tra dữ liệu threshold vào database**
   - ESP32 chỉ đọc, không ghi

2. **Set ngưỡng mới = Gán vào Database + ESP32 đọc qua Web Server**
   - Admin cập nhật → Database
   - ESP32 polling → Web Server đọc Database → Trả về JSON → ESP32 nhận

3. **Web Server là trung gian:**
   - Đọc từ Database
   - Convert sang JSON
   - Trả về cho ESP32

4. **ESP32 lưu thresholds trong memory (RAM):**
   - Không lưu vào database
   - Chỉ lưu trong biến `ThresholdSettings thresholds` (trong code)
   - Mỗi lần polling, cập nhật lại từ server

---

## 🔄 TIMELINE THỰC TẾ

```
T=0s:   Admin cập nhật temperature = 35.0 → Database ✅
T=5s:   ESP32 đọc cảm biến (vẫn dùng ngưỡng cũ: 32.0)
T=10s:  ESP32 polling → Nhận ngưỡng mới: 35.0 → Cập nhật memory ✅
T=15s:  ESP32 đọc cảm biến (đã dùng ngưỡng mới: 35.0) ✅
```

**Delay tối đa:** 10 giây (POLL_INTERVAL) để ESP32 nhận ngưỡng mới.


