-- =============================================
-- Insert Sample Data for IoTDB Database
-- Run this in SQL Server Management Studio
-- =============================================

USE IoTDB;
GO

-- Insert sample sensor data with multiple timestamps
-- This will create data for the last 10 time points

DECLARE @i INT = 0;
DECLARE @deviceId INT = 1;

WHILE @i < 10
BEGIN
    -- Temperature (sensor_type_id = 1)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 1, 25.0 + (@i * 0.5) + (RAND() * 2), DATEADD(MINUTE, -@i, GETDATE()));
    
    -- Humidity (sensor_type_id = 2)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 2, 60.0 + (@i * 0.3) + (RAND() * 5), DATEADD(MINUTE, -@i, GETDATE()));
    
    -- MQ1 (sensor_type_id = 3)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 3, 250.0 + (@i * 5) + (RAND() * 20), DATEADD(MINUTE, -@i, GETDATE()));
    
    -- MQ2 (sensor_type_id = 4)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 4, 280.0 + (@i * 3) + (RAND() * 15), DATEADD(MINUTE, -@i, GETDATE()));
    
    -- MQ3 (sensor_type_id = 5)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 5, 550.0 + (@i * 10) + (RAND() * 30), DATEADD(MINUTE, -@i, GETDATE()));
    
    -- Dust (sensor_type_id = 6)
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES (@deviceId, 6, 35.0 + (@i * 2) + (RAND() * 10), DATEADD(MINUTE, -@i, GETDATE()));
    
    SET @i = @i + 1;
END

-- Update device last_seen
UPDATE DeviceInfo 
SET last_seen = GETDATE()
WHERE device_id = @deviceId;

GO

-- Verify inserted data
PRINT '=============================================';
PRINT 'Sample data inserted successfully!';
PRINT '=============================================';
PRINT '';

-- Count total records
SELECT 'Total SensorData records' as Info, COUNT(*) as Count FROM SensorData;

-- Show latest data by sensor type
SELECT 
    st.sensor_name,
    COUNT(*) as record_count,
    AVG(sd.sensor_value) as avg_value,
    MIN(sd.sensor_value) as min_value,
    MAX(sd.sensor_value) as max_value
FROM SensorData sd
JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
WHERE sd.device_id = 1
GROUP BY st.sensor_name
ORDER BY st.sensor_name;

-- Show latest 10 readings
PRINT '';
PRINT 'Latest 10 sensor readings:';
PRINT '';

SELECT TOP 10
    sd.data_id,
    sd.device_id,
    st.sensor_name,
    sd.sensor_value,
    st.unit,
    sd.timestamp
FROM SensorData sd
JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
WHERE sd.device_id = 1
ORDER BY sd.timestamp DESC;

PRINT '';
PRINT 'You can now access dashboard: http://localhost:9999/IoTWebApp/';
PRINT '';
