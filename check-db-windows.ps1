# Check SQL Server with Windows Authentication

Write-Host "Checking SQL Server connection..." -ForegroundColor Cyan

try {
    # Try Windows Authentication first
    $connectionString = "Server=localhost;Database=master;Integrated Security=True;TrustServerCertificate=True;"
    $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
    $connection.Open()
    
    Write-Host "✓ Connected with Windows Authentication!" -ForegroundColor Green
    
    $command = $connection.CreateCommand()
    
    # Check if IoTDB exists
    Write-Host "`nChecking if IoTDB database exists..." -ForegroundColor Yellow
    $command.CommandText = "SELECT name FROM sys.databases WHERE name = 'IoTDB'"
    $result = $command.ExecuteScalar()
    
    if ($result) {
        Write-Host "✓ Database IoTDB exists!" -ForegroundColor Green
        
        # Switch to IoTDB
        $connection.Close()
        $connectionString = "Server=localhost;Database=IoTDB;Integrated Security=True;TrustServerCertificate=True;"
        $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
        $connection.Open()
        $command = $connection.CreateCommand()
        
        # Check tables
        Write-Host "`nChecking tables..." -ForegroundColor Yellow
        $command.CommandText = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
        $reader = $command.ExecuteReader()
        $tables = @()
        while ($reader.Read()) {
            $tables += $reader["TABLE_NAME"]
        }
        $reader.Close()
        
        Write-Host "Found $($tables.Count) tables:" -ForegroundColor Green
        $tables | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
        
        # Check SensorData count
        Write-Host "`nChecking SensorData..." -ForegroundColor Yellow
        $command.CommandText = "SELECT COUNT(*) FROM SensorData"
        $count = $command.ExecuteScalar()
        Write-Host "SensorData records: $count" -ForegroundColor $(if($count -gt 0){"Green"}else{"Red"})
        
        if ($count -eq 0) {
            Write-Host "`n⚠ No sensor data found! Inserting test data..." -ForegroundColor Yellow
            
            # Insert test data
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 1, 25.5, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 2, 60.0, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 3, 250.0, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 4, 280.0, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 5, 550.0, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            $command.CommandText = "INSERT INTO SensorData (device_id, sensor_type_id, sensor_value, timestamp) VALUES (1, 6, 35.0, GETDATE())"
            $command.ExecuteNonQuery() | Out-Null
            
            Write-Host "✓ Test data inserted!" -ForegroundColor Green
            
            $command.CommandText = "SELECT COUNT(*) FROM SensorData"
            $newCount = $command.ExecuteScalar()
            Write-Host "New count: $newCount" -ForegroundColor Green
        }
        
        # Show sample data
        Write-Host "`nLatest sensor readings:" -ForegroundColor Yellow
        $command.CommandText = "SELECT TOP 10 sd.device_id, st.sensor_name, sd.sensor_value, st.unit, sd.timestamp FROM SensorData sd JOIN SensorType st ON sd.sensor_type_id = st.sensor_type_id ORDER BY sd.timestamp DESC"
        $reader = $command.ExecuteReader()
        while ($reader.Read()) {
            Write-Host "  Device $($reader['device_id']): $($reader['sensor_name']) = $($reader['sensor_value']) $($reader['unit']) at $($reader['timestamp'])" -ForegroundColor White
        }
        $reader.Close()
        
    }
    else {
        Write-Host "✗ Database IoTDB does NOT exist!" -ForegroundColor Red
        Write-Host "`nPlease run: sqlcmd -S localhost -E -i database-schema.sql" -ForegroundColor Yellow
    }
    
    $connection.Close()
    
    Write-Host "`n✓ Check complete!" -ForegroundColor Green
    
}
catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}
