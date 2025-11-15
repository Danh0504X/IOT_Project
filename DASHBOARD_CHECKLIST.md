# 📋 CHECKLIST KIỂM TRA DASHBOARD HIỂN THỊ DỮ LIỆU

## ✅ LUỒNG DỮ LIỆU: Database → Dashboard

### 1. **Database Query (SensorDataDAO.findLatestByDevice)**
- ✅ Query lấy 100 records mới nhất theo deviceId
- ✅ ORDER BY createdAt DESC
- ✅ Load SensorType info để có sensorName và unit
- ✅ Logging: In ra số lượng records và thông tin từng sensor

### 2. **Data Conversion (DashboardServlet.convertToDashboardData)**
- ✅ Group các SensorData theo timestamp (cùng timestamp = cùng một lần đọc)
- ✅ Map database sensor name → standard name:
  - "DHT11 - Nhiệt độ" → "temperature"
  - "DHT11 - Độ ẩm" → "humidity"
  - "MQ135 - Chất lượng không khí" → "mq1"
  - "MQ7 - Khí CO" → "mq2"
  - "MQ2 - Gas/LPG" → "mq3"
  - "GP2Y10 - Bụi mịn PM2.5" → "dust"
- ✅ Tạo DashboardData với sensorValues map
- ✅ Sort theo timestamp DESC

### 3. **JSP Rendering**
- ✅ Metric Cards: Hiển thị ${sensorData[0].temperature}, ${sensorData[0].humidity}, ${sensorData[0].dust}
- ✅ Table: Hiển thị tất cả records với <c:forEach>
- ✅ Charts: Khởi tạo với dữ liệu từ JSP, sau đó update qua AJAX

### 4. **AJAX Real-time Update**
- ✅ Fetch từ `/dashboard?deviceId=DEVICE001&format=json`
- ✅ Update metric cards, table, charts mỗi 5 giây
- ✅ Xử lý lỗi và hiển thị thông báo

## ⚠️ CÁC ĐIỂM CẦN KIỂM TRA

### 1. **Mapping Sensor Names**
Kiểm tra xem tên sensor trong database có đúng với mapping không:
- Database: "DHT11 - Nhiệt độ" → Dashboard: "temperature" ✅
- Database: "DHT11 - Độ ẩm" → Dashboard: "humidity" ✅
- Database: "MQ135 - Chất lượng không khí" → Dashboard: "mq1" ✅
- Database: "MQ7 - Khí CO" → Dashboard: "mq2" ✅
- Database: "MQ2 - Gas/LPG" → Dashboard: "mq3" ✅
- Database: "GP2Y10 - Bụi mịn PM2.5" → Dashboard: "dust" ✅

### 2. **Grouping theo Timestamp**
- ✅ Các sensor từ cùng một payload ESP32 phải có cùng timestamp
- ✅ SensorService.saveSensorDataFromESP32() đảm bảo tất cả sensors cùng timestamp
- ✅ Grouping trong convertToDashboardData() group đúng theo timestamp

### 3. **JSP Display Fields**
- ✅ Temperature: ${sensorData[0].temperature} → getTemperature() → sensorValues.get("temperature")
- ✅ Humidity: ${sensorData[0].humidity} → getHumidity() → sensorValues.get("humidity")
- ✅ MQ1: ${sensorData[0].mq1} → getMq1() → sensorValues.get("mq1")
- ✅ MQ2: ${sensorData[0].mq2} → getMq2() → sensorValues.get("mq2")
- ✅ MQ3: ${sensorData[0].mq3} → getMq3() → sensorValues.get("mq3")
- ✅ Dust: ${sensorData[0].dust} → getDust() → sensorValues.get("dust")
- ⚠️ WiFi Signal: ${sensorData[0].wifiSignal} → getWifiSignal() → sensorValues.get("wifi_signal") - **KHÔNG CÓ TRONG ESP32 PAYLOAD**
- ⚠️ Uptime: ${sensorData[0].uptime} → getUptime() → sensorValues.get("uptime") - **KHÔNG CÓ TRONG ESP32 PAYLOAD**

### 4. **JSON Response Format**
Khi gọi `/dashboard?format=json`, response phải có format:
```json
{
  "success": true,
  "deviceId": 2,
  "data": [
    {
      "deviceId": 2,
      "timestamp": "2025-11-15 21:30:00",
      "temperature": 25.0,
      "humidity": 60.0,
      "mq1": 3963.0,
      "mq2": 4077.0,
      "mq3": 4066.0,
      "dust": 4800.0
    }
  ],
  "count": 1
}
```

## 🔍 CÁCH KIỂM TRA

### 1. **Kiểm tra Logs**
Xem console logs khi load dashboard:
```
[DashboardServlet] Loading data for device: 2
[SensorDataDAO] Found X sensor data records for deviceId: 2
[SensorDataDAO] Loaded sensor info: typeId=1, name='DHT11 - Nhiệt độ', value=25.0
[DashboardServlet] Converting X raw sensor records to dashboard data
[DashboardServlet] Grouped into Y timestamp groups
[DashboardServlet] Processing sensor: typeId=1, name='DHT11 - Nhiệt độ', value=25.0
[DashboardServlet] Mapped 'DHT11 - Nhiệt độ' -> 'temperature'
[DashboardServlet] DashboardData created with 6 sensor values: {temperature=25.0, humidity=60.0, ...}
```

### 2. **Kiểm tra Browser Console**
Mở Developer Tools → Console, xem logs:
```
[Dashboard] Fetching data...
[Dashboard] Fetching from: /IoTWebApp/dashboard?deviceId=DEVICE001&format=json&_t=...
[Dashboard] Response status: 200
[Dashboard] Received data: {success: true, data: [...], count: X}
[Dashboard] Updating UI with X records
```

### 3. **Kiểm tra Network Tab**
- Request: `GET /IoTWebApp/dashboard?deviceId=DEVICE001&format=json`
- Response: JSON với success=true và data array không rỗng

### 4. **Kiểm tra Database**
Chạy SQL query để xác minh dữ liệu:
```sql
SELECT TOP 10 
    sd.DataID,
    sd.DeviceID,
    sd.SensorTypeID,
    st.SensorName,
    sd.Value,
    sd.CreatedAt
FROM SensorData sd
INNER JOIN SensorType st ON sd.SensorTypeID = st.SensorTypeID
WHERE sd.DeviceID = 2
ORDER BY sd.CreatedAt DESC
```

## 🐛 CÁC VẤN ĐỀ CÓ THỂ XẢY RA

### 1. **Không có dữ liệu hiển thị**
- ❌ Kiểm tra: Có dữ liệu trong database không?
- ❌ Kiểm tra: deviceId có đúng không? (ESP32 gửi "DEVICE001" nhưng database có DeviceID=2)
- ❌ Kiểm tra: Logs có báo lỗi không?

### 2. **Dữ liệu hiển thị sai**
- ❌ Kiểm tra: Mapping sensor name có đúng không?
- ❌ Kiểm tra: Grouping theo timestamp có đúng không?
- ❌ Kiểm tra: SensorType có được load đúng không?

### 3. **Charts không update**
- ❌ Kiểm tra: AJAX request có thành công không?
- ❌ Kiểm tra: Chart.js có load không?
- ❌ Kiểm tra: JavaScript có lỗi không?

## ✅ KẾT LUẬN

Nếu tất cả các điểm trên đều ✅, dashboard sẽ hiển thị dữ liệu đúng cách.

**Lưu ý**: WiFi Signal và Uptime sẽ hiển thị 0 vì ESP32 không gửi các giá trị này trong payload hiện tại.

