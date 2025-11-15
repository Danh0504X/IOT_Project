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

#define MQ1_PIN 32   // MQ-135 (Gas)
#define MQ2_PIN 33   // MQ-7 (CO)
#define MQ3_PIN 34   // MQ-2 (LPG)

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
bool thresholdsFetchedOnce = false;

DHT dht(DHTPIN, DHTTYPE);
WiFiClient client;
HTTPClient http;

// ================================
// ✨ THAY ĐỔI 1: CẤU TRÚC NGƯỠNG MỚI (MULTI-LEVEL)
// ================================
// Mỗi sensor có nhiều mức ngưỡng (Level) thay vì chỉ 1 giá trị
struct ThresholdLevel {
  String levelName;
  float minValue;
  float maxValue;
  int alertLevel;  // 0=OK, 1=Chú ý, 2=Nguy hiểm, 3=Khẩn cấp
  String message;
};

struct SensorThresholds {
  ThresholdLevel levels[5];  // Tối đa 5 mức cho mỗi sensor
  int levelCount;            // Số lượng level thực tế
};

struct ThresholdSettings {
  SensorThresholds temperature;  // SensorTypeID = 1 (TEMP_DHT11)
  SensorThresholds humidity;     // SensorTypeID = 2 (HUM_DHT11)
  SensorThresholds mq2;          // SensorTypeID = 3 (MQ2 - Gas/LPG)
  SensorThresholds co;           // SensorTypeID = 4 (MQ7 - CO)
  SensorThresholds mq135;        // SensorTypeID = 5 (MQ135 - Air Quality)
  SensorThresholds pm25;         // SensorTypeID = 6 (GP2Y10 - PM2.5)
  bool loaded;
};

ThresholdSettings thresholds;

// ================================
// ✨ THAY ĐỔI 2: NGƯỠNG MẶC ĐỊNH (FALLBACK)
// ================================
void initDefaultThresholds() {
  Serial.println("\n[INIT] Loading default thresholds...");
  
  // Nhiệt độ (4 levels)
  thresholds.temperature.levelCount = 4;
  thresholds.temperature.levels[0] = {"Lạnh", -40, 18, 0, "Nhiệt độ thấp"};
  thresholds.temperature.levels[1] = {"Bình thường", 18, 32, 0, "Nhiệt độ thoải mái"};
  thresholds.temperature.levels[2] = {"Nóng", 32, 39, 1, "Nhiệt độ cao"};
  thresholds.temperature.levels[3] = {"Rất nguy hiểm", 39, 80, 2, "Nhiệt độ cực cao"};
  
  // Độ ẩm (3 levels)
  thresholds.humidity.levelCount = 3;
  thresholds.humidity.levels[0] = {"Khô", 0, 30, 1, "Không khí khô"};
  thresholds.humidity.levels[1] = {"Bình thường", 30, 70, 0, "Độ ẩm tốt"};
  thresholds.humidity.levels[2] = {"Ẩm", 70, 85, 1, "Độ ẩm cao"};
  
  // MQ135 - Chất lượng không khí (4 levels) - SensorTypeID = 5
  thresholds.mq135.levelCount = 4;
  thresholds.mq135.levels[0] = {"Tốt", 0, 50, 0, "Không khí sạch, an toàn"};
  thresholds.mq135.levels[1] = {"Trung bình", 50, 150, 1, "Không khí hơi ô nhiễm"};
  thresholds.mq135.levels[2] = {"Nguy hiểm", 150, 300, 2, "Không khí ô nhiễm"};
  thresholds.mq135.levels[3] = {"Rất nguy hiểm", 300, 600, 3, "NGUY HIỂM! Không khí độc hại, sơ tán ra ngoài"};
  
  // MQ2 - Gas/LPG (4 levels) - SensorTypeID = 3
  thresholds.mq2.levelCount = 4;
  thresholds.mq2.levels[0] = {"An toàn", 0, 300, 0, "Không phát hiện gas"};
  thresholds.mq2.levels[1] = {"Chú ý", 300, 1000, 1, "Có gas"};
  thresholds.mq2.levels[2] = {"Rò rỉ", 1000, 5000, 2, "RÒ RỈ GAS!"};
  thresholds.mq2.levels[3] = {"Rất nguy hiểm", 5000, 10000, 3, "CỰC NGUY HIỂM!"};
  
  // CO - MQ-7 (4 levels)
  thresholds.co.levelCount = 4;
  thresholds.co.levels[0] = {"An toàn", 0, 50, 0, "CO an toàn"};
  thresholds.co.levels[1] = {"Chú ý", 50, 200, 1, "Kiểm tra thông gió"};
  thresholds.co.levels[2] = {"Nguy hiểm", 200, 800, 2, "NGUY HIỂM!"};
  thresholds.co.levels[3] = {"Rất nguy hiểm", 800, 2000, 3, "SƠ TÁN NGAY!"};
  
  // PM2.5 - Dust (5 levels)
  thresholds.pm25.levelCount = 5;
  thresholds.pm25.levels[0] = {"Tốt", 0, 12, 0, "Không khí tốt"};
  thresholds.pm25.levels[1] = {"Trung bình", 12, 35, 1, "Chấp nhận được"};
  thresholds.pm25.levels[2] = {"Kém", 35, 55, 2, "Hạn chế ra ngoài"};
  thresholds.pm25.levels[3] = {"Xấu", 55, 150, 2, "Hạn chế hoạt động"};
  thresholds.pm25.levels[4] = {"Rất nguy hiểm", 150, 500, 3, "KHẨN CẤP!"};
  
  thresholds.loaded = true;  // Đánh dấu đã có ngưỡng mặc định
  Serial.println("[INIT] ✓ Default thresholds loaded successfully");
}

