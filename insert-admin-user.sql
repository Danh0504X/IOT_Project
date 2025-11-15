-- =====================================================
-- INSERT TÀI KHOẢN ADMIN
-- =====================================================
USE AirQualityManagement;
GO

-- Option 1: INSERT với password plain text (để test - KHÔNG AN TOÀN cho production)
-- Password: admin123
INSERT INTO [User] (Username, Email, PasswordHash, FullName, Phone, Role, IsActive)
VALUES 
    (N'admin', N'admin@example.com', N'admin123', N'Administrator', N'0123456789', N'Admin', 1);

-- Option 2: INSERT với password đã hash SHA256 (AN TOÀN HƠN)
-- Password: admin123
-- SHA256 hash của "admin123" = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- INSERT INTO [User] (Username, Email, PasswordHash, FullName, Phone, Role, IsActive)
-- VALUES 
--     (N'admin', N'admin@example.com', N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', N'Administrator', N'0123456789', N'Admin', 1);

-- Option 3: INSERT với password hash bằng HASHBYTES (SQL Server built-in)
-- Password: admin123
-- INSERT INTO [User] (Username, Email, PasswordHash, FullName, Phone, Role, IsActive)
-- VALUES 
--     (N'admin', N'admin@example.com', CONVERT(NVARCHAR(255), HASHBYTES('SHA2_256', N'admin123'), 2), N'Administrator', N'0123456789', N'Admin', 1);

-- Kiểm tra tài khoản đã được tạo
SELECT UserID, Username, Email, FullName, Role, IsActive 
FROM [User] 
WHERE Username = N'admin';

GO

