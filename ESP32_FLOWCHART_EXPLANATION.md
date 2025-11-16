# 📋 Giải Thích Chi Tiết Flowchart ESP32 - Map Với Code

## 🔍 Tổng Quan
Flowchart này mô tả luồng hoạt động của ESP32 IoT System, từ khi khởi động đến vòng lặp chính với 3 tác vụ song song.

---

## 🚀 PHẦN 1: SETUP (Khởi Tạo Hệ Thống)

### **A: START - ESP32 Power On**
- **Mô tả**: Điểm bắt đầu khi ESP32 được cấp nguồn
- **Code tương ứng**: Tự động chạy khi ESP32 khởi động

### **B: setup() - Initialize System**
- **Mô tả**: Hàm setup() được gọi tự động khi ESP32 khởi động
- **Code tương ứng**: `void setup() {` (dòng 696)
- **Chức năng**: Khởi tạo toàn bộ hệ thống

---

### **B1: Serial.begin(115200) - delay(1000)**
- **Mô tả**: Khởi tạo Serial Monitor ở tốc độ 115200 baud, delay 1 giây
- **Code tương ứng**: 
  - Dòng 697: `Serial.begin(115200);`
  - Dòng 698: `delay(1000);`
- **Mục đích**: Thiết lập giao tiếp serial để debug, delay để ổn định

### **B2: Configure GPIO Pins - pinMode for all pins**
- **Mô tả**: Cấu hình các chân GPIO làm INPUT hoặc OUTPUT
- **Code tương ứng**: Dòng 701-707
  ```cpp
  pinMode(DUST_LED_PIN, OUTPUT);    // Pin 12
  pinMode(DUST_VOUT_PIN, INPUT);    // Pin 35
  pinMode(MQ1_PIN, INPUT);          // Pin 32 (MQ-135)
  pinMode(MQ2_PIN, INPUT);          // Pin 33 (MQ-7)
  pinMode(MQ3_PIN, INPUT);          // Pin 34 (MQ-2)
  pinMode(FAN_PIN, OUTPUT);         // Pin 18
  pinMode(BUZZER_PIN, OUTPUT);      // Pin 19
  ```

### **B3: Set FAN & BUZZER OFF - digitalWrite HIGH**
- **Mô tả**: Tắt FAN và BUZZER bằng cách set HIGH (LOW = ON, HIGH = OFF trong logic này)
- **Code tương ứng**: Dòng 709-710
  ```cpp
  digitalWrite(FAN_PIN, HIGH);      // FAN OFF
  digitalWrite(BUZZER_PIN, HIGH);   // BUZZER OFF
  ```

### **C: initDefaultThresholds() - Load Default Thresholds**
- **Mô tả**: Nạp các ngưỡng mặc định cho tất cả sensors
- **Code tương ứng**: 
  - Dòng 715: `initDefaultThresholds();`
  - Hàm định nghĩa: Dòng 78-125
- **Chức năng**: 
  - Khởi tạo ngưỡng cho Temperature (4 levels), Humidity (3 levels), MQ135 (4 levels), MQ2 (4 levels), CO (4 levels), PM2.5 (5 levels)
  - Set `thresholds.loaded = true` (dòng 123)

---

## 📡 PHẦN 2: KẾT NỐI WIFI & FETCH THRESHOLDS

### **D: connectWiFi() - Attempt WiFi Connection**
- **Mô tả**: Kết nối ESP32 với WiFi
- **Code tương ứng**: 
  - Dòng 717: `connectWiFi();`
  - Hàm định nghĩa: Dòng 163-185
- **Chi tiết trong hàm**:
  - Dòng 167: `WiFi.mode(WIFI_STA);` - Chế độ Station
  - Dòng 168: `WiFi.begin(WIFI_SSID, WIFI_PASSWORD);` - Bắt đầu kết nối
  - Dòng 171-175: Vòng lặp chờ kết nối, tối đa 30 lần (15 giây)

### **E: WiFi Connected?** (Decision Diamond)
- **Mô tả**: Kiểm tra xem WiFi đã kết nối chưa
- **Code tương ứng**: Trong hàm `connectWiFi()`, dòng 178
  ```cpp
  if (WiFi.status() == WL_CONNECTED) {
    // Connected
  } else {
    // Failed
  }
  ```
- **Điều kiện**: 
  - **Yes**: `WiFi.status() == WL_CONNECTED` → Tiếp tục
  - **No**: `WiFi.status() != WL_CONNECTED` → Quay lại D (thử kết nối lại)

### **G: WiFi Still Connected?** (Decision Diamond)
- **Mô tả**: Kiểm tra lại WiFi sau khi kết nối
- **Code tương ứng**: Dòng 720 (trong setup)
  ```cpp
  if (WiFi.status() == WL_CONNECTED) {
    fetchThresholds();
  }
  ```
