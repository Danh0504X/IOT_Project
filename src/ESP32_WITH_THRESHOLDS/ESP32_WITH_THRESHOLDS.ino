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
// Server URLs
// ================================
const char* serverUrl = "http://192.168.137.1:8080/IoTWebApp/api/data";
const char* pollUrl   = "http://192.168.137.1:8080/IoTWebApp/api/poll";
const char* deviceId  = "DEVICE001";

unsigned long lastSend = 0;
unsigned long lastPoll = 0;
const unsigned long SEND_INTERVAL = 5000;  // 5s - gửi dữ liệu
const unsigned long POLL_INTERVAL = 10000; // 10s - lấy threshold settings
bool thresholdsFetchedOnce = false; // Flag để track nếu đã fetch ít nhất 1 lần

DHT dht(DHTPIN, DHTTYPE);
WiFiClient client;
HTTPClient http;

// ================================
// THRESHOLD SETTINGS (từ server)
// ================================
struct ThresholdSettings {
  float temperature = 30.0;
  float humidity = 70.0;
  float mq1 = 300.0;      // MQ-135
  float mq2 = 300.0;      // MQ-7
  float mq3 = 600.0;      // MQ-2
  float dust = 50.0;      // Dust Density
  bool loaded = false;    // Đã load từ server chưa
};

ThresholdSettings thresholds;

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
  } else {
    Serial.println("✗ WiFi connect failed");
  }
}