// ================================
// ✨ THAY ĐỔI 3: HÀM TÌM MỨC NGƯỠNG (LEVEL) CỦA GIÁ TRỊ
// ================================
int findAlertLevel(float value, SensorThresholds sensor, String& levelName, String& message) {
  for (int i = 0; i < sensor.levelCount; i++) {
    if (value >= sensor.levels[i].minValue && value < sensor.levels[i].maxValue) {
      levelName = sensor.levels[i].levelName;
      message = sensor.levels[i].message;
      return sensor.levels[i].alertLevel;
    }
  }
  // Nếu vượt max của level cuối cùng
  if (sensor.levelCount > 0) {
    levelName = sensor.levels[sensor.levelCount - 1].levelName;
    message = sensor.levels[sensor.levelCount - 1].message;
    return sensor.levels[sensor.levelCount - 1].alertLevel;
  }
  return 0;  // Mặc định OK
}

// ================================
// ✨ THAY ĐỔI 4: TÍNH AQI (AIR QUALITY INDEX)
// ================================
float calculateAQI(float pm25, int co, int gas) {
  // Công thức AQI đơn giản (có thể tinh chỉnh theo chuẩn quốc tế)
  float aqiPM25 = (pm25 / 500.0) * 500;  // PM2.5 contribution
  float aqiCO = (co / 2000.0) * 200;     // CO contribution
  float aqiGas = (gas / 10000.0) * 150;  // Gas contribution
  
  float aqi = aqiPM25 + aqiCO + aqiGas;
  return aqi;
}

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
// ✨ THAY ĐỔI 5: FETCH THRESHOLDS TỪ SERVER (PARSE MULTI-LEVEL)
// ================================
void fetchThresholds() {
  Serial.println("\n[fetchThresholds] Fetching from server...");
  
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[fetchThresholds] ✗ WiFi not connected");
    return;
  }

  String url = String(pollUrl) + "?deviceId=" + String(deviceId);
  http.begin(client, url);
  http.setTimeout(5000);

  int code = http.GET();
  Serial.printf("  HTTP Response Code: %d\n", code);

  if (code == HTTP_CODE_OK) {
    String response = http.getString();
    Serial.println("  Server response: " + response);

    // Parse JSON response
    DynamicJsonDocument doc(4096);  // Tăng size vì data phức tạp hơn
    DeserializationError error = deserializeJson(doc, response);

    if (!error && doc.containsKey("update") && doc["update"] == true) {
      bool thresholdsParsed = false;
      
      // Try to parse multi-level thresholds first
      if (doc.containsKey("thresholds")) {
        JsonObject thresholdsObj = doc["thresholds"];
        
        // Parse temperature
        if (thresholdsObj.containsKey("temperature")) {
          JsonArray tempArray = thresholdsObj["temperature"];
          thresholds.temperature.levelCount = min((int)tempArray.size(), 5);
          for (int i = 0; i < thresholds.temperature.levelCount; i++) {
            JsonObject level = tempArray[i];
            thresholds.temperature.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        // Parse humidity
        if (thresholdsObj.containsKey("humidity")) {
          JsonArray humArray = thresholdsObj["humidity"];
          thresholds.humidity.levelCount = min((int)humArray.size(), 5);
          for (int i = 0; i < thresholds.humidity.levelCount; i++) {
            JsonObject level = humArray[i];
            thresholds.humidity.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        // Parse mq135 (air quality)
        if (thresholdsObj.containsKey("mq135")) {
          JsonArray mq135Array = thresholdsObj["mq135"];
          thresholds.mq135.levelCount = min((int)mq135Array.size(), 5);
          for (int i = 0; i < thresholds.mq135.levelCount; i++) {
            JsonObject level = mq135Array[i];
            thresholds.mq135.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        // Parse mq2 (gas/lpg) - server may return as "mq3" or "mq2"
        if (thresholdsObj.containsKey("mq3")) {
          // Server returns MQ2 (Gas/LPG) as "mq3"
          JsonArray mq3Array = thresholdsObj["mq3"];
          thresholds.mq2.levelCount = min((int)mq3Array.size(), 5);
          for (int i = 0; i < thresholds.mq2.levelCount; i++) {
            JsonObject level = mq3Array[i];
            thresholds.mq2.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        } else if (thresholdsObj.containsKey("mq2") && !thresholdsObj.containsKey("co")) {
          // Only use mq2 for Gas/LPG if co is not present (to avoid conflict)
          JsonArray mq2Array = thresholdsObj["mq2"];
          thresholds.mq2.levelCount = min((int)mq2Array.size(), 5);
          for (int i = 0; i < thresholds.mq2.levelCount; i++) {
            JsonObject level = mq2Array[i];
            thresholds.mq2.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        // Parse co (MQ-7) - server may return as "mq2" or "co"
        if (thresholdsObj.containsKey("co")) {
          JsonArray coArray = thresholdsObj["co"];
          thresholds.co.levelCount = min((int)coArray.size(), 5);
          for (int i = 0; i < thresholds.co.levelCount; i++) {
            JsonObject level = coArray[i];
            thresholds.co.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        } else if (thresholdsObj.containsKey("mq2")) {
          // Fallback: server returns MQ7 (CO) as "mq2"
          JsonArray mq2Array = thresholdsObj["mq2"];
          thresholds.co.levelCount = min((int)mq2Array.size(), 5);
          for (int i = 0; i < thresholds.co.levelCount; i++) {
            JsonObject level = mq2Array[i];
            thresholds.co.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        // Parse pm25 (dust)
        if (thresholdsObj.containsKey("pm25")) {
          JsonArray pm25Array = thresholdsObj["pm25"];
          thresholds.pm25.levelCount = min((int)pm25Array.size(), 5);
          for (int i = 0; i < thresholds.pm25.levelCount; i++) {
            JsonObject level = pm25Array[i];
            thresholds.pm25.levels[i] = {
              level["levelName"].as<String>(),
              level["minValue"].as<float>(),
              level["maxValue"].as<float>(),
              level["alertLevel"].as<int>(),
              level["message"].as<String>()
            };
          }
          thresholdsParsed = true;
        }
        
        if (thresholdsParsed) {
          thresholds.loaded = true;
          thresholdsFetchedOnce = true;
          Serial.println("  ✅ Multi-level thresholds loaded from server");
        }
      }
      
      // Fallback: Parse simple settings and convert to multi-level
      if (!thresholdsParsed && doc.containsKey("settings")) {
        JsonObject settingsObj = doc["settings"];
        Serial.println("  ⚠ Parsing simple settings and converting to multi-level...");
        
        // Update maxValue of "Bình thường" level for each sensor
        if (settingsObj.containsKey("temperature")) {
          float maxVal = settingsObj["temperature"].as<float>();
          // Find "Bình thường" level and update maxValue
          for (int i = 0; i < thresholds.temperature.levelCount; i++) {
            if (thresholds.temperature.levels[i].levelName.indexOf("Bình thường") >= 0) {
              thresholds.temperature.levels[i].maxValue = maxVal;
              Serial.printf("    Updated temperature 'Bình thường' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        if (settingsObj.containsKey("humidity")) {
          float maxVal = settingsObj["humidity"].as<float>();
          for (int i = 0; i < thresholds.humidity.levelCount; i++) {
            if (thresholds.humidity.levels[i].levelName.indexOf("Bình thường") >= 0) {
              thresholds.humidity.levels[i].maxValue = maxVal;
              Serial.printf("    Updated humidity 'Bình thường' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        if (settingsObj.containsKey("mq1") || settingsObj.containsKey("mq135")) {
          float maxVal = settingsObj.containsKey("mq1") ? 
                         settingsObj["mq1"].as<float>() : 
                         settingsObj["mq135"].as<float>();
          for (int i = 0; i < thresholds.mq135.levelCount; i++) {
            if (thresholds.mq135.levels[i].levelName.indexOf("An toàn") >= 0 || 
                thresholds.mq135.levels[i].levelName.indexOf("Bình thường") >= 0) {
              thresholds.mq135.levels[i].maxValue = maxVal;
              Serial.printf("    Updated mq135 'An toàn' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        // Map mq2 from settings to CO (MQ-7)
        if (settingsObj.containsKey("mq2")) {
          float maxVal = settingsObj["mq2"].as<float>();
          for (int i = 0; i < thresholds.co.levelCount; i++) {
            if (thresholds.co.levels[i].levelName.indexOf("An toàn") >= 0) {
              thresholds.co.levels[i].maxValue = maxVal;
              Serial.printf("    Updated CO (MQ-7) 'An toàn' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        // Map mq3 from settings to MQ2 (Gas/LPG)
        if (settingsObj.containsKey("mq3")) {
          float maxVal = settingsObj["mq3"].as<float>();
          for (int i = 0; i < thresholds.mq2.levelCount; i++) {
            if (thresholds.mq2.levels[i].levelName.indexOf("An toàn") >= 0) {
              thresholds.mq2.levels[i].maxValue = maxVal;
              Serial.printf("    Updated MQ2 (Gas/LPG) 'An toàn' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        if (settingsObj.containsKey("dust")) {
          float maxVal = settingsObj["dust"].as<float>();
          for (int i = 0; i < thresholds.pm25.levelCount; i++) {
            if (thresholds.pm25.levels[i].levelName.indexOf("Tốt") >= 0 || 
                thresholds.pm25.levels[i].levelName.indexOf("Bình thường") >= 0) {
              thresholds.pm25.levels[i].maxValue = maxVal;
              Serial.printf("    Updated PM2.5 'Tốt' maxValue to %.1f\n", maxVal);
              break;
            }
          }
        }
        
        thresholds.loaded = true;
        thresholdsFetchedOnce = true;
        Serial.println("  ✅ Thresholds updated from simple settings");
      }
      
      if (!thresholdsParsed && !doc.containsKey("settings")) {
        Serial.println("  ⚠ No thresholds or settings in response, using defaults");
      }
    } else {
      Serial.println("  ⚠ JSON parse error or update=false");
      if (error) {
        Serial.print("  JSON Error: ");
        Serial.println(error.c_str());
      }
    }
  } else {
    Serial.printf("  ✗ HTTP Error: %d\n", code);
  }

  http.end();
}

// ================================
// ✨ THAY ĐỔI 6: LOGIC KIỂM TRA VÀ KÍCH HOẠT MỚI
// ================================
void checkThresholds(float temperature, float humidity, int mq135, int mq7, int mq2, float pm25) {
  Serial.println("\n========================================");
  Serial.println("=== CHECKING THRESHOLDS (MULTI-LEVEL) ===");
  Serial.println("========================================");
  
  if (!thresholds.loaded) {
    Serial.println("⚠ Thresholds not loaded, using defaults");
    initDefaultThresholds();
  }

  // --------------------------------
  // BƯỚC 1: Xác định Alert Level của từng sensor
  // --------------------------------
  String tempLevelName, humLevelName, mq135LevelName, mq2LevelName, coLevelName, pm25LevelName;
  String tempMsg, humMsg, mq135Msg, mq2Msg, coMsg, pm25Msg;
  
  int tempAlert = findAlertLevel(temperature, thresholds.temperature, tempLevelName, tempMsg);
  int humAlert = findAlertLevel(humidity, thresholds.humidity, humLevelName, humMsg);
  int mq135Alert = findAlertLevel(mq135, thresholds.mq135, mq135LevelName, mq135Msg);
  int mq2Alert = findAlertLevel(mq2, thresholds.mq2, mq2LevelName, mq2Msg);
  int coAlert = findAlertLevel(mq7, thresholds.co, coLevelName, coMsg);
  int pm25Alert = findAlertLevel(pm25, thresholds.pm25, pm25LevelName, pm25Msg);
  
  // In ra kết quả
  Serial.println("\n--- SENSOR READINGS & LEVELS ---");
  Serial.printf("🌡️  Nhiệt độ: %.1f°C → [%s] Alert=%d | %s\n", 
                temperature, tempLevelName.c_str(), tempAlert, tempMsg.c_str());
  Serial.printf("💧 Độ ẩm: %.1f%% → [%s] Alert=%d | %s\n", 
                humidity, humLevelName.c_str(), humAlert, humMsg.c_str());
  Serial.printf("🌫️  MQ135 (Air Quality): %d → [%s] Alert=%d | %s\n", 
                mq135, mq135LevelName.c_str(), mq135Alert, mq135Msg.c_str());
  Serial.printf("🔥 MQ2 (Gas/LPG): %d → [%s] Alert=%d | %s\n", 
                mq2, mq2LevelName.c_str(), mq2Alert, mq2Msg.c_str());
  Serial.printf("☠️  CO (MQ-7): %d → [%s] Alert=%d | %s\n", 
                mq7, coLevelName.c_str(), coAlert, coMsg.c_str());
  Serial.printf("💨 PM2.5: %.1f µg/m³ → [%s] Alert=%d | %s\n", 
                pm25, pm25LevelName.c_str(), pm25Alert, pm25Msg.c_str());
  
  // --------------------------------
  // BƯỚC 2: Tính AQI
  // --------------------------------
  float aqi = calculateAQI(pm25, mq7, mq135);
  Serial.printf("\n📊 AQI (Air Quality Index): %.1f\n", aqi);
  
  // --------------------------------
  // BƯỚC 3: LOGIC KÍCH HOẠT QUẠT
  // --------------------------------
  bool fanOn = false;
  String fanReason = "";
  
  // Rule 1: AQI >= 75
  if (aqi >= 75) {
    fanOn = true;
    fanReason += "AQI>=75 ";
  }
  
  // Rule 2: Bất kỳ sensor nào AlertLevel >= 2 (trừ độ ẩm)
  if (tempAlert >= 2 || mq135Alert >= 2 || mq2Alert >= 2 || coAlert >= 2 || pm25Alert >= 2) {
    fanOn = true;
    fanReason += "Sensor_Alert>=2 ";
  }
  
  // Rule 3: Độ ẩm đặc biệt (AlertLevel >= 1)
  if (humAlert >= 1) {
    fanOn = true;
    fanReason += "Humidity_High ";
  }
  
  // --------------------------------
  // BƯỚC 4: LOGIC KÍCH HOẠT CHUÔNG
  // --------------------------------
  bool buzzerOn = false;
  String buzzerReason = "";
  
  // Rule 3.1.1: Bất kỳ sensor nào AlertLevel = 3 (KHẨN CẤP)
  if (tempAlert == 3 || mq135Alert == 3 || mq2Alert == 3 || coAlert == 3 || pm25Alert == 3) {
    buzzerOn = true;
    buzzerReason += "EMERGENCY(Alert=3) ";
  }
  
  // Rule 3.1.1 (đặc biệt): Độ ẩm AlertLevel = 1 → Chuông kêu
  if (humAlert >= 1) {
    buzzerOn = true;
    buzzerReason += "Humidity_Alert ";
  }
  
  // Rule 3.1.2: PM2.5 XẤU + bất kỳ MQ nào XẤU
  if (pm25Alert >= 2 && (mq135Alert >= 2 || mq2Alert >= 2 || coAlert >= 2)) {
    buzzerOn = true;
    buzzerReason += "PM2.5_+_MQ_Bad ";
  }
  
  // Rule 3.1.3: MQ-7 (CO) XẤU (AlertLevel >= 2)
  if (coAlert >= 2) {
    buzzerOn = true;
    buzzerReason += "CO_Dangerous ";
  }
  
  // Rule 3.1.4: MQ-135 XẤU + Độ ẩm cao
  if ((mq135Alert >= 2 || mq2Alert >= 2) && humAlert >= 1) {
    buzzerOn = true;
    buzzerReason += "Gas_+_Humid ";
  }
  
  // Rule 3.1.5: Nhiệt độ quá nóng + PM2.5 cao nhẹ
  if (tempAlert >= 2 && pm25Alert >= 1 && aqi >= 100) {
    buzzerOn = true;
    buzzerReason += "Very_Hot_+_PM2.5 ";
  }
  
  // --------------------------------
  // BƯỚC 5: KÍCH HOẠT THIẾT BỊ
  // --------------------------------
  Serial.println("\n========================================");
  Serial.println("=== CONTROL DECISION ===");
  Serial.println("========================================");
  
  if (fanOn) {
    digitalWrite(FAN_PIN, LOW);  // Quạt BẬT
    Serial.println("🌀 QUẠT: BẬT ✅");
    Serial.println("   Lý do: " + fanReason);
  } else {
    digitalWrite(FAN_PIN, HIGH);  // Quạt TẮT
    Serial.println("🌀 QUẠT: TẮT ⚪");
  }
  
  if (buzzerOn) {
    digitalWrite(BUZZER_PIN, LOW);  // Chuông BẬT
    Serial.println("🔔 CHUÔNG: BẬT 🚨");
    Serial.println("   Lý do: " + buzzerReason);
  } else {
    digitalWrite(BUZZER_PIN, HIGH);  // Chuông TẮT
    Serial.println("🔔 CHUÔNG: TẮT ⚪");
  }
  
  Serial.println("========================================\n");
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
      
      // Fetch thresholds nếu chưa có
      if (!thresholdsFetchedOnce && WiFi.status() == WL_CONNECTED) {
        fetchThresholds();
      }
    }
  } else {
    Serial.print("HTTP Error: ");
    Serial.println(code);
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
    Serial.println("✗ Failed to read DHT, using defaults");
    humidity    = 60.0;
    temperature = 25.0;
  }

  // MQ sensors
  int mq135 = analogRead(MQ1_PIN);  // Gas
  int mq7   = analogRead(MQ2_PIN);  // CO
  int mq2   = analogRead(MQ3_PIN);  // LPG (không dùng trong logic mới)

  // Dust sensor GP2Y1010
  digitalWrite(DUST_LED_PIN, LOW);
  delayMicroseconds(280);
  int dustRaw = analogRead(DUST_VOUT_PIN);
  delayMicroseconds(40);
  digitalWrite(DUST_LED_PIN, HIGH);
  delayMicroseconds(9680);

  float voltage = dustRaw * (3.3 / 4095.0);
  float pm25 = (voltage - 0.9) * 1000 / 0.5;
  if (pm25 < 0) pm25 = 0;

  Serial.printf("Temp: %.1f°C | Hum: %.1f%% | MQ135: %d | MQ7: %d | PM2.5: %.1f µg/m³\n",
                temperature, humidity, mq135, mq7, pm25);

  // ✨ THAY ĐỔI: Gọi hàm kiểm tra mới
  checkThresholds(temperature, humidity, mq135, mq7, mq2, pm25);

  // JSON gửi lên server
  StaticJsonDocument<256> doc;
  doc["deviceId"]    = deviceId;
  doc["temperature"] = temperature;
  doc["humidity"]    = humidity;
  doc["mq135"]       = mq135;
  doc["mq7"]         = mq7;
  doc["mq2"]         = mq2;
  doc["dust"]        = pm25;

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
  Serial.println("ESP32 Multi-Level Threshold System START");

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
  
  // ✨ THAY ĐỔI: Load ngưỡng mặc định ngay từ đầu
  initDefaultThresholds();
  
  connectWiFi();
  delay(2000);
  
  if (WiFi.status() == WL_CONNECTED) {
    fetchThresholds();
  }
}

// ================================
// LOOP
// ================================
void loop() {
  unsigned long currentMillis = millis();

  if (currentMillis - lastSend >= SEND_INTERVAL) {
    lastSend = currentMillis;
    readAndSend();
  }

  if (currentMillis - lastPoll >= POLL_INTERVAL) {
    lastPoll = currentMillis;
    if (WiFi.status() == WL_CONNECTED) {
      fetchThresholds();
    }
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected, reconnecting...");
    connectWiFi();
    delay(2000);
    if (WiFi.status() == WL_CONNECTED && !thresholds.loaded) {
      fetchThresholds();
    }
  }

  delay(200);
}