# 🚀 Hướng dẫn kết nối ESP32 với Web Application

## 📋 Tổng quan

ESP32 sẽ đọc dữ liệu từ các cảm biến và gửi lên server Java qua HTTP POST. Server nhận dữ liệu JSON, lưu vào database SQL Server, và hiển thị trên dashboard.

---

## 🔧 Bước 1: Chuẩn bị phần cứng ESP32

### Linh kiện cần thiết:
- ✅ ESP32 Development Board
- ✅ DHT22 - Cảm biến nhiệt độ & độ ẩm
- ✅ MQ-135 - Cảm biến chất lượng không khí
- ✅ MQ-7 - Cảm biến CO (Carbon Monoxide)
- ✅ MQ-2 - Cảm biến khói và gas
- ✅ GP2Y1010AU0F - Cảm biến bụi PM2.5
- ✅ Breadboard và dây nối

### Sơ đồ kết nối:

```
DHT22:
  VCC  → 3.3V
  GND  → GND
  DATA → GPIO 4

MQ-135 (Air Quality):
  VCC  → 5V
  GND  → GND
  AOUT → GPIO 34

MQ-7 (Carbon Monoxide):
  VCC  → 5V
  GND  → GND
  AOUT → GPIO 35

MQ-2 (Smoke/Gas):
  VCC  → 5V
  GND  → GND
  AOUT → GPIO 32

Dust Sensor:
  VCC  → 5V
  GND  → GND
  VOUT → GPIO 33
```

---

## 💻 Bước 2: Cài đặt Arduino IDE và thư viện

### 2.1. Cài đặt Arduino IDE:
1. Download từ: https://www.arduino.cc/en/software
2. Cài đặt và mở Arduino IDE

### 2.2. Thêm ESP32 Board:
1. File → Preferences
2. Thêm URL vào "Additional Board Manager URLs":
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
3. Tools → Board → Boards Manager
4. Tìm "esp32" và click Install

### 2.3. Cài đặt thư viện cần thiết:
1. Tools → Manage Libraries
2. Tìm và cài đặt các thư viện sau:
   - **DHT sensor library** by Adafruit
   - **Adafruit Unified Sensor** by Adafruit
   - **ArduinoJson** by Benoit Blanchon (v6.x)

---

## 🌐 Bước 3: Cấu hình mạng

### 3.1. Kiểm tra IP máy tính:

**Windows:**
```powershell
ipconfig
```
Tìm dòng "IPv4 Address" của WiFi adapter
Ví dụ: `192.168.1.10`

**Linux/Mac:**
```bash
ifconfig
```

### 3.2. Cập nhật code ESP32:

Mở file `ESP32_SAMPLE_CODE.ino` và thay đổi:

```cpp
// WiFi của bạn
const char* ssid = "TenWiFi";           // ← Thay đổi
const char* password = "MatKhauWiFi";   // ← Thay đổi

// IP máy tính chạy Tomcat
const char* serverUrl = "http://192.168.1.10:9999/IoTWebApp/api/data";  // ← Thay đổi IP
```

---

## 📡 Bước 4: Cài đặt Java Server

### 4.1. Thêm dependency JSON vào pom.xml:
```xml
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20240303</version>
</dependency>
```

### 4.2. Clean and Build project:
1. NetBeans → Right-click project
2. Clean and Build
3. Đợi Maven download dependencies

### 4.3. Deploy lên Tomcat:
1. Run project trong NetBeans
2. Kiểm tra Tomcat chạy trên port 9999

### 4.4. Test API endpoint:
Mở browser và truy cập:
```
http://localhost:9999/IoTWebApp/api/data
```
Sẽ thấy response JSON:
```json
{
  "status": "online",
  "endpoint": "/api/data",
  "method": "POST",
  ...
}
```

---

## 🔥 Bước 5: Upload code lên ESP32

### 5.1. Kết nối ESP32:
1. Kết nối ESP32 với máy tính qua USB
2. Arduino IDE → Tools → Board → ESP32 Dev Module
3. Tools → Port → Chọn COM port của ESP32 (Windows: COMx)

### 5.2. Upload code:
1. Mở file `ESP32_SAMPLE_CODE.ino`
2. Click nút Upload (→)
3. Đợi upload hoàn tất

### 5.3. Mở Serial Monitor:
1. Tools → Serial Monitor
2. Set baud rate: **115200**
3. Quan sát output:

```
=================================
ESP32 IoT Sensor System
=================================
Connecting to WiFi: YourWiFi
........
✓ WiFi Connected!
IP Address: 192.168.1.100
Signal Strength: -45 dBm

----- Reading Sensors -----
Temperature: 25.50 °C
Humidity: 60.20 %
MQ-135 (Air Quality): 248.30 ppm
MQ-7 (CO): 275.80 ppm
MQ-2 (Smoke): 545.20 ppm
Dust (PM2.5): 35.10 µg/m³

----- Sending to Server -----
URL: http://192.168.1.10:9999/IoTWebApp/api/data
JSON: {"deviceId":"DEVICE001","temperature":25.5,...}
✓ HTTP Response Code: 200
Server Response: {"status":"success","message":"Data saved","deviceId":"DEVICE001","recordsSaved":6}
---------------------------
```

---

## ✅ Bước 6: Kiểm tra dữ liệu trên Dashboard

### 6.1. Truy cập Dashboard:
```
http://localhost:9999/IoTWebApp/dashboard
```

### 6.2. Đăng nhập:
- Username: `admin`
- Password: `admin123`

### 6.3. Xem dữ liệu real-time:
- Dashboard sẽ tự động hiển thị dữ liệu từ ESP32
- Data được cập nhật mỗi 15 giây
- Xem biểu đồ và bảng sensor readings

---

## 🐛 Troubleshooting

### ❌ ESP32 không kết nối được WiFi:
```
✗ WiFi Connection Failed!
```
**Giải pháp:**
- Kiểm tra tên WiFi và mật khẩu
- Đảm bảo WiFi là 2.4GHz (ESP32 không hỗ trợ 5GHz)
- Thử reset ESP32

### ❌ HTTP Error Code: -1
```
✗ HTTP Error Code: -1
```
**Giải pháp:**
- Kiểm tra IP máy tính: `ipconfig`
- Đảm bảo Tomcat đang chạy: `http://localhost:9999`
- Tắt Windows Firewall hoặc allow port 9999:
  ```powershell
  netsh advfirewall firewall add rule name="Tomcat" dir=in action=allow protocol=TCP localport=9999
  ```
- Ping từ ESP32 IP đến máy tính:
  ```powershell
  ping 192.168.1.100
  ```

### ❌ Server trả về 404:
```
HTTP Response Code: 404
```
**Giải pháp:**
- Kiểm tra URL có đúng: `/IoTWebApp/api/data`
- Kiểm tra context path trong Tomcat
- Clean and Build lại project

### ❌ Sensor readings = 0 hoặc NaN:
**Giải pháp:**
- Kiểm tra lại kết nối dây cảm biến
- Kiểm tra nguồn cấp (3.3V cho DHT22, 5V cho MQ sensors)
- DHT22 cần resistor pull-up 10kΩ giữa DATA và VCC

### ❌ Dashboard không hiển thị data:
**Giải pháo:**
- Kiểm tra SQL Server: `SELECT TOP 10 * FROM SensorData ORDER BY id DESC`
- Kiểm tra Tomcat logs: `catalina.out`
- F12 browser → Network tab → Xem có lỗi API không

---

## 📊 JSON Format từ ESP32

ESP32 gửi POST request với JSON payload:

```json
{
  "deviceId": "DEVICE001",
  "temperature": 25.5,
  "humidity": 60.2,
  "mq135": 248.3,
  "mq7": 275.8,
  "mq2": 545.2,
  "dust": 35.1
}
```

Server response:
```json
{
  "status": "success",
  "message": "Data saved",
  "deviceId": "DEVICE001",
  "recordsSaved": 6
}
```

---

## 🔒 Security Notes

⚠️ **Quan trọng:**
- API endpoint `/api/data` hiện không yêu cầu authentication
- Trong production, nên thêm API key hoặc token
- Nên sử dụng HTTPS thay vì HTTP
- Validate và sanitize input data

---

## 📈 Performance Tips

- **Giảm tần suất gửi:** Thay đổi `sendInterval` từ 15000ms lên 30000ms (30 giây)
- **Sleep mode:** Sử dụng deep sleep giữa các lần đọc để tiết kiệm pin
- **Batch sending:** Gửi nhiều readings trong 1 request

---

## 🎯 Next Steps

1. ✅ Thêm nhiều ESP32 devices (DEVICE002, DEVICE003)
2. ✅ Thêm alert notifications khi vượt ngưỡng
3. ✅ Tạo mobile app để xem data
4. ✅ Export data ra CSV/Excel
5. ✅ Thêm machine learning predictions

---

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Serial Monitor output từ ESP32
2. Tomcat logs: `catalina.out`
3. SQL Server logs
4. Browser console (F12)

**Happy Coding! 🚀**