// ================================
// LẤY THRESHOLD SETTINGS TỪ SERVER
// ================================
void fetchThresholds() {
  Serial.println("\n[fetchThresholds] Function called");
  
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[fetchThresholds] ✗ WiFi not connected, skip polling thresholds");
    Serial.printf("  WiFi.status() = %d (need %d)\n", WiFi.status(), WL_CONNECTED);
    thresholds.loaded = false;
    return;
  }

  String url = String(pollUrl) + "?deviceId=" + String(deviceId);
  Serial.println("\n========================================");
  Serial.println("Fetching thresholds from server:");
  Serial.println("  URL: " + url);
  
  http.begin(client, url);
  http.setTimeout(5000); // 5 seconds timeout

  int code = http.GET();
  Serial.printf("  HTTP Response Code: %d\n", code);

  if (code > 0) {
    if (code == HTTP_CODE_OK) {
      String response = http.getString();
      Serial.println("  Server response: " + response);

      // Parse JSON response
      StaticJsonDocument<512> doc;
      DeserializationError error = deserializeJson(doc, response);

      if (error) {
        Serial.print("  ✗ JSON parse error: ");
        Serial.println(error.c_str());
        Serial.printf("  Error details: %s\n", error.c_str());
        thresholds.loaded = false;
      } else {
        Serial.println("  ✓ JSON parsed successfully");
        
        // Debug: Print JSON structure
        if (doc.containsKey("update")) {
          Serial.printf("  Update flag: %s\n", doc["update"].as<bool>() ? "true" : "false");
        } else {
          Serial.println("  ⚠ No 'update' field in response");
        }
        
        if (doc.containsKey("update") && doc["update"] == true) {
          if (doc.containsKey("settings")) {
            JsonObject settings = doc["settings"];
            Serial.println("  ✓ Settings object found");
            
            // Check if settings is empty
            int settingsSize = settings.size();
            Serial.printf("  Settings object size: %d keys\n", settingsSize);
            
            if (settingsSize == 0) {
              Serial.println("  ⚠ WARNING: Settings object is EMPTY! No thresholds configured in database.");
              Serial.println("  → Solution: Please set thresholds on web dashboard at /admin/thresholds?deviceId=DEVICE001");
            } else {
              // Debug: Print all keys in settings
              Serial.print("  Settings keys: ");
              for (JsonPair kv : settings) {
                Serial.print(kv.key().c_str());
                Serial.print("=");
                Serial.print(kv.value().as<float>());
                Serial.print(" ");
              }
              Serial.println();
            }
            
            // Đọc threshold values (Backend trả về: mq1, mq2, mq3, temperature, dust)
            // ESP32 mapping: MQ1_PIN = mq1, MQ2_PIN = mq2, MQ3_PIN = mq3
            bool hasAnyThreshold = false;
            
            if (settings.containsKey("temperature")) {
              thresholds.temperature = settings["temperature"].as<float>();
              Serial.printf("  ✓ Temperature threshold: %.1f°C\n", thresholds.temperature);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'temperature' in settings (using default 30.0°C)");
            }
            
            if (settings.containsKey("humidity")) {
              thresholds.humidity = settings["humidity"].as<float>();
              Serial.printf("  ✓ Humidity threshold: %.1f%%\n", thresholds.humidity);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'humidity' in settings (using default 70.0%)");
            }
            
            // Backend trả về "mq1" cho MQ-135 (MQ1_PIN)
            if (settings.containsKey("mq1")) {
              thresholds.mq1 = settings["mq1"].as<float>();
              Serial.printf("  ✓ MQ1 (MQ-135) threshold: %.1f\n", thresholds.mq1);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'mq1' in settings (using default 300.0)");
            }
            
            // Backend trả về "mq2" cho MQ-7 (MQ2_PIN)
            if (settings.containsKey("mq2")) {
              thresholds.mq2 = settings["mq2"].as<float>();
              Serial.printf("  ✓ MQ2 (MQ-7) threshold: %.1f\n", thresholds.mq2);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'mq2' in settings (using default 300.0)");
            }
            
            // Backend trả về "mq3" cho MQ-2 (MQ3_PIN)
            if (settings.containsKey("mq3")) {
              thresholds.mq3 = settings["mq3"].as<float>();
              Serial.printf("  ✓ MQ3 (MQ-2) threshold: %.1f\n", thresholds.mq3);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'mq3' in settings (using default 600.0)");
            }
            
            if (settings.containsKey("dust")) {
              thresholds.dust = settings["dust"].as<float>();
              Serial.printf("  ✓ Dust threshold: %.1f µg/m³\n", thresholds.dust);
              hasAnyThreshold = true;
            } else {
              Serial.println("  ⚠ No 'dust' in settings (using default 50.0)");
            }
            
            if (hasAnyThreshold) {
              thresholds.loaded = true;
              thresholdsFetchedOnce = true; // Mark as fetched at least once
              Serial.println("\n  ✅ Thresholds loaded successfully:");
              Serial.printf("     Temperature: %.1f°C\n", thresholds.temperature);
              Serial.printf("     Humidity: %.1f%%\n", thresholds.humidity);
              Serial.printf("     MQ1: %.1f\n", thresholds.mq1);
              Serial.printf("     MQ2: %.1f\n", thresholds.mq2);
              Serial.printf("     MQ3: %.1f\n", thresholds.mq3);
              Serial.printf("     Dust: %.1f µg/m³\n", thresholds.dust);
            } else {
              Serial.println("\n  ✗ No threshold values found in settings object");
              Serial.println("  ⚠ This means no thresholds are configured in database for device: " + String(deviceId));
              Serial.println("  → ACTION REQUIRED: Please go to web dashboard and set thresholds at:");
              Serial.println("     http://192.168.137.1:8080/IoTWebApp/admin/thresholds?deviceId=" + String(deviceId));
              thresholds.loaded = false;
              // Even if no thresholds, mark as fetched to avoid repeated attempts
              if (!thresholdsFetchedOnce) {
                thresholdsFetchedOnce = true;
                Serial.println("  ⚠ Marking as fetched (even though empty) to avoid repeated attempts");
                Serial.println("     Will retry on next poll interval (10s)");
              }
            }
          } else {
            Serial.println("  ✗ No 'settings' field in response JSON");
            Serial.println("  ⚠ Server response format is incorrect");
            Serial.println("  → Expected: {\"update\":true,\"settings\":{...},\"commands\":[]}");
            Serial.println("  → Received: " + response);
            thresholds.loaded = false;
            // Mark as fetched even if empty to avoid repeated attempts
            if (!thresholdsFetchedOnce) {
              thresholdsFetchedOnce = true;
              Serial.println("  ⚠ Marking as fetched (even though no settings) to avoid repeated attempts");
            }
          }
        } else {
          Serial.println("  ✗ Update flag is false or missing");
          thresholds.loaded = false;
          // Mark as fetched even if update=false
          if (!thresholdsFetchedOnce) {
            thresholdsFetchedOnce = true;
            Serial.println("  ⚠ Marking as fetched (even though update=false) to avoid repeated attempts");
          }
        }
      }
    } else {
      Serial.printf("  ✗ HTTP Error: %d\n", code);
      Serial.println("  Error: " + http.errorToString(code));
      thresholds.loaded = false;
      // Mark as attempted (even if failed) to avoid too many retries
      if (!thresholdsFetchedOnce) {
        thresholdsFetchedOnce = true;
        Serial.println("  ⚠ Marking as attempted (even though failed) - will retry on next poll interval");
      }
    }
  } else {
    Serial.printf("  ✗ HTTP Request failed: %d\n", code);
    Serial.println("  Error: " + http.errorToString(code));
    Serial.println("  Check server URL and network connection");
    thresholds.loaded = false;
    // Mark as attempted (even if failed) to avoid too many retries
    if (!thresholdsFetchedOnce) {
      thresholdsFetchedOnce = true;
      Serial.println("  ⚠ Marking as attempted (even though failed) - will retry on next poll interval");
    }
  }

  http.end();
  Serial.println("========================================\n");
}

