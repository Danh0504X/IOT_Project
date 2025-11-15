-- Script để chèn dữ liệu mẫu đầy đủ cho DEVICE001
-- Chèn dữ liệu mẫu cho tất cả các sensor types

USE [IOT_DB]
GO

-- Xóa dữ liệu cũ (tùy chọn - chỉ dùng khi test)
-- DELETE FROM SensorData WHERE device_id = 'DEVICE001';
-- GO

-- Chèn dữ liệu mẫu cho Temperature (sensor_type_id = 1)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 1 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 1, 0, GETDATE(), 25.5, 1, GETDATE(), 25.5, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 1, 0, DATEADD(MINUTE, -5, GETDATE()), 26.0, 1, GETDATE(), 26.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 1, 0, DATEADD(MINUTE, -10, GETDATE()), 25.8, 1, GETDATE(), 25.8, DATEADD(MINUTE, -10, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 1, 0, DATEADD(MINUTE, -15, GETDATE()), 26.2, 1, GETDATE(), 26.2, DATEADD(MINUTE, -15, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 1, 0, DATEADD(MINUTE, -20, GETDATE()), 25.9, 1, GETDATE(), 25.9, DATEADD(MINUTE, -20, GETDATE()));
    
    PRINT 'Temperature data inserted';
END

-- Chèn dữ liệu mẫu cho Humidity (sensor_type_id = 2)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 2 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 2, 0, GETDATE(), 60.0, 1, GETDATE(), 60.0, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 2, 0, DATEADD(MINUTE, -5, GETDATE()), 61.0, 1, GETDATE(), 61.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 2, 0, DATEADD(MINUTE, -10, GETDATE()), 59.5, 1, GETDATE(), 59.5, DATEADD(MINUTE, -10, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 2, 0, DATEADD(MINUTE, -15, GETDATE()), 60.5, 1, GETDATE(), 60.5, DATEADD(MINUTE, -15, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 2, 0, DATEADD(MINUTE, -20, GETDATE()), 59.8, 1, GETDATE(), 59.8, DATEADD(MINUTE, -20, GETDATE()));
    
    PRINT 'Humidity data inserted';
END

-- Chèn dữ liệu mẫu cho Gas MQ1 (sensor_type_id = 3)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 3 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 3, 0, GETDATE(), 300.0, 1, GETDATE(), 300.0, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 3, 0, DATEADD(MINUTE, -5, GETDATE()), 310.0, 1, GETDATE(), 310.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 3, 0, DATEADD(MINUTE, -10, GETDATE()), 295.0, 1, GETDATE(), 295.0, DATEADD(MINUTE, -10, GETDATE()));
    
    PRINT 'Gas MQ1 data inserted';
END

-- Chèn dữ liệu mẫu cho Gas MQ2 (sensor_type_id = 4)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 4 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 4, 0, GETDATE(), 250.0, 1, GETDATE(), 250.0, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 4, 0, DATEADD(MINUTE, -5, GETDATE()), 260.0, 1, GETDATE(), 260.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 4, 0, DATEADD(MINUTE, -10, GETDATE()), 240.0, 1, GETDATE(), 240.0, DATEADD(MINUTE, -10, GETDATE()));
    
    PRINT 'Gas MQ2 data inserted';
END

-- Chèn dữ liệu mẫu cho Gas MQ3 (sensor_type_id = 5)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 5 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 5, 0, GETDATE(), 600.0, 1, GETDATE(), 600.0, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 5, 0, DATEADD(MINUTE, -5, GETDATE()), 610.0, 1, GETDATE(), 610.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 5, 0, DATEADD(MINUTE, -10, GETDATE()), 590.0, 1, GETDATE(), 590.0, DATEADD(MINUTE, -10, GETDATE()));
    
    PRINT 'Gas MQ3 data inserted';
END

-- Chèn dữ liệu mẫu cho Dust Density (sensor_type_id = 6)
IF NOT EXISTS (SELECT 1 FROM SensorData WHERE device_id = 'DEVICE001' AND sensor_type_id = 6 AND timestamp > DATEADD(HOUR, -1, GETDATE()))
BEGIN
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 6, 0, GETDATE(), 50.0, 1, GETDATE(), 50.0, GETDATE());
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 6, 0, DATEADD(MINUTE, -5, GETDATE()), 52.0, 1, GETDATE(), 52.0, DATEADD(MINUTE, -5, GETDATE()));
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_index, ts, value, is_validated, created_at, sensor_value, timestamp)
    VALUES ('DEVICE001', 6, 0, DATEADD(MINUTE, -10, GETDATE()), 48.0, 1, GETDATE(), 48.0, DATEADD(MINUTE, -10, GETDATE()));
    
    PRINT 'Dust Density data inserted';
END

GO

PRINT 'Sample data insertion completed!';
PRINT 'Run this query to verify: SELECT * FROM SensorData WHERE device_id = ''DEVICE001'' ORDER BY timestamp DESC;';



