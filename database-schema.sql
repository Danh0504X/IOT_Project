-- =============================================
-- SQL Server Database Schema for IoT System
-- =============================================

-- Create Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'IOT_DB')
BEGIN
    CREATE DATABASE IOT_DB;
END
GO

USE IOT_DB;
GO

-- =============================================
-- Table: DeviceInfo
-- Stores information about IoT devices
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DeviceInfo')
BEGIN
    CREATE TABLE DeviceInfo (
        device_id INT IDENTITY(1,1) PRIMARY KEY,
        device_name NVARCHAR(100) NOT NULL,
        device_type NVARCHAR(50) NOT NULL,
        mac_address NVARCHAR(20),
        ip_address NVARCHAR(50),
        location NVARCHAR(200),
        status NVARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
        registered_at DATETIME DEFAULT GETDATE(),
        last_seen DATETIME,
        CONSTRAINT UQ_DeviceInfo_MacAddress UNIQUE (mac_address)
    );
END
GO

-- =============================================
-- Table: SensorType
-- Defines types of sensors available
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SensorType')
BEGIN
    CREATE TABLE SensorType (
        sensor_type_id INT IDENTITY(1,1) PRIMARY KEY,
        sensor_name NVARCHAR(50) NOT NULL UNIQUE,
        unit NVARCHAR(20),
        description NVARCHAR(255)
    );
END
GO

-- =============================================
-- Table: DeviceSensor
-- Maps which sensors are attached to which devices
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DeviceSensor')
BEGIN
    CREATE TABLE DeviceSensor (
        device_sensor_id INT IDENTITY(1,1) PRIMARY KEY,
        device_id INT NOT NULL,
        sensor_type_id INT NOT NULL,
        sensor_pin NVARCHAR(10),
        status NVARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ERROR')),
        CONSTRAINT FK_DeviceSensor_Device FOREIGN KEY (device_id) REFERENCES DeviceInfo(device_id) ON DELETE CASCADE,
        CONSTRAINT FK_DeviceSensor_SensorType FOREIGN KEY (sensor_type_id) REFERENCES SensorType(sensor_type_id),
        CONSTRAINT UQ_DeviceSensor UNIQUE (device_id, sensor_type_id)
    );
END
GO

-- =============================================
-- Table: SensorData
-- Stores actual sensor readings
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SensorData')
BEGIN
    CREATE TABLE SensorData (
        data_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_id INT NOT NULL,
        sensor_type_id INT NOT NULL,
        sensor_value FLOAT NOT NULL,
        timestamp DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_SensorData_Device FOREIGN KEY (device_id) REFERENCES DeviceInfo(device_id) ON DELETE CASCADE,
        CONSTRAINT FK_SensorData_SensorType FOREIGN KEY (sensor_type_id) REFERENCES SensorType(sensor_type_id)
    );
    
    -- Index for performance
    CREATE INDEX IX_SensorData_DeviceTime ON SensorData(device_id, timestamp DESC);
    CREATE INDEX IX_SensorData_Timestamp ON SensorData(timestamp DESC);
END
GO

-- =============================================
-- Table: SensorSettings
-- Stores threshold values for each sensor type per device
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SensorSettings')
BEGIN
    CREATE TABLE SensorSettings (
        setting_id INT IDENTITY(1,1) PRIMARY KEY,
        device_id INT NOT NULL,
        sensor_type_id INT NOT NULL,
        threshold_value FLOAT NOT NULL,
        alert_condition NVARCHAR(20) DEFAULT 'GREATER_THAN' CHECK (alert_condition IN ('GREATER_THAN', 'LESS_THAN', 'EQUALS', 'NOT_EQUALS')),
        updated_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_SensorSettings_Device FOREIGN KEY (device_id) REFERENCES DeviceInfo(device_id) ON DELETE CASCADE,
        CONSTRAINT FK_SensorSettings_SensorType FOREIGN KEY (sensor_type_id) REFERENCES SensorType(sensor_type_id),
        CONSTRAINT UQ_SensorSettings UNIQUE (device_id, sensor_type_id)
    );
END
GO

