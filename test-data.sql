-- =============================================
-- Test Data for IoT System
-- Insert sample sensor readings
-- =============================================

USE IoTDB;
GO

-- Insert test sensor readings for Device 1
-- Temperature readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 1, 25.5, DATEADD(MINUTE, -10, GETDATE())),
    (1, 1, 26.0, DATEADD(MINUTE, -9, GETDATE())),
    (1, 1, 25.8, DATEADD(MINUTE, -8, GETDATE())),
    (1, 1, 26.2, DATEADD(MINUTE, -7, GETDATE())),
    (1, 1, 25.9, DATEADD(MINUTE, -6, GETDATE()));

-- Humidity readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 2, 60.0, DATEADD(MINUTE, -10, GETDATE())),
    (1, 2, 61.5, DATEADD(MINUTE, -9, GETDATE())),
    (1, 2, 60.8, DATEADD(MINUTE, -8, GETDATE())),
    (1, 2, 62.0, DATEADD(MINUTE, -7, GETDATE())),
    (1, 2, 61.2, DATEADD(MINUTE, -6, GETDATE()));

-- MQ1 readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 3, 250.0, DATEADD(MINUTE, -10, GETDATE())),
    (1, 3, 255.0, DATEADD(MINUTE, -9, GETDATE())),
    (1, 3, 248.0, DATEADD(MINUTE, -8, GETDATE())),
    (1, 3, 260.0, DATEADD(MINUTE, -7, GETDATE())),
    (1, 3, 252.0, DATEADD(MINUTE, -6, GETDATE()));

-- MQ2 readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 4, 280.0, DATEADD(MINUTE, -10, GETDATE())),
    (1, 4, 285.0, DATEADD(MINUTE, -9, GETDATE())),
    (1, 4, 278.0, DATEADD(MINUTE, -8, GETDATE())),
    (1, 4, 290.0, DATEADD(MINUTE, -7, GETDATE())),
    (1, 4, 282.0, DATEADD(MINUTE, -6, GETDATE()));

-- MQ3 readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 5, 550.0, DATEADD(MINUTE, -10, GETDATE())),
    (1, 5, 555.0, DATEADD(MINUTE, -9, GETDATE())),
    (1, 5, 548.0, DATEADD(MINUTE, -8, GETDATE())),
    (1, 5, 560.0, DATEADD(MINUTE, -7, GETDATE())),
    (1, 5, 552.0, DATEADD(MINUTE, -6, GETDATE()));

-- Dust readings
INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
VALUES 
    (1, 6, 35.0, DATEADD(MINUTE, -10, GETDATE())),
    (1, 6, 38.0, DATEADD(MINUTE, -9, GETDATE())),
    (1, 6, 36.0, DATEADD(MINUTE, -8, GETDATE())),
    (1, 6, 40.0, DATEADD(MINUTE, -7, GETDATE())),
    (1, 6, 37.0, DATEADD(MINUTE, -6, GETDATE()));

-- Update device last_seen
UPDATE DeviceInfo 
SET last_seen = GETDATE()
WHERE device_id = 1;

GO

-- Verify data
SELECT 
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

GO
