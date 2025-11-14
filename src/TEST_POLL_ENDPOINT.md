# Test Poll Endpoint - Hướng dẫn Debug

## Vấn đề hiện tại:
- Database có thresholds (đã kiểm tra bằng SQL)
- Nhưng ESP32 không load được thresholds
- Có thể endpoint `/api/poll` trả về HTTP 500 hoặc empty settings

## Bước 1: Test Endpoint trong Trình duyệt

Mở trình duyệt và truy cập:
```
http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001
```

### Kết quả mong đợi:
```json
{
  "update": true,
  "settings": {
    "temperature": 25.0,
    "humidity": 70.0,
    "mq1": 400.0,
    "mq2": 1500.0,
    "mq3": 1000.0,
    "dust": 50.0
  },
  "commands": []
}
```

### Nếu thấy HTTP 500:
- Xem server logs (Tomcat console) để biết lỗi cụ thể
- Có thể là lỗi SQL, lỗi connection, hoặc exception

### Nếu thấy `settings: {}`:
- Kiểm tra server logs xem có log `[ThresholdService]` không
- Có thể mapping bị lỗi hoặc query không trả về dữ liệu

## Bước 2: Kiểm tra Server Logs

Khi ESP32 gọi `/api/poll` hoặc bạn test trong trình duyệt, bạn sẽ thấy trong server logs (Tomcat console):

```
[PollServlet] Received GET request for deviceId: DEVICE001
[PollServlet] Fetching thresholds for device: DEVICE001
[ThresholdService] Getting threshold map for ESP32, deviceId: DEVICE001
[ThresholdService] Retrieved 6 thresholds from database
[ThresholdService] Database thresholds:
  DB Name: 'Temperature' = 25.0
  DB Name: 'Humidity' = 70.0
  DB Name: 'Gas MQ1' = 400.0
  DB Name: 'Gas MQ2' = 1500.0
  DB Name: 'Gas MQ3' = 1000.0
  DB Name: 'Dust Density' = 50.0
[ThresholdService] Mapping: 'Temperature' -> 'temperature' = 25.0
[ThresholdService] Mapping: 'Humidity' -> 'humidity' = 70.0
[ThresholdService] Mapping: 'Gas MQ1' -> 'mq1' = 400.0
[ThresholdService] Mapping: 'Gas MQ2' -> 'mq2' = 1500.0
[ThresholdService] Mapping: 'Gas MQ3' -> 'mq3' = 1000.0
[ThresholdService] Mapping: 'Dust Density' -> 'dust' = 50.0
[ThresholdService] Final standard thresholds map size: 6
[ThresholdService] Standard thresholds:
  'temperature' = 25.0
  'humidity' = 70.0
  'mq1' = 400.0
  'mq2' = 1500.0
  'mq3' = 1000.0
  'dust' = 50.0
[PollServlet] Retrieved 6 threshold settings
[PollServlet]   temperature = 25.0
[PollServlet]   humidity = 70.0
[PollServlet]   mq1 = 400.0
[PollServlet]   mq2 = 1500.0
[PollServlet]   mq3 = 1000.0
[PollServlet]   dust = 50.0
[PollServlet] Sending response: {"update":true,"settings":{...},"commands":[]}
```

## Bước 3: Kiểm tra Database Query

Chạy query này trong SQL Server Management Studio:
```sql
SELECT st.name, ss.threshold_value 
FROM SensorSettings ss
INNER JOIN SensorType st ON ss.sensor_type_id = st.sensor_type_id
WHERE ss.device_id = 'DEVICE001';
```

Bạn sẽ thấy 6 rows với các giá trị thresholds.

## Bước 4: Restart Server

Sau khi thêm logging, restart Tomcat server để áp dụng code mới.

## Bước 5: Test lại

1. Test endpoint trong trình duyệt
2. Xem server logs để biết chi tiết
3. Nếu vẫn lỗi, kiểm tra exception trong logs

## Các lỗi thường gặp:

### 1. HTTP 500 - SQL Exception
**Nguyên nhân:** Lỗi kết nối database hoặc SQL syntax error
**Giải pháp:** Kiểm tra DBConnection configuration

### 2. HTTP 500 - NullPointerException
**Nguyên nhân:** settingsDAO hoặc sensorTypeDAO null
**Giải pháp:** Kiểm tra initialization

### 3. Empty settings trong response
**Nguyên nhân:** Query không trả về dữ liệu hoặc mapping lỗi
**Giải pháp:** Xem server logs để biết chi tiết