// ================================
// KIỂM TRA VÀ KÍCH HOẠT CẢNH BÁO
// ================================
void checkThresholds(float temperature, float humidity, int mq1, int mq2, int mq3, float dust) {
  // Hiển thị thông tin debug
  Serial.println("\n--- Checking Thresholds ---");
  Serial.printf("Thresholds loaded: %s\n", thresholds.loaded ? "YES" : "NO");
  
  if (!thresholds.loaded) {
    Serial.println("⚠ WARNING: Thresholds not loaded from server yet!");
    Serial.printf("  thresholdsFetchedOnce = %s\n", thresholdsFetchedOnce ? "YES" : "NO");
    Serial.println("  Using default thresholds or will skip alert check");
    if (!thresholdsFetchedOnce) {
      Serial.println("  ⚠ fetchThresholds() has not been called yet - will fetch after next successful data send");
    } else {
      Serial.println("  ⚠ fetchThresholds() was called but failed or returned empty settings");
      Serial.println("  Will retry on next poll interval (10s)");
    }
    return;
  }

  // Hiển thị current thresholds
  Serial.printf("Current Thresholds: Temp=%.1f, Hum=%.1f, MQ1=%.1f, MQ2=%.1f, MQ3=%.1f, Dust=%.1f\n",
                thresholds.temperature, thresholds.humidity, 
                thresholds.mq1, thresholds.mq2, thresholds.mq3, thresholds.dust);
  Serial.printf("Current Values: Temp=%.1f, Hum=%.1f, MQ1=%d, MQ2=%d, MQ3=%d, Dust=%.1f\n",
                temperature, humidity, mq1, mq2, mq3, dust);

  bool alert = false;
  String alertMsg = "🚨 ALERT: ";

  // Kiểm tra từng sensor
  if (temperature > thresholds.temperature) {
    alert = true;
    alertMsg += "Temp HIGH (" + String(temperature, 1) + ">" + String(thresholds.temperature, 1) + "°C) ";
    Serial.printf("  ⚠ Temp EXCEEDED: %.1f > %.1f\n", temperature, thresholds.temperature);
  } else {
    Serial.printf("  ✓ Temp OK: %.1f <= %.1f\n", temperature, thresholds.temperature);
  }
  
  if (humidity > thresholds.humidity) {
    alert = true;
    alertMsg += "Hum HIGH (" + String(humidity, 1) + ">" + String(thresholds.humidity, 1) + "%) ";
    Serial.printf("  ⚠ Humidity EXCEEDED: %.1f > %.1f\n", humidity, thresholds.humidity);
  } else {
    Serial.printf("  ✓ Humidity OK: %.1f <= %.1f\n", humidity, thresholds.humidity);
  }
  
  if (mq1 > thresholds.mq1) {
    alert = true;
    alertMsg += "MQ1 HIGH (" + String(mq1) + ">" + String((int)thresholds.mq1) + ") ";
    Serial.printf("  ⚠ MQ1 EXCEEDED: %d > %.1f\n", mq1, thresholds.mq1);
  } else {
    Serial.printf("  ✓ MQ1 OK: %d <= %.1f\n", mq1, thresholds.mq1);
  }
  
  if (mq2 > thresholds.mq2) {
    alert = true;
    alertMsg += "MQ2 HIGH (" + String(mq2) + ">" + String((int)thresholds.mq2) + ") ";
    Serial.printf("  ⚠ MQ2 EXCEEDED: %d > %.1f\n", mq2, thresholds.mq2);
  } else {
    Serial.printf("  ✓ MQ2 OK: %d <= %.1f\n", mq2, thresholds.mq2);
  }
  
  if (mq3 > thresholds.mq3) {
    alert = true;
    alertMsg += "MQ3 HIGH (" + String(mq3) + ">" + String((int)thresholds.mq3) + ") ";
    Serial.printf("  ⚠ MQ3 EXCEEDED: %d > %.1f\n", mq3, thresholds.mq3);
  } else {
    Serial.printf("  ✓ MQ3 OK: %d <= %.1f\n", mq3, thresholds.mq3);
  }
  
  if (dust > thresholds.dust) {
    alert = true;
    alertMsg += "Dust HIGH (" + String(dust, 1) + ">" + String(thresholds.dust, 1) + "µg/m³) ";
    Serial.printf("  ⚠ Dust EXCEEDED: %.1f > %.1f\n", dust, thresholds.dust);
  } else {
    Serial.printf("  ✓ Dust OK: %.1f <= %.1f\n", dust, thresholds.dust);
  }

  // Kích hoạt FAN và BUZZER nếu có cảnh báo
  if (alert) {
    Serial.println(alertMsg);
    Serial.println("  🔴 ACTIVATING FAN and BUZZER...");
    digitalWrite(FAN_PIN, LOW);      // FAN ON (active LOW)
    digitalWrite(BUZZER_PIN, LOW);   // BUZZER ON (active LOW)
    Serial.println("  ✓ FAN and BUZZER are now ON");
  } else {
    Serial.println("  ✓ All values within limits - No alert");
    digitalWrite(FAN_PIN, HIGH);     // FAN OFF
    digitalWrite(BUZZER_PIN, HIGH);  // BUZZER OFF
  }
  Serial.println("--- End Threshold Check ---\n");
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

  Serial.println("Posting JSON:");
  Serial.println(payload);

  int code = http.POST(payload);

  if (code > 0) {
    Serial.print("HTTP Response Code: ");
    Serial.println(code);
    if (code == HTTP_CODE_OK) {
      Serial.println("Server reply: " + http.getString());
      
      // Sau khi gửi data thành công, nếu chưa fetch thresholds thì fetch ngay
      if (!thresholdsFetchedOnce && WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[sendToServer] Data sent successfully, fetching thresholds for the first time...");
        Serial.println("[sendToServer] WiFi status: " + String(WiFi.status()) + " (need " + String(WL_CONNECTED) + ")");
        fetchThresholds();
      } else if (!thresholds.loaded && WiFi.status() == WL_CONNECTED) {
        // Nếu đã fetch nhưng chưa load được (có thể empty settings), retry
        Serial.println("\n[sendToServer] Thresholds not loaded yet, retrying fetch...");
        fetchThresholds();
      }
    }
  } else {
    Serial.print("HTTP Error: ");
    Serial.println(code);
    Serial.println("Error: " + http.errorToString(code));
  }

  http.end();
}

