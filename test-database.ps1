# Test Database Connection and Insert Sample Data
# Run this script to verify SQL Server connection and insert test data

$serverName = "localhost"
$databaseName = "IoTDB"
$username = "sa"
$password = "YourPassword123"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "IoT Database Test Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Test connection
Write-Host "Testing database connection..." -ForegroundColor Yellow
try {
    $connectionString = "Server=$serverName;Database=$databaseName;User Id=$username;Password=$password;TrustServerCertificate=True;"
    $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
    $connection.Open()
    Write-Host "Success! Connected to database." -ForegroundColor Green
    
    # Check if tables exist
    Write-Host "`nChecking database tables..." -ForegroundColor Yellow
    $query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME"
    $command = $connection.CreateCommand()
    $command.CommandText = $query
    $reader = $command.ExecuteReader()
    
    $tables = @()
    while ($reader.Read()) {
        $tables += $reader["TABLE_NAME"]
    }
    $reader.Close()
    
    if ($tables.Count -gt 0) {
        Write-Host "Found $($tables.Count) tables:" -ForegroundColor Green
        $tables | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
    }
    else {
        Write-Host "No tables found! Please run database-schema.sql first." -ForegroundColor Red
        $connection.Close()
        exit
    }
    
    # Check SensorData count
    Write-Host "`nChecking sensor data..." -ForegroundColor Yellow
    $command.CommandText = "SELECT COUNT(*) as cnt FROM SensorData"
    $count = $command.ExecuteScalar()
    Write-Host "Current sensor data records: $count" -ForegroundColor Cyan
    
    if ($count -eq 0) {
        Write-Host "`nInserting test data..." -ForegroundColor Yellow
        
        # Insert temperature
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 1, 25.5, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 2, 60.0, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 3, 250.0, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 4, 280.0, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 5, 550.0, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 6, 35.0, DATEADD(MINUTE, -10, GETDATE()))"
        $command.ExecuteNonQuery() | Out-Null
        
        $command.CommandText = "UPDATE DeviceInfo SET last_seen = GETDATE() WHERE device_id = 1"
        $command.ExecuteNonQuery() | Out-Null
        
        Write-Host "Test data inserted successfully!" -ForegroundColor Green
        
        # Verify
        $command.CommandText = "SELECT COUNT(*) FROM SensorData"
        $newCount = $command.ExecuteScalar()
        Write-Host "Total sensor data records now: $newCount" -ForegroundColor Green
    }
    else {
        Write-Host "Data already exists in database" -ForegroundColor Green
    }
    
    # Show latest data
    Write-Host "`nLatest sensor readings:" -ForegroundColor Yellow
    $selectQuery = "SELECT TOP 10 sd.data_id, sd.device_id, st.sensor_name, sd.sensor_value, st.unit, sd.timestamp FROM SensorData sd JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id WHERE sd.device_id = 1 ORDER BY sd.timestamp DESC"
    
    $command.CommandText = $selectQuery
    $reader = $command.ExecuteReader()
    
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Gray
    Write-Host "DataID    Device    Sensor           Value           Unit       Timestamp" -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Gray
    
    while ($reader.Read()) {
        $dataId = $reader["data_id"]
        $deviceId = $reader["device_id"]
        $sensorName = $reader["sensor_name"]
        $value = $reader["sensor_value"]
        $unit = $reader["unit"]
        $timestamp = $reader["timestamp"]
        
        $line = "{0,-10}{1,-10}{2,-17}{3,-16}{4,-11}{5}" -f $dataId, $deviceId, $sensorName, $value, $unit, $timestamp
        Write-Host $line -ForegroundColor White
    }
    $reader.Close()
    Write-Host "================================================================================" -ForegroundColor Gray
    
    $connection.Close()
    
    Write-Host "`nDatabase is ready! You can now access the dashboard." -ForegroundColor Green
    Write-Host "Dashboard URL: http://localhost:9999/IoTWebApp/" -ForegroundColor Cyan
    
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nPlease check:" -ForegroundColor Yellow
    Write-Host "1. SQL Server is running" -ForegroundColor Yellow
    Write-Host "2. Database credentials are correct (check db.properties)" -ForegroundColor Yellow
    Write-Host "3. Database 'IoTDB' exists (run database-schema.sql)" -ForegroundColor Yellow
}

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
