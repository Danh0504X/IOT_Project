/*
 * ESP32 IoT Sensor Data Sender
 * 
 * Hardware:
 * - ESP32 Development Board
 * - DHT22 Temperature & Humidity Sensor
 * - MQ-135, MQ-7, MQ-2 Gas Sensors
 * - GP2Y1010AU0F Dust Sensor
 * 
 * Connect to WiFi and send sensor readings to Java web server
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <DHT.h>
#include <ArduinoJson.h>

// ============================================
// WiFi Configuration
// ============================================
const char* ssid = "YOUR_WIFI_SSID";           // Thay bằng tên WiFi của bạn
const char* password = "YOUR_WIFI_PASSWORD";   // Thay bằng mật khẩu WiFi

// ============================================
// Server Configuration
// ============================================
// Thay YOUR_COMPUTER_IP bằng IP máy tính chạy Tomcat
// Kiểm tra bằng lệnh: ipconfig (Windows) hoặc ifconfig (Linux/Mac)
const char* serverUrl = "http://192.168.1.10:9999/IoTWebApp/api/data";

// Device ID - unique identifier for this ESP32
const char* deviceId = "DEVICE001";

// ============================================
// Sensor Configuration
// ============================================
#define DHTPIN 4          // DHT22 connected to GPIO 4
#define DHTTYPE DHT22     // DHT 22 (AM2302)

#define MQ135_PIN 34      // MQ-135 analog pin
#define MQ7_PIN 35        // MQ-7 analog pin
#define MQ2_PIN 32        // MQ-2 analog pin
#define DUST_PIN 33       // Dust sensor analog pin

DHT dht(DHTPIN, DHTTYPE);

// ============================================
// Timing Configuration
// ============================================
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 15000; // Send every 15 seconds

// ============================================
// Setup Function
// ============================================
void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("\n\n=================================");
  Serial.println("ESP32 IoT Sensor System");
  Serial.println("=================================");
  
  // Initialize sensors
  dht.begin();
  
  // Connect to WiFi
  connectWiFi();
}

// ============================================
// Main Loop
// ============================================
void loop() {
  // Check WiFi connection
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected! Reconnecting...");
    connectWiFi();
  }
  
  // Send data at specified interval
  unsigned long currentTime = millis();
  if (currentTime - lastSendTime >= sendInterval) {
    lastSendTime = currentTime;
    
    // Read sensors and send data
    readAndSendData();
  }
  
  delay(100);
}

// ============================================
// Connect to WiFi
// ============================================
void connectWiFi() {
  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✓ WiFi Connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    Serial.print("Signal Strength: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("\n✗ WiFi Connection Failed!");
    Serial.println("Please check SSID and password");
  }
}

// ============================================
// Read Sensors and Send Data
// ============================================
void readAndSendData() {
  Serial.println("\n----- Reading Sensors -----");
  
  // Read DHT22 (Temperature & Humidity)
  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();
  
  // Read Gas Sensors (convert analog to ppm - simplified)
  float mq135 = analogRead(MQ135_PIN) * (500.0 / 4095.0); // 0-500 ppm range
  float mq7 = analogRead(MQ7_PIN) * (500.0 / 4095.0);
  float mq2 = analogRead(MQ2_PIN) * (1000.0 / 4095.0);    // 0-1000 ppm range
  
  // Read Dust Sensor (convert analog to µg/m³ - simplified)
  float dust = analogRead(DUST_PIN) * (100.0 / 4095.0);   // 0-100 µg/m³
  
  // Check if DHT reading failed
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("✗ Failed to read from DHT sensor!");
    temperature = 25.0; // Default value
    humidity = 60.0;
  }
  
  // Print readings
  Serial.printf("Temperature: %.2f °C\n", temperature);
  Serial.printf("Humidity: %.2f %%\n", humidity);
  Serial.printf("MQ-135 (Air Quality): %.2f ppm\n", mq135);
  Serial.printf("MQ-7 (CO): %.2f ppm\n", mq7);
  Serial.printf("MQ-2 (Smoke): %.2f ppm\n", mq2);
  Serial.printf("Dust (PM2.5): %.2f µg/m³\n", dust);
  
  // Create JSON payload
  StaticJsonDocument<256> jsonDoc;
  jsonDoc["deviceId"] = deviceId;
  jsonDoc["temperature"] = round(temperature * 10) / 10.0;
  jsonDoc["humidity"] = round(humidity * 10) / 10.0;
  jsonDoc["mq135"] = round(mq135 * 10) / 10.0;
  jsonDoc["mq7"] = round(mq7 * 10) / 10.0;
  jsonDoc["mq2"] = round(mq2 * 10) / 10.0;
  jsonDoc["dust"] = round(dust * 10) / 10.0;
  
  String jsonString;
  serializeJson(jsonDoc, jsonString);
  
  Serial.println("\n----- Sending to Server -----");
  Serial.print("URL: ");
  Serial.println(serverUrl);
  Serial.print("JSON: ");
  Serial.println(jsonString);
  
  // Send HTTP POST request
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(jsonString);
    
    if (httpResponseCode > 0) {
      Serial.print("✓ HTTP Response Code: ");
      Serial.println(httpResponseCode);
      
      String response = http.getString();
      Serial.print("Server Response: ");
      Serial.println(response);
    } else {
      Serial.print("✗ HTTP Error Code: ");
      Serial.println(httpResponseCode);
      Serial.println("Possible issues:");
      Serial.println("  - Check server IP address");
      Serial.println("  - Check if Tomcat is running on port 9999");
      Serial.println("  - Check firewall settings");
    }
    
    http.end();
  } else {
    Serial.println("✗ WiFi not connected!");
  }
  
  Serial.println("---------------------------\n");
}

// ============================================
// Notes:
// ============================================
/*
 * IMPORTANT SETUP STEPS:
 * 
 * 1. Install Required Libraries (Arduino IDE):
 *    - Tools → Manage Libraries
 *    - Search and install:
 *      * "DHT sensor library" by Adafruit
 *      * "ArduinoJson" by Benoit Blanchon
 *      * WiFi (built-in)
 *      * HTTPClient (built-in)
 * 
 * 2. Configure WiFi:
 *    - Replace "YOUR_WIFI_SSID" with your WiFi name
 *    - Replace "YOUR_WIFI_PASSWORD" with your WiFi password
 * 
 * 3. Find Your Computer's IP Address:
 *    Windows: Open CMD → type "ipconfig" → find "IPv4 Address"
 *    Example: 192.168.1.10
 * 
 * 4. Update Server URL:
 *    - Replace 192.168.1.10 with your computer's IP
 *    - Keep the port :9999 and path /IoTWebApp/api/data
 * 
 * 5. Hardware Connections:
 *    DHT22:
 *      - VCC → 3.3V
 *      - GND → GND
 *      - DATA → GPIO 4
 *    
 *    MQ Sensors (MQ-135, MQ-7, MQ-2):
 *      - VCC → 5V (or 3.3V depending on module)
 *      - GND → GND
 *      - AOUT → Analog pins (GPIO 34, 35, 32)
 *    
 *    Dust Sensor:
 *      - VCC → 5V
 *      - GND → GND
 *      - VOUT → GPIO 33
 * 
 * 6. Upload Code:
 *    - Connect ESP32 via USB
 *    - Select Board: Tools → Board → ESP32 Dev Module
 *    - Select Port: Tools → Port → COMx
 *    - Click Upload
 * 
 * 7. Monitor Serial Output:
 *    - Tools → Serial Monitor
 *    - Set baud rate to 115200
 *    - Watch for connection status and data transmission
 * 
 * 8. Troubleshooting:
 *    - If "HTTP Error Code: -1" → Check server IP and firewall
 *    - If "WiFi not connected" → Check WiFi credentials
 *    - If sensor readings are 0 → Check sensor connections
 */