- **Điều kiện**:
  - **Yes**: WiFi vẫn kết nối → Đi đến H (fetch thresholds)
  - **No**: WiFi mất kết nối → Đi đến I (vào main loop ngay)

### **H: fetchThresholds() - First Poll from Server**
- **Mô tả**: Lấy thresholds từ server lần đầu tiên
- **Code tương ứng**: Dòng 721: `fetchThresholds();`
- **Hàm định nghĩa**: Dòng 190-459
- **Chi tiết**:
  - Dòng 193-196: Kiểm tra WiFi, nếu không kết nối thì return
  - Dòng 198: Tạo URL: `pollUrl + "?deviceId=" + deviceId`
  - Dòng 199-200: Thiết lập HTTP request với timeout 5s
  - Dòng 202: `http.GET()` - Gửi request GET
  - Dòng 205-457: Parse JSON response và cập nhật thresholds

### **I: ENTER MAIN LOOP**
- **Mô tả**: Điểm vào vòng lặp chính
- **Code tương ứng**: Sau khi setup() hoàn thành, ESP32 tự động vào `loop()`

---

## 🔄 PHẦN 3: MAIN LOOP (Vòng Lặp Chính)

### **J: loop() - Get currentMillis = millis()**
- **Mô tả**: Điểm bắt đầu mỗi lần lặp, lấy thời gian hiện tại
- **Code tương ứng**: Dòng 728-729
  ```cpp
  void loop() {
    unsigned long currentMillis = millis();
  ```
- **Chức năng**: Là điểm trung tâm cho 3 nhánh song song

---

## 📊 NHÁNH 1: 5-SECOND SEND INTERVAL (Gửi Dữ Liệu)

### **K: 5 seconds elapsed? SEND_INTERVAL** (Decision Diamond)
- **Mô tả**: Kiểm tra xem đã đủ 5 giây chưa để gửi dữ liệu
- **Code tương ứng**: Dòng 731
  ```cpp
  if (currentMillis - lastSend >= SEND_INTERVAL) {
    // SEND_INTERVAL = 5000 (5 giây) - định nghĩa ở dòng 38
  ```
- **Điều kiện**:
  - **Yes**: `currentMillis - lastSend >= 5000` → Đi đến N (readAndSend)
  - **No**: `currentMillis - lastSend < 5000` → Đi đến R (delay 200ms)

### **N: readAndSend()**
- **Mô tả**: Đọc tất cả sensors, kiểm tra thresholds, và gửi dữ liệu
- **Code tương ứng**: Dòng 733: `readAndSend();`
- **Hàm định nghĩa**: Dòng 641-691

### **N2: checkThresholds() - Find Alert Levels & AQI**
- **Mô tả**: Kiểm tra ngưỡng, tìm alert level cho mỗi sensor, tính AQI
- **Code tương ứng**: Dòng 675 (trong readAndSend)
  ```cpp
  checkThresholds(temperature, humidity, mq135, mq7, mq2, pm25);
  ```
- **Hàm định nghĩa**: Dòng 464-600
- **Chi tiết**:
  - Dòng 469-472: Kiểm tra thresholds.loaded, nếu chưa thì gọi initDefaultThresholds()
  - Dòng 480-485: Tìm alert level cho mỗi sensor bằng findAlertLevel()
  - Dòng 505: Tính AQI: `calculateAQI(pm25, mq7, mq135)`
  - Dòng 511-530: Logic điều khiển FAN
  - Dòng 535-572: Logic điều khiển BUZZER
  - Dòng 581-597: Thực thi điều khiển FAN và BUZZER (digitalWrite)

### **N3: Control FAN & BUZZER - digitalWrite GPIO pins**
- **Mô tả**: Điều khiển FAN và BUZZER dựa trên kết quả checkThresholds
- **Code tương ứng**: Dòng 581-597 (trong checkThresholds)
  ```cpp
  if (fanOn) {
    digitalWrite(FAN_PIN, LOW);   // FAN ON
  } else {
    digitalWrite(FAN_PIN, HIGH);  // FAN OFF
  }
  
  if (buzzerOn) {
    digitalWrite(BUZZER_PIN, LOW);   // BUZZER ON
  } else {
    digitalWrite(BUZZER_PIN, HIGH);  // BUZZER OFF
  }
  ```

