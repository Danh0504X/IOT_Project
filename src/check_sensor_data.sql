-- Kiểm tra dữ liệu sensor mới nhất từ DEVICE001
SELECT TOP 10 
    id,
    device_id,
    sensor_type_id,
    sensor_value,
    timestamp,
    created_at
FROM SensorData 
WHERE device_id = 'DEVICE001'
ORDER BY timestamp DESC;

-- Kiểm tra số lượng records đã được lưu
SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT sensor_type_id) as sensor_types,
    MIN(timestamp) as first_record,
    MAX(timestamp) as last_record
FROM SensorData 
WHERE device_id = 'DEVICE001';

-- Kiểm tra device info (last_seen đã được update)
SELECT 
    device_id,
    device_name,
    status,
    created_at,
    updated_at
FROM DeviceInfo 
WHERE device_id = 'DEVICE001';



