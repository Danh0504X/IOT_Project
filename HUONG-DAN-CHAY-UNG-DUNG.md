# 🚀 HƯỚNG DẪN CHẠY ỨNG DỤNG IOT WEB APP

## ✅ Đã hoàn thành
- ✔️ Tạo file `index.jsp` - trang chủ đẹp mắt
- ✔️ Sửa lỗi CSS warning trong `dashboard.jsp`
- ✔️ Cấu trúc dự án đầy đủ và đúng chuẩn

## 📋 YÊU CẦU HỆ THỐNG

### 1. Java Development Kit (JDK) 11 trở lên
- Tải từ: https://adoptium.net/
- Sau khi cài, kiểm tra: `java -version`

### 2. Apache Maven
- Tải từ: https://maven.apache.org/download.cgi
- Giải nén và thêm `bin` vào PATH
- Kiểm tra: `mvn -version`

### 3. Apache Tomcat 10.x
- Tải từ: https://tomcat.apache.org/download-10.cgi
- Giải nén vào thư mục (ví dụ: `C:\apache-tomcat-10.1.x`)

### 4. SQL Server
- Đã cài đặt SQL Server
- Đã tạo database `IoTDB`
- Cấu hình trong file `src\main\resources\db.properties`:
  ```properties
  db.url=jdbc:sqlserver://localhost:1433;databaseName=IoTDB;encrypt=true;trustServerCertificate=true
  db.username=sa
  db.password=YourPassword123
  ```

## 🔧 BƯỚC 1: KIỂM TRA CÔNG CỤ

Mở PowerShell và chạy:

```powershell
# Kiểm tra Java
java -version

# Kiểm tra Maven
mvn -version
```

## 🗄️ BƯỚC 2: THIẾT LẬP DATABASE

Chạy các script SQL theo thứ tự:

```powershell
# 1. Tạo schema database
sqlcmd -S localhost -U sa -P YourPassword123 -i database-schema.sql

# 2. Thêm dữ liệu mẫu
sqlcmd -S localhost -U sa -P YourPassword123 -i insert-sample-data.sql

# 3. Kiểm tra dữ liệu
sqlcmd -S localhost -U sa -P YourPassword123 -i check-data-quick.sql
```

## 📦 BƯỚC 3: BUILD DỰ ÁN

```powershell
# Di chuyển vào thư mục dự án
cd d:\PRJ\IoTWebApp

# Clean và build
mvn clean package
```

Sau khi build thành công, file WAR sẽ được tạo tại:
`target\IoTWebApp-1.0-SNAPSHOT.war`

## 🚀 BƯỚC 4: DEPLOY VÀO TOMCAT

### Cách 1: Deploy thủ công
1. Copy file `target\IoTWebApp-1.0-SNAPSHOT.war`
2. Paste vào thư mục `<TOMCAT_HOME>\webapps\`
3. Đổi tên thành `IoTWebApp.war` (bỏ version)
4. Khởi động Tomcat:
   ```powershell
   # Windows
   <TOMCAT_HOME>\bin\startup.bat
   ```

### Cách 2: Sử dụng Maven Tomcat Plugin
Thêm vào `pom.xml` (trong `<build><plugins>`):

```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <url>http://localhost:8080/manager/text</url>
        <server>TomcatServer</server>
        <path>/IoTWebApp</path>
    </configuration>
</plugin>
```

Sau đó chạy:
```powershell
mvn tomcat7:deploy
```

## 🌐 BƯỚC 5: TRUY CẬP ỨNG DỤNG

Sau khi Tomcat khởi động, mở trình duyệt:

- **Trang chủ**: http://localhost:8080/IoTWebApp/
- **Dashboard**: http://localhost:8080/IoTWebApp/dashboard
- **Quản lý ngưỡng**: http://localhost:8080/IoTWebApp/admin/thresholds

## 📡 BƯỚC 6: KIỂM TRA API (Optional)

### Test nhận dữ liệu từ ESP32:
```powershell
# Sử dụng PowerShell để gửi POST request
$body = @{
    deviceId = 1
    temperature = 25.5
    humidity = 60.0
    light = 450.0
    motion = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/IoTWebApp/api/data" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Test polling thresholds:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/IoTWebApp/api/poll?deviceId=1" `
    -Method GET
```

## 🐛 XỬ LÝ SỰ CỐ

### Lỗi: Port 8080 đã được sử dụng
Đổi port trong `<TOMCAT_HOME>\conf\server.xml`:
```xml
<Connector port="8080" protocol="HTTP/1.1" ... />
```
Thay `8080` thành port khác (ví dụ: `9090`)

### Lỗi: Không kết nối được database
1. Kiểm tra SQL Server đang chạy
2. Xác nhận thông tin kết nối trong `db.properties`
3. Test connection:
   ```powershell
   sqlcmd -S localhost -U sa -P YourPassword123 -Q "SELECT @@VERSION"
   ```

### Lỗi: 404 Not Found
1. Kiểm tra file WAR đã được deploy vào `webapps`
2. Kiểm tra logs trong `<TOMCAT_HOME>\logs\catalina.out`
3. Đảm bảo context path đúng: `/IoTWebApp`

### Lỗi build Maven
```powershell
# Xóa cache Maven và build lại
mvn clean
mvn dependency:purge-local-repository
mvn package
```

## 📊 CẤU TRÚC ENDPOINTS

| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/` | GET | Trang chủ |
| `/dashboard` | GET | Dashboard hiển thị dữ liệu cảm biến |
| `/admin/thresholds` | GET/POST | Quản lý ngưỡng cảnh báo |
| `/api/data` | POST | Nhận dữ liệu từ ESP32 |
| `/api/poll` | GET | ESP32 lấy cập nhật ngưỡng |

## 🎯 TÍNH NĂNG

✨ **Đã triển khai**:
- ✅ Trang chủ hiện đại với UI đẹp mắt
- ✅ Dashboard real-time với biểu đồ Chart.js
- ✅ Quản lý ngưỡng cảnh báo
- ✅ REST API cho ESP32
- ✅ Database schema chuẩn hóa
- ✅ JPA/Hibernate ORM
- ✅ Responsive design

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra logs Tomcat: `<TOMCAT_HOME>\logs\`
2. Kiểm tra console trong browser (F12)
3. Xem file `INTEGRATION-COMPLETE.md` để biết chi tiết kỹ thuật

---

**Lưu ý**: Đảm bảo tất cả công cụ (JDK, Maven, Tomcat, SQL Server) đã được cài đặt và cấu hình đúng trước khi chạy ứng dụng.
