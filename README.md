# IoT Web Application - SQL Server Integration

Complete Java Maven MVC Web Application (Servlet + JSP + JDBC) integrated with SQL Server database for IoT sensor data management.

## 📋 Project Overview

This application provides a complete backend and frontend for managing IoT devices (ESP32) with real-time sensor data collection, threshold management, and device command functionality.

### Key Features
- ✅ **Pure JDBC** implementation (no JPA/Hibernate)
- ✅ **SQL Server** database integration
- ✅ **ESP32 Compatible** REST API
- ✅ **Real-time** sensor data collection
- ✅ **Dynamic threshold** management
- ✅ **Device command** system
- ✅ **Web dashboard** for monitoring

---

## 🗄️ Database Schema

### Tables

#### 1. **DeviceInfo**
Stores IoT device information.
```sql
- device_id (PK, INT, IDENTITY)
- device_name (NVARCHAR(100))
- device_type (NVARCHAR(50))
- mac_address (NVARCHAR(20), UNIQUE)
- ip_address (NVARCHAR(50))
- location (NVARCHAR(200))
- status (NVARCHAR(20): ACTIVE/INACTIVE/MAINTENANCE)
- registered_at (DATETIME)
- last_seen (DATETIME)
```

#### 2. **SensorType**
Defines available sensor types.
```sql
- sensor_type_id (PK, INT, IDENTITY)
- sensor_name (NVARCHAR(50), UNIQUE)
- unit (NVARCHAR(20))
- description (NVARCHAR(255))
```

**Default Sensor Types:**
- `temperature` (°C)
- `humidity` (%)
- `mq1` (ppm) - MQ-135 Air Quality
- `mq2` (ppm) - MQ-7 Carbon Monoxide
- `mq3` (ppm) - MQ-2 Smoke/Gas
- `dust` (µg/m³) - PM2.5

#### 3. **DeviceSensor**
Maps sensors to devices.
```sql
- device_sensor_id (PK, INT, IDENTITY)
- device_id (FK → DeviceInfo)
- sensor_type_id (FK → SensorType)
- sensor_pin (NVARCHAR(10))
- status (NVARCHAR(20): ACTIVE/INACTIVE/ERROR)
```

#### 4. **SensorData**
Stores individual sensor readings.
```sql
- data_id (PK, BIGINT, IDENTITY)
- device_id (FK → DeviceInfo)
- sensor_type_id (FK → SensorType)
- sensor_value (FLOAT)
- timestamp (DATETIME, DEFAULT GETDATE())
```

#### 5. **SensorSettings**
Threshold values for alerts.
```sql
- setting_id (PK, INT, IDENTITY)
- device_id (FK → DeviceInfo)
- sensor_type_id (FK → SensorType)
- threshold_value (FLOAT)
- alert_condition (NVARCHAR(20): GREATER_THAN/LESS_THAN/EQUALS/NOT_EQUALS)
- updated_at (DATETIME)
```

#### 6. **DeviceCommand**
Commands to send to devices.
```sql
- command_id (PK, INT, IDENTITY)
- device_id (FK → DeviceInfo)
- command_type (NVARCHAR(50))
- command_value (NVARCHAR(255))
- status (NVARCHAR(20): PENDING/EXECUTED/FAILED/CANCELLED)
- created_at (DATETIME)
- executed_at (DATETIME)
```

#### 7. **SensorDataAggregated**
Aggregated data for analytics.
```sql
- agg_id (PK, BIGINT, IDENTITY)
- device_id (FK → DeviceInfo)
- sensor_type_id (FK → SensorType)
- aggregation_type (NVARCHAR(10): HOURLY/DAILY/WEEKLY/MONTHLY)
- avg_value, min_value, max_value (FLOAT)
- record_count (INT)
- period_start, period_end (DATETIME)
```

---

## 🏗️ Application Architecture

### Project Structure
```
src/main/java/com/mycompany/iotwebapp/
├── controller/          # Servlets
│   ├── DataServlet.java           # POST /api/data
│   ├── PollServlet.java           # GET /api/poll
│   ├── ThresholdAdminServlet.java # /admin/thresholds
│   └── DashboardServlet.java      # /dashboard
├── dao/                 # Data Access Objects
│   ├── DBConnection.java
│   ├── DeviceInfoDAO.java
│   ├── SensorTypeDAO.java
│   ├── DeviceSensorDAO.java
│   ├── SensorDataDAO.java
│   ├── SensorSettingsDAO.java
│   └── DeviceCommandDAO.java
├── model/               # POJOs
│   ├── DeviceInfo.java
│   ├── SensorType.java
│   ├── DeviceSensor.java
│   ├── SensorData.java
│   ├── SensorSettings.java
│   ├── DeviceCommand.java
│   ├── SensorDataAggregated.java
│   └── ESP32SensorPayload.java
└── service/             # Business Logic
    ├── SensorService.java
    └── ThresholdService.java

src/main/webapp/
├── WEB-INF/
│   ├── views/
│   │   ├── dashboard.jsp
│   │   └── thresholds.jsp
│   └── web.xml
└── index.jsp
```

---

## 🔌 API Endpoints

### 1. **POST /api/data**
Receive sensor data from ESP32.

