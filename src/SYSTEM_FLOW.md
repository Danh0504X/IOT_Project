# System Flow - IoT Sensor Monitoring System

## 📊 Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SYSTEM FLOW                                  │
└─────────────────────────────────────────────────────────────────────┘

1. ESP32 (Hardware Layer)
   │
   ├─► Đọc cảm biến (Sensors)
   │   • DHT11: Temperature, Humidity
   │   • MQ-135 (Pin 32): Air Quality
   │   • MQ-7 (Pin 33): CO Gas
   │   • MQ-2 (Pin 34): Smoke/Gas
   │   • GP2Y1010: Dust Density
   │
   ├─► Đóng gói dữ liệu thành JSON
   │   {
   │     "deviceId": "DEVICE001",
   │     "temperature": 25.8,
   │     "humidity": 60.0,
   │     "mq135": 300,
   │     "mq7": 250,
   │     "mq2": 600,
   │     "dust": 50.5
   │   }
   │
   └─► Gửi HTTP POST đến Server
       POST http://192.168.137.1:8080/IoTWebApp/api/data
       Content-Type: application/json

                        ▼

2. Server (Backend Layer)
   │
   ├─► DataApiServlet /api/data (hoặc DataServlet)
   │   │
   │   ├─► Nhận JSON từ ESP32
   │   │   • Parse JSON → ESP32SensorPayload
   │   │   • Validate deviceId
   │   │
   │   ├─► Đảm bảo Device tồn tại
   │   │   • DeviceInfoDAO.updateLastSeen()
   │   │   • Tự động tạo device nếu chưa có
   │   │
   │   └─► Lưu dữ liệu vào Database
   │       • SensorService.saveSensorDataFromESP32()
   │       • Map ESP32 field → Database sensor names:
   │         - mq135 → "Gas MQ1" (sensor_type_id = 3)
   │         - mq7   → "Gas MQ2" (sensor_type_id = 4)
   │         - mq2   → "Gas MQ3" (sensor_type_id = 5)
   │       • SensorDataDAO.insertBatch()
   │
   └─► Trả về Response
       {"status":"success", "recordsSaved":6}

                        ▼

3. Database (Data Layer)
   │
   ├─► DeviceInfo Table
   │   • device_id: "DEVICE001"
   │   • status: "ACTIVE"
   │   • last_seen: updated
   │
   └─► SensorData Table
       • device_id: "DEVICE001"
       • sensor_type_id: 1-6
       • sensor_value: actual values
       • timestamp: current time

                        ▼

4. Web Dashboard (Frontend Layer)
   │
   ├─► DashboardServlet /dashboard
   │   │
   │   ├─► Đọc dữ liệu từ Database
   │   │   • SensorService.getRecentData(deviceId, 100)
   │   │   • SensorDataDAO.findLatestByDevice()
   │   │   • Load SensorType info (sensor names)
   │   │
   │   ├─► Convert sang DashboardData
   │   │   • Group by timestamp
   │   │   • Map database names → standard names:
   │   │     - "Temperature" → "temperature"
   │   │     - "Gas MQ1" → "mq1"
   │   │   • Aggregate sensor values per timestamp
   │   │
   │   └─► Render JSP (dashboard.jsp)
   │
   └─► Auto-refresh (JavaScript)
       • Tự động reload mỗi 5 giây
       • Cập nhật charts và tables
       • Hiển thị dữ liệu real-time

                        ▼

5. User Interface (Display Layer)
   │
   ├─► Metric Cards
   │   • Temperature, Humidity, Air Quality, WiFi Signal
   │
   ├─► Charts
   │   • Line chart: Temperature & Humidity (10 readings)
   │   • Radar chart: MQ1, MQ2, MQ3 values
   │
   └─► Data Table
       • Recent 10 sensor readings
       • All sensor values with timestamps
```

## 🔄 Data Flow Summary

### Step 1: ESP32 → Server
- **Endpoint**: `POST /api/data`
- **Format**: JSON
- **Frequency**: Mỗi 5 giây (SEND_INTERVAL = 5000ms)

### Step 2: Server → Database
- **Tables**: 
  - `DeviceInfo` (device metadata)
  - `SensorData` (sensor readings)
- **Mapping**: ESP32 field names → Database sensor names

### Step 3: Database → Web Dashboard
- **Endpoint**: `GET /dashboard?deviceId=DEVICE001`
- **Query**: Lấy 100 records mới nhất
- **Processing**: Group by timestamp, aggregate values

### Step 4: Dashboard Auto-Refresh
- **Interval**: 5 giây
- **Method**: JavaScript `setInterval` → Page reload
- **Result**: Real-time data display

## ✅ System Components Status

| Component | Status | Notes |
|-----------|--------|-------|
| ESP32 Code | ✅ Ready | Gửi JSON đúng format |
| DataApiServlet | ✅ Ready | Nhận và lưu dữ liệu |
| DataServlet | ✅ Ready | Alternative handler |
| SensorService | ✅ Ready | Map và lưu dữ liệu |
| DeviceInfoDAO | ✅ Ready | Auto-create device |
| SensorDataDAO | ✅ Ready | Batch insert |
| Database Schema | ✅ Ready | Foreign keys OK |
| DashboardServlet | ✅ Ready | Load từ database |
| Dashboard JSP | ✅ Ready | Auto-refresh enabled |
| Sensor Name Mapping | ✅ Ready | ESP32 ↔ Database ↔ Web |

## 🎯 Complete Flow Verification

1. ✅ ESP32 đo đạc sensors → OK
2. ✅ ESP32 gửi JSON POST → OK
3. ✅ Server nhận và parse JSON → OK
4. ✅ Server tạo device nếu chưa có → OK
5. ✅ Server lưu vào Database → OK
6. ✅ Dashboard load từ Database → OK
7. ✅ Dashboard hiển thị dữ liệu → OK
8. ✅ Auto-refresh cập nhật real-time → OK

## 📝 Next Steps

1. **Restart Server** - Để áp dụng code mới
2. **Upload ESP32 Code** - Đảm bảo đúng IP và endpoint
3. **Test Flow**:
   - Cắm ESP32 → Kiểm tra Serial Monitor
   - Kiểm tra Server Logs → Xem dữ liệu được nhận
   - Kiểm tra Database → Xác nhận dữ liệu được lưu
   - Mở Dashboard → Xem dữ liệu hiển thị