-- =============================================
-- Table: SensorDataAggregated
-- Stores aggregated sensor data (hourly/daily)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SensorDataAggregated')
BEGIN
    CREATE TABLE SensorDataAggregated (
        agg_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        device_id INT NOT NULL,
        sensor_type_id INT NOT NULL,
        aggregation_type NVARCHAR(10) CHECK (aggregation_type IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY')),
        avg_value FLOAT,
        min_value FLOAT,
        max_value FLOAT,
        record_count INT,
        period_start DATETIME NOT NULL,
        period_end DATETIME NOT NULL,
        CONSTRAINT FK_SensorDataAgg_Device FOREIGN KEY (device_id) REFERENCES DeviceInfo(device_id) ON DELETE CASCADE,
        CONSTRAINT FK_SensorDataAgg_SensorType FOREIGN KEY (sensor_type_id) REFERENCES SensorType(sensor_type_id)
    );
    
    CREATE INDEX IX_SensorDataAgg_DevicePeriod ON SensorDataAggregated(device_id, aggregation_type, period_start DESC);
END
GO

-- =============================================
-- Table: DeviceCommand
-- Stores commands to be sent to devices
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DeviceCommand')
BEGIN
    CREATE TABLE DeviceCommand (
        command_id INT IDENTITY(1,1) PRIMARY KEY,
        device_id INT NOT NULL,
        command_type NVARCHAR(50) NOT NULL,
        command_value NVARCHAR(255),
        status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'EXECUTED', 'FAILED', 'CANCELLED')),
        created_at DATETIME DEFAULT GETDATE(),
        executed_at DATETIME,
        CONSTRAINT FK_DeviceCommand_Device FOREIGN KEY (device_id) REFERENCES DeviceInfo(device_id) ON DELETE CASCADE
    );
    
    CREATE INDEX IX_DeviceCommand_Status ON DeviceCommand(device_id, status, created_at);
END
GO

-- =============================================
-- Insert Default Sensor Types
-- =============================================
IF NOT EXISTS (SELECT * FROM SensorType WHERE sensor_name = 'temperature')
BEGIN
    INSERT INTO SensorType (sensor_name, unit, description) VALUES
    ('temperature', '°C', 'Temperature sensor'),
    ('humidity', '%', 'Humidity sensor'),
    ('mq1', 'ppm', 'MQ-135 Air Quality sensor'),
    ('mq2', 'ppm', 'MQ-7 Carbon Monoxide sensor'),
    ('mq3', 'ppm', 'MQ-2 Smoke/Gas sensor'),
    ('dust', 'µg/m³', 'Dust particulate sensor PM2.5');
END
GO

-- =============================================
-- Insert Default Device (for testing)
-- =============================================
IF NOT EXISTS (SELECT * FROM DeviceInfo WHERE device_id = 1)
BEGIN
    SET IDENTITY_INSERT DeviceInfo ON;
    INSERT INTO DeviceInfo (device_id, device_name, device_type, mac_address, location, status)
    VALUES (1, 'ESP32-IoT-Device-01', 'ESP32', '00:00:00:00:00:01', 'Office Room', 'ACTIVE');
    SET IDENTITY_INSERT DeviceInfo OFF;
END
GO

-- =============================================
-- Insert Device Sensor Mappings (for testing)
-- =============================================
IF NOT EXISTS (SELECT * FROM DeviceSensor WHERE device_id = 1)
BEGIN
    INSERT INTO DeviceSensor (device_id, sensor_type_id, sensor_pin, status)
    SELECT 1, sensor_type_id, 'GPIO' + CAST(sensor_type_id AS VARCHAR), 'ACTIVE'
    FROM SensorType;
END
GO

-- =============================================
-- Insert Default Settings (for testing)
-- =============================================
IF NOT EXISTS (SELECT * FROM SensorSettings WHERE device_id = 1)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, threshold_value, alert_condition)
    SELECT 
        1, 
        sensor_type_id,
        CASE sensor_name
            WHEN 'temperature' THEN 30.0
            WHEN 'humidity' THEN 80.0
            WHEN 'mq1' THEN 300.0
            WHEN 'mq2' THEN 300.0
            WHEN 'mq3' THEN 600.0
            WHEN 'dust' THEN 50.0
        END,
        'GREATER_THAN'
    FROM SensorType;
END
GO

PRINT 'Database schema created successfully!';
GO