**Request (JSON):**
```json
{
  "deviceId": 1,
  "temperature": 25.5,
  "humidity": 60.0,
  "mq1": 250.0,
  "mq2": 280.0,
  "mq3": 550.0,
  "dust": 35.0,
  "wifiSignal": -65,
  "uptime": 123456
}
```

**Response:**
```json
{
  "status": "ok",
  "received": true
}
```

### 2. **GET /api/poll?deviceId=1**
ESP32 polls for threshold updates and commands.

**Response:**
```json
{
  "update": true,
  "settings": {
    "mq1": 300.0,
    "mq2": 300.0,
    "mq3": 600.0,
    "temperature": 30.0,
    "dust": 50.0
  },
  "commands": [
    {
      "command_type": "REBOOT",
      "command_value": "now",
      "command_id": 123
    }
  ]
}
```

### 3. **POST /api/poll?commandId=123**
Mark command as executed.

**Response:**
```json
{
  "status": "ok",
  "message": "Command marked as executed"
}
```

### 4. **GET /admin/thresholds?deviceId=1**
Display threshold management UI (JSP).

### 5. **POST /admin/thresholds**
Update thresholds from admin form.

### 6. **GET / or GET /dashboard?deviceId=1**
Display main dashboard with sensor data.

---

## ⚙️ Configuration

### 1. Database Configuration
Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=IoTDB;encrypt=true;trustServerCertificate=true
db.username=sa
db.password=YourPassword123
db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### 2. Database Setup
Run the SQL schema file:
```bash
sqlcmd -S localhost -U sa -P YourPassword123 -i database-schema.sql
```

Or use SQL Server Management Studio to execute `database-schema.sql`.

---

## 🚀 Build & Deploy

### Prerequisites
- Java 11+
- Maven 3.6+
- SQL Server 2019+ (or Azure SQL Database)
- Apache Tomcat 10+

### Build
```bash
mvn clean package
```

### Deploy
Copy `target/IoTWebApp-1.0-SNAPSHOT.war` to Tomcat `webapps/` folder.

### Test Endpoints

**Test POST /api/data:**
```bash
curl -X POST http://localhost:8080/IoTWebApp/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": 1,
    "temperature": 25.5,
    "humidity": 60.0,
    "mq1": 250.0,
    "mq2": 280.0,
    "mq3": 550.0,
    "dust": 35.0
  }'
```

**Test GET /api/poll:**
```bash
curl http://localhost:8080/IoTWebApp/api/poll?deviceId=1
```

---

## 📊 DAO Methods Overview

### DeviceInfoDAO
- `findById(Integer deviceId)`
- `findAll()`
- `findByStatus(String status)`
- `insert(DeviceInfo device)`
- `update(DeviceInfo device)`
- `updateLastSeen(Integer deviceId)`
- `delete(Integer deviceId)`

### SensorDataDAO
- `insert(SensorData data)`
- `insertBatch(List<SensorData> dataList)`
- `findLatestByDevice(Integer deviceId, int limit)`
- `findLatestBySensorType(Integer deviceId, Integer sensorTypeId)`
- `findByTimeRange(Integer deviceId, LocalDateTime start, LocalDateTime end)`
- `countByDevice(Integer deviceId)`
- `deleteOlderThan(LocalDateTime cutoffDate)`

### SensorSettingsDAO
- `findById(Integer settingId)`
- `findByDeviceId(Integer deviceId)`
- `findByDeviceAndSensorType(Integer deviceId, Integer sensorTypeId)`
- `getThresholdMapForDevice(Integer deviceId)`
- `insert(SensorSettings setting)`
- `update(SensorSettings setting)`
- `updateThreshold(Integer deviceId, Integer sensorTypeId, Double threshold)`
- `upsert(SensorSettings setting)`

### DeviceCommandDAO
- `findById(Integer commandId)`
- `findByDeviceId(Integer deviceId)`
- `findPendingByDeviceId(Integer deviceId)`
- `insert(DeviceCommand command)`
- `markAsExecuted(Integer commandId)`
- `updateStatus(Integer commandId, String status)`

---

## 🔐 Security Considerations

1. **SQL Injection Prevention**: All DAOs use `PreparedStatement`
2. **Connection Management**: Try-with-resources for auto-closing
3. **Error Handling**: Comprehensive exception handling
4. **Input Validation**: Servlets validate all input parameters

---

## 📝 Default Data

The database schema includes default test data:
- **Device ID 1**: ESP32-IoT-Device-01
- **All 6 sensor types** pre-configured
- **Default thresholds** set for device 1

---

## 🛠️ Troubleshooting

### Connection Issues
1. Verify SQL Server is running
2. Check firewall allows port 1433
3. Verify credentials in `db.properties`
4. Enable TCP/IP in SQL Server Configuration Manager

### Build Issues
```bash
mvn clean install -U
```

### Check Logs
- Tomcat logs: `logs/catalina.out`
- Application exceptions logged to console

---

## 📄 License

This project is provided as-is for IoT development purposes.

---

## ✅ Completed Features

- ✅ Full SQL Server database schema with relationships
- ✅ Complete JDBC DAO layer (7 DAOs)
- ✅ ESP32-compatible REST API
- ✅ Service layer with business logic
- ✅ Servlets for all endpoints
- ✅ JSP pages with modern UI
- ✅ Default test data included
- ✅ Comprehensive error handling
- ✅ Production-ready code

**Status**: FULLY INTEGRATED AND READY FOR DEPLOYMENT
