# 🔧 SỬA LỖI MAPPING - TRANG INDEX KHÔNG LINK ĐƯỢC

## ❌ Vấn đề đã phát hiện

Servlet `DashboardServlet` đang map cả `/` (root URL) và `/dashboard`, điều này khiến:
- Trang `index.jsp` không hiển thị được
- Các nút link không hoạt động đúng

## ✅ Đã sửa

Đã thay đổi trong file `DashboardServlet.java`:

**Trước:**
```java
@WebServlet(name = "dashboardServlet", urlPatterns = {"/", "/dashboard"})
```

**Sau:**
```java
@WebServlet(name = "dashboardServlet", urlPatterns = "/dashboard")
```

## 🚀 CÁC BƯỚC BUILD LẠI TRONG NETBEANS

### Cách 1: Build và Deploy trực tiếp trong NetBeans

1. **Dừng Tomcat server cũ:**
   - Trong tab "Services" → "Servers"
   - Chuột phải vào "Apache Tomcat" → **Stop**

2. **Clean và Build project:**
   - Chuột phải vào project "IoTWebApp" trong Projects
   - Chọn **"Clean and Build"**
   - Hoặc nhấn **Shift + F11**

3. **Run lại project:**
   - Chuột phải vào project "IoTWebApp"
   - Chọn **"Run"**
   - Hoặc nhấn **F6**

### Cách 2: Build bằng PowerShell (nếu đã cài Maven)

```powershell
cd d:\PRJ\IoTWebApp

# Build project
mvn clean package

# Copy WAR file vào Tomcat
copy target\IoTWebApp-1.0-SNAPSHOT.war C:\<TOMCAT_HOME>\webapps\IoTWebApp.war
```

### Cách 3: Deploy thủ công

1. **Tìm file WAR:**
   - Sau khi build, file WAR nằm ở: `d:\PRJ\IoTWebApp\target\IoTWebApp-1.0-SNAPSHOT.war`

2. **Deploy vào Tomcat:**
   - Copy file WAR vào `<TOMCAT_HOME>\webapps\`
   - Đổi tên thành `IoTWebApp.war`
   - Restart Tomcat

## 🌐 SAU KHI DEPLOY XONG

Truy cập các URL sau:

| Trang | URL | Mô tả |
|-------|-----|-------|
| **Trang chủ** | http://localhost:8080/IoTWebApp/ | Trang index.jsp với 2 nút |
| **Dashboard** | http://localhost:8080/IoTWebApp/dashboard | Hiển thị dữ liệu cảm biến |
| **Quản lý** | http://localhost:8080/IoTWebApp/admin/thresholds | Quản lý ngưỡng cảnh báo |

## ✨ KẾT QUẢ MONG ĐỢI

Sau khi build lại:

1. ✅ Trang **index.jsp** hiển thị đúng ở URL root (`/`)
2. ✅ Nút **"Xem Dashboard"** dẫn đến `/dashboard` → Hiển thị biểu đồ dữ liệu
3. ✅ Nút **"Quản lý Ngưỡng"** dẫn đến `/admin/thresholds` → Form quản lý threshold

## 🐛 NẾU VẪN KHÔNG CHẠY

### Kiểm tra Console Log

Trong NetBeans, xem tab **Output** để kiểm tra lỗi:
- Tìm dòng có chữ "ERROR" hoặc "Exception"
- Kiểm tra deployment có thành công không

### Xóa Cache Tomcat

```powershell
# Dừng Tomcat
# Xóa thư mục work và webapps\IoTWebApp
cd <TOMCAT_HOME>
Remove-Item -Recurse -Force work\*
Remove-Item -Recurse -Force webapps\IoTWebApp
Remove-Item -Force webapps\IoTWebApp.war

# Deploy lại từ đầu
```

### Kiểm tra Database

Đảm bảo database đang chạy và có dữ liệu:

```powershell
sqlcmd -S localhost -U sa -P YourPassword123 -d IoTDB -Q "SELECT TOP 5 * FROM SensorData ORDER BY timestamp DESC"
```

## 📝 GHI CHÚ KỸ THUẬT

### URL Mapping hiện tại:

| Servlet | URL Pattern | Mô tả |
|---------|-------------|-------|
| (none) | `/` | Hiển thị `index.jsp` (welcome file) |
| DashboardServlet | `/dashboard` | Dashboard với biểu đồ |
| ThresholdAdminServlet | `/admin/thresholds` | Quản lý threshold |
| DataServlet | `/api/data` | REST API nhận dữ liệu ESP32 |
| PollServlet | `/api/poll` | REST API ESP32 poll commands |

### Context Path
- Context path mặc định: `/IoTWebApp`
- Có thể đổi bằng cách rename file WAR thành `ROOT.war` → context path là `/`

## ✅ CHECKLIST BUILD & DEPLOY

- [ ] Đã sửa file `DashboardServlet.java`
- [ ] Đã build lại project (Clean and Build)
- [ ] Tomcat đã khởi động thành công
- [ ] Database SQL Server đang chạy
- [ ] Có thể truy cập được trang index
- [ ] Nút "Xem Dashboard" hoạt động
- [ ] Nút "Quản lý Ngưỡng" hoạt động

---

**Lưu ý quan trọng:** Sau mỗi lần sửa code Java, **PHẢI** build lại project để thay đổi có hiệu lực!