// ================================
// ĐỌC CẢM BIẾN + GỬI + KIỂM TRA
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

  // Kiểm tra thresholds và kích hoạt cảnh báo
  checkThresholds(temperature, humidity, mq1, mq2, mq3, dustDensity);

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
  Serial.println("ESP32 Sensor System START");
  Serial.println("With Threshold Monitoring and Alert System");

  pinMode(DUST_LED_PIN, OUTPUT);
  pinMode(DUST_VOUT_PIN, INPUT);

  pinMode(MQ1_PIN, INPUT);
  pinMode(MQ2_PIN, INPUT);
  pinMode(MQ3_PIN, INPUT);

  pinMode(FAN_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  // Khởi tạo FAN và BUZZER ở trạng thái OFF (HIGH = OFF)
  digitalWrite(FAN_PIN, HIGH);
  digitalWrite(BUZZER_PIN, HIGH);

  dht.begin();
  connectWiFi();
  
  // Đợi WiFi ổn định trước khi fetch thresholds
  delay(2000);
  
  // Kiểm tra WiFi status và log
  Serial.println("\n[SETUP] Checking WiFi status...");
  Serial.printf("  WiFi.status() = %d (WL_CONNECTED = %d)\n", WiFi.status(), WL_CONNECTED);
  
  // Lấy threshold settings ngay khi khởi động
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("[SETUP] ✓ WiFi connected, fetching thresholds...");
    fetchThresholds();
  } else {
    Serial.println("[SETUP] ✗ WiFi not connected, will retry in loop()");
    Serial.println("  Note: fetchThresholds() will be called when WiFi connects");
  }
  
  // Log initial state
  Serial.printf("[SETUP] Initial state: thresholds.loaded = %s\n", thresholds.loaded ? "YES" : "NO");
}

// ================================
// LOOP
// ================================
void loop() {
  unsigned long currentMillis = millis();

  // Gửi dữ liệu cảm biến mỗi 5 giây
  if (currentMillis - lastSend >= SEND_INTERVAL) {
    lastSend = currentMillis;
    readAndSend();
  }

  // Lấy threshold settings mỗi 10 giây (hoặc có thể tăng lên 30s)
  if (currentMillis - lastPoll >= POLL_INTERVAL) {
    lastPoll = currentMillis;
    Serial.printf("\n[LOOP] Polling interval reached (%lu ms since last poll)\n", currentMillis);
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("[LOOP] WiFi connected, calling fetchThresholds()...");
      fetchThresholds();
    } else {
      Serial.println("[LOOP] ✗ WiFi not connected, skipping fetchThresholds()");
    }
  }

  // Kiểm tra WiFi và reconnect nếu cần
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected, attempting reconnect...");
    connectWiFi();
    delay(2000); // Đợi WiFi ổn định
    // Sau khi reconnect, fetch thresholds ngay
    if (WiFi.status() == WL_CONNECTED && !thresholds.loaded) {
      Serial.println("[LOOP] WiFi reconnected, fetching thresholds...");
      fetchThresholds();
    }
  }

  delay(200);
}

