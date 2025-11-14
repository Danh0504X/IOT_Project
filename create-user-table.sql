-- =============================================
-- CREATE USER TABLE FOR LOGIN
-- =============================================

USE IOT_DB;
GO

-- Create Users table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    CREATE TABLE Users (
        user_id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100),
        role NVARCHAR(20) DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER')),
        is_active BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETUTCDATE(),
        last_login DATETIME,
        CONSTRAINT UQ_Users_Username UNIQUE (username)
    );
END
GO

-- Insert default admin account
-- Username: admin
-- Password: admin123 (in production, use hashed password)
IF NOT EXISTS (SELECT * FROM Users WHERE username = 'admin')
BEGIN
    INSERT INTO Users (username, password, full_name, email, role, is_active, created_at)
    VALUES ('admin', 'admin123', N'Administrator', 'admin@iot.local', 'ADMIN', 1, GETUTCDATE());
END
GO

-- Insert default user account
-- Username: user
-- Password: user123
IF NOT EXISTS (SELECT * FROM Users WHERE username = 'user')
BEGIN
    INSERT INTO Users (username, password, full_name, email, role, is_active, created_at)
    VALUES ('user', 'user123', N'Regular User', 'user@iot.local', 'USER', 1, GETUTCDATE());
END
GO

PRINT '=============================================';
PRINT 'Users table created successfully!';
PRINT '=============================================';
PRINT 'Default accounts:';
PRINT '  Admin - username: admin, password: admin123';
PRINT '  User  - username: user, password: user123';
PRINT '=============================================';
GO