### **N4: Create JSON Payload - deviceId, temperature, etc.**
- **Mô tả**: Tạo JSON payload chứa dữ liệu sensors
- **Code tương ứng**: Dòng 678-688 (trong readAndSend)
  ```cpp
  StaticJsonDocument<256> doc;
  doc["deviceId"]    = deviceId;
  doc["temperature"] = temperature;
  doc["humidity"]    = humidity;
  doc["mq135"]       = mq135;
  doc["mq7"]         = mq7;
  doc["mq2"]         = mq2;
  doc["dust"]        = pm25;
  
  String jsonStr;
  serializeJson(doc, jsonStr);
  ```

### **N5: sendToServer() - POST /api/data**
- **Mô tả**: Gửi dữ liệu lên server qua HTTP POST
- **Code tương ứng**: Dòng 690: `sendToServer(jsonStr);`
- **Hàm định nghĩa**: Dòng 605-636
- **Chi tiết**:
  - Dòng 606-609: Kiểm tra WiFi, nếu không kết nối thì return
  - Dòng 611: `http.begin(client, serverUrl);` - serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data"
  - Dòng 612: Set header Content-Type: application/json
  - Dòng 617: `http.POST(payload);` - Gửi POST request

### **O: WiFi Connected?** (Decision Diamond - trong sendToServer)
- **Mô tả**: Kiểm tra WiFi trước khi gửi
- **Code tương ứng**: Dòng 606 (trong sendToServer)
  ```cpp
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("✗ WiFi not connected, skip sending");
    return;
  }
  ```
- **Điều kiện**:
  - **Yes**: `WiFi.status() == WL_CONNECTED` → Đi đến P (HTTP POST Success)
  - **No**: `WiFi.status() != WL_CONNECTED` → Đi đến Q (Skip Send)

### **P: HTTP POST Success**
- **Mô tả**: Gửi thành công
- **Code tương ứng**: Dòng 622 (trong sendToServer)
  ```cpp
  if (code == HTTP_CODE_OK) {
    Serial.println("Server reply: " + http.getString());
  }
  ```

### **Q: Skip Send**
- **Mô tả**: Bỏ qua việc gửi do WiFi không kết nối
- **Code tương ứng**: Dòng 608 (trong sendToServer) - return early

---

## 🔄 NHÁNH 2: 10-SECOND POLL INTERVAL (Lấy Thresholds)

### **L: 10 seconds elapsed? POLL_INTERVAL** (Decision Diamond)
- **Mô tả**: Kiểm tra xem đã đủ 10 giây chưa để poll thresholds
- **Code tương ứng**: Dòng 736
  ```cpp
  if (currentMillis - lastPoll >= POLL_INTERVAL) {
    // POLL_INTERVAL = 10000 (10 giây) - định nghĩa ở dòng 39
  ```
- **Điều kiện**:
  - **Yes**: `currentMillis - lastPoll >= 10000` → Kiểm tra tiếp WiFi
  - **No**: `currentMillis - lastPoll < 10000` → Đi đến R (delay 200ms)

### **L1: Update lastPoll = currentMillis**
- **Mô tả**: Cập nhật thời điểm poll cuối cùng
- **Code tương ứng**: Dòng 737
  ```cpp
  lastPoll = currentMillis;
  ```

### **S: fetchThresholds() - GET /api/poll?deviceId=DEVICE001**
- **Mô tả**: Lấy thresholds mới từ server
- **Code tương ứng**: Dòng 739 (trong loop)
  ```cpp
  if (WiFi.status() == WL_CONNECTED) {
    fetchThresholds();
  }
  ```
- **Điều kiện WiFi**: Chỉ gọi fetchThresholds() nếu WiFi đã kết nối (dòng 738)

### **S1: Parse JSON Response - Multi-level thresholds**
- **Mô tả**: Parse JSON response từ server
- **Code tương ứng**: Dòng 205-357 (trong fetchThresholds)
- **Chi tiết**:
  - Dòng 206: `String response = http.getString();`
  - Dòng 210: `DynamicJsonDocument doc(4096);`
  - Dòng 211: `deserializeJson(doc, response);`
  - Dòng 213: Kiểm tra `doc["update"] == true`
  - Dòng 217-351: Parse multi-level thresholds cho từng sensor

### **S2: Has thresholds?** (Decision Diamond)
- **Mô tả**: Kiểm tra xem response có chứa thresholds không
- **Code tương ứng**: Dòng 217 (trong fetchThresholds)
  ```cpp
  if (doc.containsKey("thresholds")) {
    // Có thresholds → Parse
  } else if (doc.containsKey("settings")) {
    // Có settings → Update
  } else {
    // Không có gì → Keep defaults
  }
  ```
- **Điều kiện**:
  - **Yes**: Có thresholds hoặc settings → Đi đến S3 (Update Local Threshold Structure)
  - **No**: Không có thresholds → Đi đến S4 (Keep Default Thresholds)

