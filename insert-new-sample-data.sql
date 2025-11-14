-- =============================================
-- INSERT SAMPLE DATA FOR IOT_DB DATABASE
-- Chạy script này trong SQL Server Management Studio
-- =============================================

USE IOT_DB;
GO

-- Xóa dữ liệu cũ (nếu có)
DELETE FROM SensorData;
DELETE FROM DeviceSensor;
DELETE FROM SensorSettings;
DELETE FROM DeviceCommand;
DELETE FROM DeviceInfo;
DELETE FROM SensorType;
GO

-- =============================================
-- 1. INSERT SENSOR TYPES
-- =============================================
SET IDENTITY_INSERT SensorType ON;

INSERT INTO SensorType (sensor_type_id, sensor_code, name, unit, data_type, is_active, created_at, description) VALUES
(1, 'TEMP', N'Temperature', '°C', 'FLOAT', 1, GETUTCDATE(), N'Temperature sensor'),
(2, 'HUM', N'Humidity', '%', 'FLOAT', 1, GETUTCDATE(), N'Humidity sensor'),
(3, 'MQ1', N'Gas MQ-135', 'ppm', 'FLOAT', 1, GETUTCDATE(), N'Air quality sensor'),
(4, 'MQ2', N'Gas MQ-7', 'ppm', 'FLOAT', 1, GETUTCDATE(), N'Carbon monoxide sensor'),
(5, 'MQ3', N'Gas MQ-2', 'ppm', 'FLOAT', 1, GETUTCDATE(), N'Smoke and gas sensor'),
(6, 'DUST', N'Dust Sensor', 'µg/m³', 'FLOAT', 1, GETUTCDATE(), N'PM2.5 dust sensor');

SET IDENTITY_INSERT SensorType OFF;
GO

-- =============================================
-- 2. INSERT DEVICE INFO
-- =============================================
INSERT INTO DeviceInfo (device_id, device_name, status, install_date, latitude, longitude, created_at, updated_at) VALUES
('DEVICE001', N'IoT Device - Living Room', 'ACTIVE', GETUTCDATE(), 10.8231, 106.6297, GETUTCDATE(), NULL),
('DEVICE002', N'IoT Device - Bedroom', 'ACTIVE', GETUTCDATE(), 10.8232, 106.6298, GETUTCDATE(), NULL);
GO

-- =============================================
-- 3. INSERT DEVICE SENSORS
-- =============================================
INSERT INTO DeviceSensor (device_id, sensor_type_id, sensor_index, is_active, installed_at) VALUES
('DEVICE001', 1, 0, 1, GETUTCDATE()),
('DEVICE001', 2, 0, 1, GETUTCDATE()),
('DEVICE001', 3, 0, 1, GETUTCDATE()),
('DEVICE001', 4, 0, 1, GETUTCDATE()),
('DEVICE001', 5, 0, 1, GETUTCDATE()),
('DEVICE001', 6, 0, 1, GETUTCDATE());
GO

-- =============================================
-- 4. INSERT SENSOR SETTINGS (THRESHOLDS)
-- =============================================
SET IDENTITY_INSERT SensorSettings ON;

INSERT INTO SensorSettings (id, device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at) VALUES
(1, 'DEVICE001', 1, 1.0, 1.0, 30.0, GETUTCDATE(), GETUTCDATE()),  -- Temperature threshold: 30°C
(2, 'DEVICE001', 2, 1.0, 1.0, 70.0, GETUTCDATE(), GETUTCDATE()),  -- Humidity threshold: 70%
(3, 'DEVICE001', 3, 1.0, 1.0, 300.0, GETUTCDATE(), GETUTCDATE()), -- MQ1 threshold: 300 ppm
(4, 'DEVICE001', 4, 1.0, 1.0, 300.0, GETUTCDATE(), GETUTCDATE()), -- MQ2 threshold: 300 ppm
(5, 'DEVICE001', 5, 1.0, 1.0, 600.0, GETUTCDATE(), GETUTCDATE()), -- MQ3 threshold: 600 ppm
(6, 'DEVICE001', 6, 1.0, 1.0, 50.0, GETUTCDATE(), GETUTCDATE());  -- Dust threshold: 50 µg/m³

SET IDENTITY_INSERT SensorSettings OFF;
GO

-- =============================================
-- 5. INSERT SENSOR DATA (LAST 24 HOURS)
-- Tạo dữ liệu cho 24 giờ qua, mỗi 15 phút 1 lần
-- =============================================
SET IDENTITY_INSERT SensorData ON;

DECLARE @counter INT = 0;
DECLARE @maxRecords INT = 96; -- 24 hours * 4 records per hour
DECLARE @currentId BIGINT = 1;
DECLARE @baseTemp FLOAT = 25.0;
DECLARE @baseHumidity FLOAT = 60.0;
DECLARE @baseMQ1 FLOAT = 250.0;
DECLARE @baseMQ2 FLOAT = 280.0;
DECLARE @baseMQ3 FLOAT = 550.0;
DECLARE @baseDust FLOAT = 35.0;

