# Debug Guide: Thresholds Not Loading

## Vấn đề hiện tại:
- ESP32 gọi `fetchThresholds()` (`thresholdsFetchedOnce = YES`)
- Nhưng `Thresholds loaded: NO`
- Có thể server trả về empty settings hoặc có lỗi parse

## Các bước kiểm tra:

### Bước 1: Kiểm tra Server Response
Mở trình duyệt hoặc dùng curl:
```
http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001
```

**Response mong đợi:**
```json
{
  "update": true,
  "settings": {
    "temperature": 30.0,
    "humidity": 70.0,
    "mq1": 300.0,
    "mq2": 300.0,
    "mq3": 600.0,
    "dust": 50.0
  },
  "commands": []
}
```

**Nếu settings empty:**
```json
{
  "update": true,
  "settings": {},
  "commands": []
}
```
→ **Giải pháp:** Cài đặt thresholds trên web dashboard

### Bước 2: Kiểm tra Server Logs
Khi ESP32 gọi `/api/poll`, bạn sẽ thấy trong server logs (Tomcat console):
```
[PollServlet] Received GET request for deviceId: DEVICE001
[PollServlet] Fetching thresholds for device: DEVICE001
[PollServlet] Retrieved X threshold settings
[PollServlet]   temperature = 30.0
[PollServlet]   mq1 = 300.0
...
[PollServlet] Sending response: {...}
```

**Nếu không có thresholds:**
```
[PollServlet] ⚠ No thresholds found for device: DEVICE001 (will return empty map)
```

### Bước 3: Kiểm tra Database
Chạy query SQL:
```sql
SELECT ss.*, st.name AS sensor_name
FROM SensorSettings ss
INNER JOIN SensorType st ON ss.sensor_type_id = st.sensor_type_id
WHERE ss.device_id = 'DEVICE001';
```

**Nếu không có dữ liệu:**
→ Cần cài đặt thresholds trên web dashboard

### Bước 4: Cài đặt Thresholds trên Web Dashboard
1. Vào dashboard: `http://192.168.137.1:8080/IoTWebApp/dashboard?deviceId=DEVICE001`
2. Click button "Cài đặt ngưỡng" (bên cạnh nút đăng xuất)
3. Hoặc truy cập trực tiếp: `http://192.168.137.1:8080/IoTWebApp/admin/thresholds?deviceId=DEVICE001`
4. Đặt các giá trị threshold:
   - **Temperature**: ví dụ `30.0`
   - **MQ1**: ví dụ `300.0`
   - **MQ2**: ví dụ `300.0`
   - **MQ3**: ví dụ `600.0`
   - **Dust**: ví dụ `50.0`
5. Click "Lưu"

### Bước 5: Kiểm tra Serial Monitor
Sau khi cài đặt thresholds, ESP32 sẽ tự động fetch lại sau 10 giây.

**Log mong đợi:**
```
========================================
Fetching thresholds from server:
  URL: http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001
  HTTP Response Code: 200
  Server response: {"update":true,"settings":{"temperature":30.0,...},...}
  ✓ JSON parsed successfully
  Update flag: true
  ✓ Settings object found
  Settings object size: 5 keys
  Settings keys: temperature=30.0 mq1=300.0 mq2=300.0 mq3=600.0 dust=50.0
  ✓ Temperature threshold: 30.0°C
  ✓ Humidity threshold: 70.0%
  ✓ MQ1 (MQ-135) threshold: 300.0
  ✓ MQ2 (MQ-7) threshold: 300.0
  ✓ MQ3 (MQ-2) threshold: 600.0
  ✓ Dust threshold: 50.0 µg/m³

  ✅ Thresholds loaded successfully:
     Temperature: 30.0°C
     Humidity: 70.0%
     MQ1: 300.0
     MQ2: 300.0
     MQ3: 600.0
     Dust: 50.0 µg/m³
========================================
```

## Các lỗi thường gặp:

### 1. Settings Empty trong Response
**Nguyên nhân:** Không có thresholds trong database  
**Giải pháp:** Cài đặt thresholds trên web dashboard (Bước 4)

### 2. HTTP Error 500
**Nguyên nhân:** Lỗi trong PollServlet  
**Kiểm tra:** Xem server logs để biết chi tiết lỗi

### 3. JSON Parse Error
**Nguyên nhân:** Response format không đúng  
**Kiểm tra:** Xem "Server response" trong Serial Monitor

### 4. Thresholds loaded nhưng không hoạt động
**Nguyên nhân:** Mapping giữa ESP32 sensor names và backend names không khớp  
**Kiểm tra:** Đảm bảo backend trả về "mq1", "mq2", "mq3", "temperature", "dust"

## Test nhanh:
1. Mở trình duyệt: `http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001`
2. Xem response JSON
3. Nếu `settings: {}` → Cài đặt thresholds trên web
4. Nếu có lỗi → Xem server logs


