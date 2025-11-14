-- Quick check IOT_DB data
USE IOT_DB;

-- Check if tables exist and have data
SELECT 'SensorType' as TableName, COUNT(*) as RowCount FROM SensorType
UNION ALL
SELECT 'DeviceInfo', COUNT(*) FROM DeviceInfo
UNION ALL
SELECT 'SensorData', COUNT(*) FROM SensorData;

-- Show SensorType records (needed for JOIN)
SELECT * FROM SensorType;

-- Show latest SensorData
SELECT TOP 5 * FROM SensorData ORDER BY timestamp DESC;