WHILE @counter < @maxRecords
BEGIN
    DECLARE @timestamp DATETIME2 = DATEADD(MINUTE, -15 * (@maxRecords - @counter - 1), GETDATE());
    DECLARE @timeVariation FLOAT = SIN(@counter * 0.1) * 2; -- Tạo biến động theo thời gian
    DECLARE @randomFactor FLOAT = (RAND() - 0.5) * 2; -- Random factor từ -1 đến 1
    
    -- Temperature (25-28°C với biến động)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 1, 0, @timestamp, 
            @baseTemp + @timeVariation + @randomFactor, 
            1, @timestamp,
            @baseTemp + @timeVariation + @randomFactor,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    -- Humidity (55-65%)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 2, 0, @timestamp, 
            @baseHumidity + @timeVariation * 2 + @randomFactor * 3, 
            1, @timestamp,
            @baseHumidity + @timeVariation * 2 + @randomFactor * 3,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    -- MQ1 (240-260 ppm)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 3, 0, @timestamp, 
            @baseMQ1 + @timeVariation * 3 + @randomFactor * 5, 
            1, @timestamp,
            @baseMQ1 + @timeVariation * 3 + @randomFactor * 5,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    -- MQ2 (270-290 ppm)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 4, 0, @timestamp, 
            @baseMQ2 + @timeVariation * 2 + @randomFactor * 4, 
            1, @timestamp,
            @baseMQ2 + @timeVariation * 2 + @randomFactor * 4,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    -- MQ3 (540-560 ppm)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 5, 0, @timestamp, 
            @baseMQ3 + @timeVariation * 4 + @randomFactor * 6, 
            1, @timestamp,
            @baseMQ3 + @timeVariation * 4 + @randomFactor * 6,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    -- Dust (30-40 µg/m³)
    INSERT INTO SensorData (id, device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES (@currentId, 'DEVICE001', 6, 0, @timestamp, 
            @baseDust + @timeVariation * 2 + @randomFactor * 3, 
            1, @timestamp,
            @baseDust + @timeVariation * 2 + @randomFactor * 3,
            CAST(@timestamp AS DATETIME));
    SET @currentId = @currentId + 1;
    
    SET @counter = @counter + 1;
END

SET IDENTITY_INSERT SensorData OFF;
GO

-- =============================================
-- 6. INSERT SAMPLE COMMANDS
-- =============================================
SET IDENTITY_INSERT DeviceCommand ON;

INSERT INTO DeviceCommand (id, device_id, command_type, command_value, status, issued_at, executed_at) VALUES
(1, 'DEVICE001', 'REBOOT', 'now', 'EXECUTED', DATEADD(HOUR, -2, GETUTCDATE()), DATEADD(HOUR, -1, GETUTCDATE())),
(2, 'DEVICE001', 'UPDATE_INTERVAL', '60', 'EXECUTED', DATEADD(HOUR, -1, GETUTCDATE()), DATEADD(MINUTE, -30, GETUTCDATE())),
(3, 'DEVICE001', 'CALIBRATE', 'all_sensors', 'PENDING', GETUTCDATE(), NULL);

SET IDENTITY_INSERT DeviceCommand OFF;
GO

-- =============================================
-- VERIFICATION - Kiểm tra dữ liệu đã insert
-- =============================================
PRINT '=============================================';
PRINT 'DATA INSERTION COMPLETED!';
PRINT '=============================================';
PRINT '';

-- Đếm số records
PRINT 'Record counts:';
SELECT 'SensorType' AS TableName, COUNT(*) AS RecordCount FROM SensorType
UNION ALL
SELECT 'DeviceInfo', COUNT(*) FROM DeviceInfo
UNION ALL
SELECT 'DeviceSensor', COUNT(*) FROM DeviceSensor
UNION ALL
SELECT 'SensorSettings', COUNT(*) FROM SensorSettings
UNION ALL
SELECT 'SensorData', COUNT(*) FROM SensorData
UNION ALL
SELECT 'DeviceCommand', COUNT(*) FROM DeviceCommand;

PRINT '';
PRINT '=============================================';
PRINT 'Latest sensor readings for DEVICE001:';
PRINT '=============================================';

-- Hiển thị dữ liệu mới nhất
SELECT TOP 10
    sd.id,
    sd.device_id,
    st.name AS sensor_name,
    CAST(sd.sensor_value AS DECIMAL(10,2)) AS value,
    st.unit,
    sd.timestamp
FROM SensorData sd
INNER JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
WHERE sd.device_id = 'DEVICE001'
ORDER BY sd.timestamp DESC;

PRINT '';
PRINT '=============================================';
PRINT 'Sensor value statistics:';
PRINT '=============================================';

SELECT 
    st.name AS sensor_name,
    st.unit,
    COUNT(*) AS total_records,
    CAST(MIN(sd.sensor_value) AS DECIMAL(10,2)) AS min_value,
    CAST(AVG(sd.sensor_value) AS DECIMAL(10,2)) AS avg_value,
    CAST(MAX(sd.sensor_value) AS DECIMAL(10,2)) AS max_value
FROM SensorData sd
INNER JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
WHERE sd.device_id = 'DEVICE001'
GROUP BY st.name, st.unit
ORDER BY st.name;

PRINT '';
PRINT '=============================================';
PRINT 'You can now access the dashboard at:';
PRINT 'http://localhost:9999/IoTWebApp/dashboard';
PRINT '=============================================';
GO
