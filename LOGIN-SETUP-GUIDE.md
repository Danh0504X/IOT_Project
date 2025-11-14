# Hướng dẫn cài đặt tính năng đăng nhập

## Bước 1: Tạo bảng Users trong database

1. Mở **SQL Server Management Studio** (SSMS)
2. Kết nối tới SQL Server: `localhost` với user `sa` / `123456789`
3. Mở file `create-user-table.sql`
4. Chạy script này để tạo bảng Users và 2 tài khoản mặc định

## Bước 2: Clean and Build project

1. Mở **NetBeans IDE**
2. Right-click vào project **IoTWebApp**
3. Chọn **Clean and Build**
4. Đợi build hoàn tất

## Bước 3: Restart Tomcat

1. Trong NetBeans, stop Tomcat nếu đang chạy
2. Start lại Tomcat server

## Bước 4: Truy cập ứng dụng

1. Mở trình duyệt web
2. Truy cập: `http://localhost:9999/IoTWebApp/dashboard`
3. Bạn sẽ được chuyển hướng đến trang đăng nhập

## Tài khoản mặc định

### Admin Account
- **Username:** admin
- **Password:** admin123

### User Account
- **Username:** user
- **Password:** user123

## Các tính năng đã thêm

✅ **Trang đăng nhập** (`/login`)
   - Giao diện đẹp với form đăng nhập
   - Hiển thị thông báo lỗi khi sai thông tin
   - Checkbox "Ghi nhớ đăng nhập"

✅ **Authentication Filter**
   - Bảo vệ các trang: `/dashboard`, `/admin/*`, `/poll`
   - Tự động redirect về login nếu chưa đăng nhập
   - Session timeout: 30 phút

✅ **Chức năng đăng xuất**
   - Nút "Đăng xuất" trên dashboard
   - Nút "Đăng xuất" trên trang thresholds
   - Hiển thị tên người dùng đang đăng nhập

✅ **User Management**
   - Model `User.java`
   - DAO `UserDAO.java` với các phương thức:
     - authenticate(username, password)
     - findByUsername(username)
     - updateLastLogin(userId)

✅ **Security**
   - Kiểm tra session trước khi truy cập các trang
   - Cập nhật thời gian đăng nhập cuối
   - Chỉ user active mới được đăng nhập

## Cấu trúc files mới

```
IoTWebApp/
├── create-user-table.sql              (Script tạo bảng Users)
├── src/main/java/com/mycompany/iotwebapp/
│   ├── controller/
│   │   ├── LoginServlet.java          (Xử lý đăng nhập)
│   │   └── LogoutServlet.java         (Xử lý đăng xuất)
│   ├── dao/
│   │   └── UserDAO.java               (Truy vấn database Users)
│   ├── model/
│   │   └── User.java                  (Model User)
│   └── filter/
│       └── AuthenticationFilter.java  (Bảo vệ các trang)
└── src/main/webapp/WEB-INF/views/
    └── login.jsp                      (Trang đăng nhập)
```

## Flow hoạt động

1. **User chưa đăng nhập** → Truy cập `/dashboard`
2. **AuthenticationFilter** → Kiểm tra session
3. **Không có session** → Redirect về `/login`
4. **User nhập username/password** → Submit form
5. **LoginServlet** → Gọi `UserDAO.authenticate()`
6. **Xác thực thành công** → Tạo session, redirect về `/dashboard`
7. **Click "Đăng xuất"** → `LogoutServlet` xóa session, redirect về `/login`

## Lưu ý bảo mật

⚠️ **Trong môi trường production:**
- Hash mật khẩu bằng BCrypt hoặc Argon2
- Sử dụng HTTPS
- Thêm CSRF protection
- Rate limiting cho login attempts
- Session management với Redis
- Two-factor authentication (2FA)

## Test thử

1. Truy cập: `http://localhost:9999/IoTWebApp/dashboard`
2. Đăng nhập với: `admin` / `admin123`
3. Xem dashboard với tên người dùng hiển thị
4. Click "Đăng xuất"
5. Kiểm tra được redirect về login page

## Troubleshooting

**Lỗi: Không redirect về login**
→ Kiểm tra AuthenticationFilter đã được deploy chưa
→ Clean and Build lại project

**Lỗi: SQL Exception khi login**
→ Kiểm tra bảng Users đã được tạo chưa
→ Chạy lại `create-user-table.sql`

**Lỗi: Session timeout quá nhanh**
→ Tăng giá trị trong LoginServlet.java: `session.setMaxInactiveInterval(30 * 60);`

## Hoàn tất! 🎉

Ứng dụng của bạn giờ đây đã có tính năng đăng nhập bảo mật!
