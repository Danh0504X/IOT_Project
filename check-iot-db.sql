-- Check IOT_DB database data
USE IOT_DB;
GO

-- Check all tables
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- Check SensorData count
SELECT 'SensorData' as TableName, COUNT(*) as RecordCount FROM SensorData
UNION ALL
SELECT 'DeviceInfo', COUNT(*) FROM DeviceInfo
UNION ALL
SELECT 'SensorType', COUNT(*) FROM SensorType;

-- Check latest sensor data
SELECT TOP 10 
    sd.data_id,
    sd.device_id,
    st.sensor_name,
    sd.sensor_value,
    st.unit,
    sd.timestamp
FROM SensorData sd
JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
ORDER BY sd.timestamp DESC;

-- If no data, insert test data
IF NOT EXISTS (SELECT 1 FROM SensorData)
BEGIN
    PRINT 'No data found. Inserting test data...';
    
    INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp)
    VALUES 
        (1, 1, 25.5, GETDATE()),
        (1, 2, 60.0, GETDATE()),
        (1, 3, 250.0, GETDATE()),
        (1, 4, 280.0, GETDATE()),
        (1, 5, 550.0, GETDATE()),
        (1, 6, 35.0, GETDATE());
    
    PRINT 'Test data inserted!';
END
ELSE
BEGIN
    PRINT 'Data already exists.';
END