### **S3: Update Local Threshold Structure**
- **Mô tả**: Cập nhật thresholds từ server vào biến local
- **Code tương ứng**: Dòng 353-357 (trong fetchThresholds)
  ```cpp
  if (thresholdsParsed) {
    thresholds.loaded = true;
    thresholdsFetchedOnce = true;
  }
  ```

### **S4: Keep Default Thresholds**
- **Mô tả**: Giữ nguyên thresholds mặc định nếu server không trả về
- **Code tương ứng**: Dòng 444-446 (trong fetchThresholds)
  ```cpp
  if (!thresholdsParsed && !doc.containsKey("settings")) {
    Serial.println("  ⚠ No thresholds or settings in response, using defaults");
  }
  ```

---

## 🔌 NHÁNH 3: WIFI RECONNECTION (Kết Nối Lại WiFi)

### **M: WiFi Connected?** (Decision Diamond)
- **Mô tả**: Kiểm tra WiFi trong mỗi lần lặp
- **Code tương ứng**: Dòng 743
  ```cpp
  if (WiFi.status() != WL_CONNECTED) {
    // WiFi mất kết nối → Reconnect
  }
  ```
- **Điều kiện**:
  - **Yes**: `WiFi.status() == WL_CONNECTED` → Đi đến R (delay 200ms)
  - **No**: `WiFi.status() != WL_CONNECTED` → Đi đến M1 (Reconnect WiFi)

### **M1: Reconnect WiFi - connectWiFi()**
- **Mô tả**: Kết nối lại WiFi
- **Code tương ứng**: Dòng 745
  ```cpp
  Serial.println("WiFi disconnected, reconnecting...");
  connectWiFi();
  ```
- **Hàm connectWiFi()**: Dòng 163-185

### **M3: WiFi Connected?** (Decision Diamond - sau reconnect)
- **Mô tả**: Kiểm tra WiFi sau khi reconnect
- **Code tương ứng**: Dòng 747
  ```cpp
  if (WiFi.status() == WL_CONNECTED && !thresholds.loaded) {
    fetchThresholds();
  }
  ```
- **Điều kiện**:
  - **Yes & !thresholds.loaded**: WiFi kết nối VÀ thresholds chưa load → Đi đến M4 (fetchThresholds)
  - **No**: WiFi chưa kết nối → Đi đến R (delay 200ms)

### **M4: fetchThresholds() - First time after reconnect**
- **Mô tả**: Lấy thresholds sau khi reconnect thành công
- **Code tương ứng**: Dòng 748
  ```cpp
  if (WiFi.status() == WL_CONNECTED && !thresholds.loaded) {
    fetchThresholds();
  }
  ```

---

## ⏱️ PHẦN 4: LOOP CONTROL

### **R: delay(200ms)**
- **Mô tả**: Delay 200ms trước khi lặp lại
- **Code tương ứng**: Dòng 752 (trong loop)
  ```cpp
  delay(200);
  ```
- **Mục đích**: Tránh loop quá nhanh, tiết kiệm tài nguyên

### **Quay lại J: loop()**
- **Mô tả**: Quay lại đầu vòng lặp
- **Code tương ứng**: Sau delay(200), ESP32 tự động lặp lại hàm loop()

---

## 📊 TÓM TẮT LUỒNG ĐI CHÍNH

1. **SETUP**: A → B → B1 → B2 → B3 → C → D → E → G → H/I
2. **MAIN LOOP**: J → [K, L, M] (3 nhánh song song)
   - **Nhánh K (5s)**: K → N → N2 → N3 → N4 → N5 → O → P/Q → R
   - **Nhánh L (10s)**: L → L1 → S → S1 → S2 → S3/S4 → R
   - **Nhánh M (WiFi)**: M → M1 → M3 → M4/R → R
3. **LOOP**: R → J (lặp lại)

---

## 🎯 CÁC BIẾN QUAN TRỌNG

- `lastSend` (dòng 36): Thời điểm gửi dữ liệu cuối cùng
- `lastPoll` (dòng 37): Thời điểm poll thresholds cuối cùng
- `SEND_INTERVAL = 5000` (dòng 38): 5 giây
- `POLL_INTERVAL = 10000` (dòng 39): 10 giây
- `thresholds.loaded` (dòng 70): Trạng thái đã load thresholds chưa
- `thresholdsFetchedOnce` (dòng 40): Đã fetch thresholds ít nhất 1 lần chưa

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **3 nhánh song song**: K, L, M chạy độc lập, không chờ nhau
2. **Điều kiện WiFi**: Nhiều hàm kiểm tra WiFi trước khi thực thi
3. **Fallback**: Nếu không có thresholds từ server, dùng defaults
4. **Timing**: Dùng `millis()` để tránh blocking, không dùng `delay()` (trừ delay(200) ở cuối loop)

