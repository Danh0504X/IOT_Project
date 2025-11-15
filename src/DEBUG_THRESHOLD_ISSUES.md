# Hướng dẫn Debug - Vấn đề Cài đặt Ngưỡng không hoạt động

## 🔍 Các vấn đề có thể xảy ra:

### 1. ESP32 chưa upload code mới
- **Triệu chứng**: ESP32 không gọi `/api/poll` để lấy threshold settings
- **Kiểm tra**: Mở Serial Monitor, tìm dòng "Fetching thresholds from server"
- **Giải pháp**: Upload file `ESP32_WITH_THRESHOLDS.ino` lên ESP32

### 2. ESP32 không kết nối được đến `/api/poll`
- **Triệu chứng**: Serial Monitor hiển thị "HTTP Error: -1" hoặc "HTTP Request failed"
- **Kiểm tra**: 
  - Kiểm tra server có chạy không: `http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001`
  - Kiểm tra WiFi connection của ESP32
  - Kiểm tra IP address trong code ESP32 có đúng không
- **Giải pháp**: 
  - Kiểm tra server URL trong code ESP32
  - Kiểm tra WiFi credentials
  - Đảm bảo server đang chạy và accessible từ ESP32

### 3. Threshold settings không được lưu vào database
- **Triệu chứng**: ESP32 gọi `/api/poll` nhưng nhận được `settings: {}` (empty)
- **Kiểm tra**: 
  - Truy cập trang cài đặt ngưỡng: `http://192.168.137.1:8080/IoTWebApp/admin/thresholds?deviceId=DEVICE001`
  - Kiểm tra xem có lưu thành công không (xem message "Thresholds updated successfully!")
  - Kiểm tra database có dữ liệu trong bảng `SensorSettings` không
- **Giải pháp**: 
  - Đảm bảo đã cài đặt và lưu threshold settings trên web
  - Kiểm tra server logs xem có lỗi khi lưu threshold không

### 4. Threshold settings được lưu nhưng không trả về đúng format
- **Triệu chứng**: ESP32 nhận được response nhưng không parse được JSON
- **Kiểm tra**: 
  - Serial Monitor hiển thị "Server response: ..." - copy JSON này và kiểm tra format
  - Kiểm tra xem có keys: `mq1`, `mq2`, `mq3`, `temperature`, `dust` không
- **Giải pháp**: 
  - Kiểm tra `PollServlet` trả về đúng format
  - Kiểm tra `ThresholdService.getThresholdMapForESP32()` mapping đúng tên

### 5. ESP32 không so sánh đúng giá trị
- **Triệu chứng**: ESP32 đã load threshold nhưng không báo khi vượt ngưỡng
- **Kiểm tra**: 
  - Serial Monitor hiển thị "Checking Thresholds" với giá trị so sánh
  - Kiểm tra logic so sánh trong `checkThresholds()`
- **Giải pháp**: 
  - Xem Serial Monitor output để debug logic so sánh
  - Kiểm tra xem FAN_PIN và BUZZER_PIN có được kích hoạt đúng không

## 📋 Checklist để kiểm tra:

### Bước 1: Kiểm tra Backend
1. ✅ Server đang chạy trên port 8080
2. ✅ Endpoint `/api/poll?deviceId=DEVICE001` trả về JSON đúng format
3. ✅ Threshold settings đã được lưu vào database (kiểm tra bảng `SensorSettings`)
4. ✅ `ThresholdService.getThresholdMapForESP32()` trả về đúng keys: `mq1`, `mq2`, `mq3`, `temperature`, `dust`

### Bước 2: Kiểm tra ESP32
1. ✅ ESP32 đã upload code mới (`ESP32_WITH_THRESHOLDS.ino`)
2. ✅ ESP32 kết nối WiFi thành công
3. ✅ ESP32 gọi `/api/poll` mỗi 10 giây (xem Serial Monitor)
4. ✅ ESP32 parse JSON response thành công (xem "✓ Thresholds loaded successfully")
5. ✅ ESP32 so sánh giá trị cảm biến với threshold (xem "Checking Thresholds")
6. ✅ ESP32 kích hoạt FAN/BUZZER khi vượt ngưỡng

### Bước 3: Kiểm tra Hardware
1. ✅ FAN_PIN (18) và BUZZER_PIN (19) đã kết nối đúng
2. ✅ FAN và BUZZER hoạt động khi test manual (digitalWrite LOW)
3. ✅ Logic active LOW/HIGH đúng với hardware

## 🛠️ Cách test thủ công:

### Test Backend Endpoint:
```bash
# Test endpoint trả về threshold settings
curl "http://192.168.137.1:8080/IoTWebApp/api/poll?deviceId=DEVICE001"

# Kết quả mong đợi:
# {
#   "update": true,
#   "settings": {
#     "temperature": 25.0,
#     "mq1": 300.0,
#     "mq2": 1500.0,
#     "mq3": 400.0,
#     "dust": 50.0
#   },
#   "commands": []
# }
```

### Test ESP32:
1. Upload code mới lên ESP32
2. Mở Serial Monitor (115200 baud)
3. Tìm các dòng:
   - "Fetching thresholds from server:"
   - "✓ Thresholds loaded successfully:"
   - "--- Checking Thresholds ---"
   - "🚨 ALERT: ..." (nếu vượt ngưỡng)

## 🐛 Debug với Serial Monitor:

### Log messages để tìm:
1. **WiFi connection**: "✓ WiFi connected" hoặc "✗ WiFi connect failed"
2. **Fetching thresholds**: "Fetching thresholds from server:" mỗi 10 giây
3. **HTTP response**: "HTTP Response Code: 200" (thành công) hoặc error code
4. **JSON parsing**: "✓ JSON parsed successfully" hoặc "✗ JSON parse error"
5. **Threshold loading**: "✓ Thresholds loaded successfully:" với các giá trị
6. **Threshold checking**: "--- Checking Thresholds ---" với so sánh chi tiết
7. **Alert activation**: "🚨 ALERT: ..." và "🔴 ACTIVATING FAN and BUZZER..."

## ✅ Giải pháp nhanh:

1. **Đảm bảo ESP32 đã upload code mới** (file `ESP32_WITH_THRESHOLDS.ino`)
2. **Kiểm tra Serial Monitor** để xem ESP32 có gọi `/api/poll` không
3. **Kiểm tra web cài đặt ngưỡng** để đảm bảo đã lưu threshold settings
4. **Kiểm tra server logs** để xem có lỗi gì không
5. **Test endpoint trực tiếp** bằng browser hoặc curl

## 📝 Ghi chú quan trọng:

- **Mapping sensor names**:
  - ESP32 gửi: `mq135` (MQ1_PIN), `mq7` (MQ2_PIN), `mq2` (MQ3_PIN)
  - Backend trả về: `mq1`, `mq2`, `mq3` (cho threshold settings)
  - ESP32 đọc: `mq1` (cho MQ1_PIN), `mq2` (cho MQ2_PIN), `mq3` (cho MQ3_PIN)

- **Logic kích hoạt**: 
  - FAN và BUZZER là active LOW (digitalWrite LOW = ON)
  - Cảnh báo khi giá trị cảm biến > threshold

- **Default thresholds**: 
  - Nếu ESP32 không load được từ server, sẽ dùng giá trị mặc định
  - Code sẽ hiển thị warning "⚠ WARNING: Thresholds not loaded from server yet!"



