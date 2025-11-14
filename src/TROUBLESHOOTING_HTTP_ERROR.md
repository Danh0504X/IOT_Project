# 🔧 TROUBLESHOOTING: HTTP Error -1

## ❌ Vấn đề
ESP32 trả về **HTTP Error: -1** khi gửi dữ liệu lên server.

## 🔍 HTTP Error -1 nghĩa là gì?
- **HTTPC_ERROR_CONNECTION_REFUSED** = -1
- ESP32 không thể kết nối đến server
- Server từ chối kết nối hoặc không tồn tại

## ✅ Code Arduino KHÔNG SAI
Code ESP32 của bạn đúng. Vấn đề là **kết nối mạng** hoặc **cấu hình server**.

---

## 🔍 CÁC NGUYÊN NHÂN CÓ THỂ

### 1. ❌ Server chưa chạy hoặc port sai
**Kiểm tra:**
- Server Tomcat có đang chạy không?
- Server đang chạy ở port nào? (8080 hay 9999?)
- Trong hình ảnh trước, bạn có URL `localhost:9999` → Server có thể ở port 9999, không phải 8080

**Giải pháp:**
- Kiểm tra server logs
- Thử truy cập từ browser: `http://192.168.137.1:8080/IoTWebApp/dashboard`
- Nếu không được, thử: `http://192.168.137.1:9999/IoTWebApp/dashboard`

### 2. ❌ IP Address sai
**ESP32 code hiện tại:**
```cpp
const char* serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data";
```

**Kiểm tra:**
- IP `192.168.137.1` có đúng không?
- Đây là IP của máy tính chạy server

**Cách tìm IP đúng:**
1. Trên máy tính chạy server:
   - Windows: `ipconfig` → Tìm "IPv4 Address"
   - Linux/Mac: `ifconfig` hoặc `ip addr`
2. Đảm bảo ESP32 và máy tính cùng mạng WiFi

### 3. ❌ ESP32 và Server không cùng mạng
**Kiểm tra:**
- ESP32 kết nối WiFi: "demo273"
- Máy tính chạy server có kết nối cùng WiFi "demo273" không?
- Nếu máy tính dùng Ethernet, cần đảm bảo cùng subnet

### 4. ❌ Firewall chặn
**Kiểm tra:**
- Windows Firewall có block port 8080/9999 không?
- Antivirus có block không?

**Giải pháp:**
- Tắt firewall tạm thời để test
- Hoặc thêm rule cho phép port 8080/9999

### 5. ❌ Context path sai
**ESP32 code:**
```cpp
http://192.168.137.1:8080/IoTWebApp/api/data
```

**Kiểm tra:**
- Context path có đúng là `/IoTWebApp` không?
- Kiểm tra file `context.xml`: `<Context path="/IoTWebApp"/>`

---

## ✅ CÁCH SỬA LỖI

### Bước 1: Xác định IP và Port của Server

**Trên máy tính chạy server:**

1. **Tìm IP address:**
   ```cmd
   ipconfig
   ```
   - Tìm dòng "IPv4 Address" trong adapter WiFi hoặc Ethernet
   - Ví dụ: `192.168.1.100` hoặc `192.168.137.1`

2. **Kiểm tra port server:**
   - Xem server logs khi start Tomcat
   - Hoặc thử truy cập:
     - `http://localhost:8080/IoTWebApp/dashboard`
     - `http://localhost:9999/IoTWebApp/dashboard`
     - Xem port nào hoạt động

3. **Test từ browser:**
   - Mở: `http://[IP_MÁY_TÍNH]:[PORT]/IoTWebApp/dashboard`
   - Nếu load được → URL này đúng

### Bước 2: Cập nhật Code ESP32

**Sửa trong file code ESP32:**
```cpp
// Thay đổi dòng này:
const char* serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data";

// Thành IP và port đúng:
const char* serverUrl = "http://[IP_MÁY_TÍNH]:[PORT]/IoTWebApp/api/data";
// Ví dụ: "http://192.168.1.100:9999/IoTWebApp/api/data"
```

### Bước 3: Test kết nối

1. **Upload code mới lên ESP32**

2. **Kiểm tra Serial Monitor:**
   ```
   ✓ WiFi connected
   IP: 192.168.1.50
   Gateway IP: 192.168.1.1
   
   ----- Testing Server Connection -----
   Pinging server: 192.168.1.100
   Sending GET request...
   ✓ Server is reachable. HTTP Code: 405  (hoặc 200)
   ```

3. **Nếu vẫn lỗi -1:**
   - Kiểm tra WiFi gateway IP có khác subnet không
   - Thử ping từ ESP32 đến server IP
   - Kiểm tra firewall

---

## 🔧 CODE ESP32 ĐÃ CẢI THIỆN

File `ESP32_IMPROVED.ino` đã được tạo với:
- ✅ Debug info chi tiết hơn
- ✅ Test server connection trước khi gửi
- ✅ Hiển thị lỗi rõ ràng
- ✅ Kiểm tra WiFi status
- ✅ Timeout settings

**Các bước tiếp theo:**
1. Tìm IP và port đúng của server
2. Cập nhật code ESP32 với IP/port đúng
3. Upload và test lại

---

## 📝 CHECKLIST

- [ ] Server Tomcat đang chạy
- [ ] Xác định đúng IP máy tính chạy server
- [ ] Xác định đúng port server (8080 hay 9999?)
- [ ] ESP32 và máy tính cùng WiFi network
- [ ] Firewall không block port
- [ ] Context path `/IoTWebApp` đúng
- [ ] Code ESP32 có IP và port đúng
- [ ] Test từ browser truy cập được server
- [ ] Upload code mới và test lại

---

## 🎯 KẾT LUẬN

**HTTP Error -1 KHÔNG phải lỗi code Arduino.**  
Vấn đề là **cấu hình mạng** hoặc **server chưa sẵn sàng**.

Hãy:
1. ✅ Kiểm tra server có chạy không
2. ✅ Tìm IP và port đúng
3. ✅ Cập nhật code ESP32
4. ✅ Test lại


