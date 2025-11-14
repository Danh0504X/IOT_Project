-- Script để kiểm tra và tạo thresholds mẫu cho DEVICE001
-- Chạy script này trong SQL Server Management Studio

USE [IOT_DB]
GO

-- 1. Kiểm tra xem có thresholds nào trong SensorSettings không
SELECT 
    ss.id,
    ss.device_id,
    ss.sensor_type_id,
    st.name AS sensor_name,
    st.sensor_code,
    ss.threshold_value,
    ss.sensitivity,
    ss.calibration_factor,
    ss.updated_at,
    ss.created_at
FROM SensorSettings ss
INNER JOIN SensorType st ON ss.sensor_type_id = st.sensor_type_id
WHERE ss.device_id = 'DEVICE001'
ORDER BY ss.sensor_type_id;

-- 2. Nếu không có dữ liệu, INSERT thresholds mẫu cho DEVICE001
-- Chạy script này nếu kết quả query trên trả về 0 rows

-- Threshold cho Temperature (sensor_type_id = 1)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 1)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 1, 1.0, 1.0, 30.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Temperature: 30.0°C';
END

-- Threshold cho Humidity (sensor_type_id = 2)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 2)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 2, 1.0, 1.0, 70.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Humidity: 70.0%';
END

-- Threshold cho Gas MQ1 - MQ-135 (sensor_type_id = 3)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 3)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 3, 1.0, 1.0, 300.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Gas MQ1 (MQ-135): 300.0';
END

-- Threshold cho Gas MQ2 - MQ-7 (sensor_type_id = 4)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 4)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 4, 1.0, 1.0, 300.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Gas MQ2 (MQ-7): 300.0';
END

-- Threshold cho Gas MQ3 - MQ-2 (sensor_type_id = 5)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 5)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 5, 1.0, 1.0, 600.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Gas MQ3 (MQ-2): 600.0';
END

-- Threshold cho Dust Density (sensor_type_id = 6)
IF NOT EXISTS (SELECT 1 FROM SensorSettings WHERE device_id = 'DEVICE001' AND sensor_type_id = 6)
BEGIN
    INSERT INTO SensorSettings (device_id, sensor_type_id, sensitivity, calibration_factor, threshold_value, updated_at, created_at)
    VALUES ('DEVICE001', 6, 1.0, 1.0, 50.0, GETUTCDATE(), GETUTCDATE());
    PRINT 'Inserted threshold for Dust Density: 50.0 µg/m³';
END

-- 3. Kiểm tra lại sau khi INSERT
SELECT 
    ss.device_id,
    st.name AS sensor_name,
    ss.threshold_value,
    st.unit
FROM SensorSettings ss
INNER JOIN SensorType st ON ss.sensor_type_id = st.sensor_type_id
WHERE ss.device_id = 'DEVICE001'
ORDER BY ss.sensor_type_id;

PRINT 'Thresholds check completed!';

