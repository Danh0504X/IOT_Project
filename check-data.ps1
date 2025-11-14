# Quick Database Data Check Script

$serverName = "localhost"
$databaseName = "IoTDB"
$username = "sa"
$password = "YourPassword123"

Write-Host "Checking IoT Database..." -ForegroundColor Cyan

try {
    $connectionString = "Server=$serverName;Database=$databaseName;User Id=$username;Password=$password;TrustServerCertificate=True;"
    $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
    $connection.Open()
    
    $command = $connection.CreateCommand()
    
    # Check SensorData count
    Write-Host "`n1. Total SensorData records:" -ForegroundColor Yellow
    $command.CommandText = "SELECT COUNT(*) FROM SensorData"
    $count = $command.ExecuteScalar()
    Write-Host "   Count: $count" -ForegroundColor $(if($count -gt 0){"Green"}else{"Red"})
    
    # Check DeviceInfo
    Write-Host "`n2. Devices:" -ForegroundColor Yellow
    $command.CommandText = "SELECT device_id, device_name, status, last_seen FROM DeviceInfo"
    $reader = $command.ExecuteReader()
    while ($reader.Read()) {
        Write-Host "   Device $($reader['device_id']): $($reader['device_name']) - $($reader['status']) - Last seen: $($reader['last_seen'])" -ForegroundColor White
    }
    $reader.Close()
    
    # Check SensorType
    Write-Host "`n3. Sensor Types:" -ForegroundColor Yellow
    $command.CommandText = "SELECT sensor_type_id, sensor_name, unit FROM SensorType"
    $reader = $command.ExecuteReader()
    while ($reader.Read()) {
        Write-Host "   ID $($reader['sensor_type_id']): $($reader['sensor_name']) ($($reader['unit']))" -ForegroundColor White
    }
    $reader.Close()
    
    # Check latest sensor data grouped by timestamp
    Write-Host "`n4. Latest sensor data (by timestamp):" -ForegroundColor Yellow
    $command.CommandText = @"
SELECT TOP 5
    sd.timestamp,
    sd.device_id,
    COUNT(*) as sensor_count
FROM SensorData sd
GROUP BY sd.timestamp, sd.device_id
ORDER BY sd.timestamp DESC
"@
    $reader = $command.ExecuteReader()
    while ($reader.Read()) {
        Write-Host "   Timestamp: $($reader['timestamp']) - Device: $($reader['device_id']) - Sensors: $($reader['sensor_count'])" -ForegroundColor White
    }
    $reader.Close()
    
    # Show actual latest readings
    Write-Host "`n5. Latest 10 sensor readings:" -ForegroundColor Yellow
    $command.CommandText = @"
SELECT TOP 10
    sd.data_id,
    sd.device_id,
    st.sensor_name,
    sd.sensor_value,
    st.unit,
    sd.timestamp
FROM SensorData sd
JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id
ORDER BY sd.timestamp DESC
"@
    $reader = $command.ExecuteReader()
    Write-Host "   DataID | Device | Sensor       | Value  | Unit | Timestamp" -ForegroundColor Cyan
    Write-Host "   $("-" * 70)" -ForegroundColor Gray
    while ($reader.Read()) {
        Write-Host ("   {0,-7}| {1,-7}| {2,-13}| {3,-7}| {4,-5}| {5}" -f `
            $reader['data_id'], `
            $reader['device_id'], `
            $reader['sensor_name'], `
            $reader['sensor_value'], `
            $reader['unit'], `
            $reader['timestamp']) -ForegroundColor White
    }
    $reader.Close()
    
    $connection.Close()
    
    Write-Host "`nDatabase check complete!" -ForegroundColor Green
    
    if ($count -eq 0) {
        Write-Host "`nWARNING: No sensor data found!" -ForegroundColor Red
        Write-Host "Run test-data.sql or use POST /api/data to insert data" -ForegroundColor Yellow
    }
    
}
catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nPossible issues:" -ForegroundColor Yellow
    Write-Host "- SQL Server not running" -ForegroundColor Yellow
    Write-Host "- Wrong credentials in db.properties" -ForegroundColor Yellow
    Write-Host "- Database not created (run database-schema.sql)" -ForegroundColor Yellow
}
