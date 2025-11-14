#include <Arduino.h>
#include "DHT.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ================================
// PIN CẢM BIẾN
// ================================
#define DUST_LED_PIN 12
#define DUST_VOUT_PIN 35

#define DHTPIN 13
#define DHTTYPE DHT11

#define MQ1_PIN 32   // MQ-135
#define MQ2_PIN 33   // MQ-7
#define MQ3_PIN 34   // MQ-2

#define FAN_PIN 18
#define BUZZER_PIN 19

// ================================
// WiFi
// ================================
const char* WIFI_SSID     = "demo273";
const char* WIFI_PASSWORD = "pass1234";

// ================================
// Server (Servlet /api/data)
// ================================
// KIỂM TRA: Thay đổi IP và port theo server của bạn
// Nếu server chạy ở localhost:9999 → Dùng IP máy tính của bạn
const char* serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data";
const char* deviceId  = "DEVICE001";

unsigned long lastSend = 0;
const unsigned long SEND_INTERVAL = 5000; // 5s

DHT dht(DHTPIN, DHTTYPE);
WiFiClient client;
HTTPClient http;

// ================================
// KẾT NỐI WIFI
// ================================
void connectWiFi() {
  Serial.print("Connecting to WiFi: ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int t = 0;
  while (WiFi.status() != WL_CONNECTED && t < 30) {
    delay(500);
    Serial.print(".");
    t++;
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("✓ WiFi connected");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
    Serial.print("Gateway IP: ");
    Serial.println(WiFi.gatewayIP());
    Serial.print("Subnet Mask: ");
    Serial.println(WiFi.subnetMask());
    Serial.print("RSSI: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("✗ WiFi connect failed");
  }
}

// ================================
// TEST KẾT NỐI SERVER
// ================================
void testServerConnection() {
  Serial.println("\n----- Testing Server Connection -----");
  
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("✗ WiFi not connected");
    return;
  }
  
  // Test ping đến server IP
  IPAddress serverIP(192, 168, 137, 1);
  Serial.print("Pinging server: ");
  Serial.println(serverIP);
  
  // Test HTTP GET đến endpoint
  http.begin(client, "http://192.168.137.1:8080/IoTWebApp/api/data");
  http.setTimeout(5000); // 5 second timeout
  
  Serial.println("Sending GET request...");
  int httpCode = http.GET();
  
  if (httpCode > 0) {
    Serial.print("✓ Server is reachable. HTTP Code: ");
    Serial.println(httpCode);
    Serial.print("Response: ");
    Serial.println(http.getString());
  } else {
    Serial.print("✗ Server connection failed. Error: ");
    Serial.println(httpCode);
    Serial.println("Error codes:");
    Serial.println("  -1: Connection refused / Server not running");
    Serial.println("  -2: Timeout");
    Serial.println("  -3: Invalid response");
    Serial.println("  -4: Out of memory");
  }
  
  http.end();
  Serial.println("--------------------------------------\n");
}

// ================================
// GỬI JSON LÊN SERVER
// ================================
void sendToServer(const String& payload) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("✗ WiFi not connected, skip sending");
    return;
  }

  http.begin(client, serverUrl);
  http.addHeader("Content-Type", "application/json");
  http.setTimeout(5000); // 5 second timeout

  Serial.println("Posting JSON:");
  Serial.println(payload);
  Serial.print("To URL: ");
  Serial.println(serverUrl);

  int code = http.POST(payload);

  if (code > 0) {
    Serial.print("✓ HTTP Response Code: ");
    Serial.println(code);
    Serial.println("Server reply:");
    String response = http.getString();
    Serial.println(response);
    
    if (code == 200) {
      Serial.println("✓ Data saved successfully!");
    }
  } else {
    Serial.print("✗ HTTP Error: ");
    Serial.println(code);
    Serial.println("Possible causes:");
    Serial.println("  1. Server not running on port 8080");
    Serial.println("  2. Wrong IP address (check: 192.168.137.1)");
    Serial.println("  3. Firewall blocking connection");
    Serial.println("  4. Server running on different port (9999?)");
    Serial.println("  5. ESP32 and server not on same network");
    
    // Debug info
    Serial.print("ESP32 IP: ");
    Serial.println(WiFi.localIP());
    Serial.print("Server URL: ");
    Serial.println(serverUrl);
  }

  http.end();
}

// ================================
// ĐỌC CẢM BIẾN + GỬI
// ================================
void readAndSend() {
  Serial.println("\n----- Reading Sensors -----");

  // DHT
  float humidity    = dht.readHumidity();
  float temperature = dht.readTemperature();

  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("✗ Failed to read DHT, dùng giá trị mặc định");
    humidity    = 60.0;
    temperature = 25.0;
  }

  // MQ sensors
  int mq1 = analogRead(MQ1_PIN); // MQ-135
  int mq2 = analogRead(MQ2_PIN); // MQ-7
  int mq3 = analogRead(MQ3_PIN); // MQ-2

  // Dust sensor GP2Y1010
  digitalWrite(DUST_LED_PIN, LOW);
  delayMicroseconds(280);
  int dustRaw = analogRead(DUST_VOUT_PIN);
  delayMicroseconds(40);
  digitalWrite(DUST_LED_PIN, HIGH);
  delayMicroseconds(9680);

  float voltage     = dustRaw * (3.3 / 4095.0);
  float dustDensity = (voltage - 0.9) * 1000 / 0.5; // công thức demo
  if (dustDensity < 0) dustDensity = 0;

  Serial.printf("Temp: %.1f°C | Hum: %.1f%% | MQ135: %d | MQ7: %d | MQ2: %d | Dust: %.1f µg/m³\n",
                temperature, humidity, mq1, mq2, mq3, dustDensity);

  // JSON gửi lên servlet /api/data
  StaticJsonDocument<256> doc;
  doc["deviceId"]    = deviceId;
  doc["temperature"] = temperature;
  doc["humidity"]    = humidity;
  doc["mq135"]       = mq1;         // sensor_type_id = 3
  doc["mq7"]         = mq2;         // sensor_type_id = 4
  doc["mq2"]         = mq3;         // sensor_type_id = 5
  doc["dust"]        = dustDensity; // sensor_type_id = 6

  String jsonStr;
  serializeJson(doc, jsonStr);

  sendToServer(jsonStr);
}

// ================================
// SETUP
// ================================
void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n========================================");
  Serial.println("ESP32 Sensor System START");
  Serial.println("========================================\n");

  pinMode(DUST_LED_PIN, OUTPUT);
  pinMode(DUST_VOUT_PIN, INPUT);

  pinMode(MQ1_PIN, INPUT);
  pinMode(MQ2_PIN, INPUT);
  pinMode(MQ3_PIN, INPUT);

  pinMode(FAN_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  digitalWrite(FAN_PIN, HIGH);
  digitalWrite(BUZZER_PIN, HIGH);

  dht.begin();
  
  connectWiFi();
  
  // Test server connection sau khi kết nối WiFi
  if (WiFi.status() == WL_CONNECTED) {
    delay(1000);
    testServerConnection();
  }
}

// ================================
// LOOP
// ================================
void loop() {
  // Kiểm tra WiFi mỗi lần loop
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected! Reconnecting...");
    connectWiFi();
    delay(2000);
    return;
  }
  
  if (millis() - lastSend >= SEND_INTERVAL) {
    lastSend = millis();
    readAndSend();
  }

  delay(200);
}

