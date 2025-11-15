# 📊 GIẢI THÍCH VỀ 4 MỨC CẢNH BÁO (THRESHOLD LEVELS)

## 🎯 TỔNG QUAN

Hệ thống sử dụng **Multi-Level Threshold System** - mỗi sensor có **nhiều mức cảnh báo** (thường là 3-5 mức) thay vì chỉ 1 giá trị ngưỡng.

---

## 📍 VỊ TRÍ LƯU TRỮ

### **1. Database: Bảng `Threshold`**

Tất cả các mức cảnh báo được lưu trong bảng `Threshold`:

```sql
CREATE TABLE Threshold (
    ThresholdID INT IDENTITY(1,1) PRIMARY KEY,
    SensorTypeID INT NOT NULL,           -- ID của sensor (1-6)
    LevelName NVARCHAR(50) NOT NULL,     -- Tên mức: "Tốt", "Bình thường", "Nguy hiểm", ...
    MinValue FLOAT NOT NULL,             -- Giá trị tối thiểu
    MaxValue FLOAT NOT NULL,             -- Giá trị tối đa
    AlertLevel INT CHECK (0-3),          -- Mức cảnh báo: 0=OK, 1=Chú ý, 2=Nguy hiểm, 3=Khẩn cấp
    Message NVARCHAR(300)                -- Thông báo
);
```

### **2. Các Mức Cảnh Báo Mặc Định (trong `sql_script.txt`)**

#### **PM2.5 (SensorTypeID=6) - 5 mức:**
```sql
INSERT INTO Threshold VALUES
(6, N'Tốt',        0,   12,  0, N'Không khí tốt'),
(6, N'Trung bình', 12,  35,  1, N'Chấp nhận được'),
(6, N'Kém',        35,  55,  2, N'Nhạy cảm nên hạn chế ra ngoài'),
(6, N'Xấu',        55,  150, 2, N'Hạn chế hoạt động ngoài trời'),
(6, N'Nguy hiểm',  150, 500, 3, N'KHẨN CẤP! Ở trong nhà');
```

#### **CO - MQ7 (SensorTypeID=4) - 4 mức:**
```sql
INSERT INTO Threshold VALUES
(4, N'An toàn',       0,    50,   0, N'Nồng độ CO an toàn'),
(4, N'Chú ý',         50,   200,  1, N'Kiểm tra thông gió'),
(4, N'Nguy hiểm',     200,  800,  2, N'NGUY HIỂM! Tăng thông gió'),
(4, N'Rất nguy hiểm', 800,  2000, 3, N'SƠ TÁN NGAY! Nguy cơ tử vong');
```

#### **Gas/LPG - MQ2 (SensorTypeID=3) - 4 mức:**
```sql
INSERT INTO Threshold VALUES
(3, N'An toàn',    0,    300,  0, N'Không phát hiện gas'),
(3, N'Chú ý',      300,  1000, 1, N'Có gas, kiểm tra nguồn'),
(3, N'Rò rỉ',      1000, 5000, 2, N'RÒ RỈ GAS! Tắt nguồn ngay'),
(3, N'Nguy cơ nổ', 5000, 10000, 3, N'CỰC NGUY HIỂM! Sơ tán ngay');
```

#### **Nhiệt độ (SensorTypeID=1) - 4 mức:**
```sql
INSERT INTO Threshold VALUES
(1, N'Lạnh',      -40, 18, 0, N'Nhiệt độ thấp'),
(1, N'Bình thường', 18, 32, 0, N'Nhiệt độ thoải mái'),
(1, N'Nóng',       32, 39, 1, N'Nhiệt độ cao'),
(1, N'Rất nóng',   39, 80, 2, N'Cảnh báo say nóng');
```

#### **Độ ẩm (SensorTypeID=2) - 3 mức:**
```sql
INSERT INTO Threshold VALUES
(2, N'Khô',        0,  30, 1, N'Không khí khô'),
(2, N'Bình thường', 30, 70, 0, N'Độ ẩm tốt'),
(2, N'Ẩm',         70, 100, 1, N'Độ ẩm cao');
```

#### **MQ135 - Chất lượng không khí (SensorTypeID=5) - 5 mức:**
```sql
INSERT INTO Threshold VALUES
(5, N'Tốt',        0,    200,  0, N'Chất lượng không khí tốt'),
(5, N'Trung bình', 200,  400,  1, N'Chất lượng không khí trung bình'),
(5, N'Kém',        400,  600,  2, N'Chất lượng không khí kém, nên thông gió'),
(5, N'Xấu',        600,  1000, 2, N'Chất lượng không khí xấu, hạn chế ra ngoài'),
(5, N'Nguy hiểm',  1000, 5000, 3, N'KHẨN CẤP! Chất lượng không khí nguy hiểm');
```

---

## ⚠️ VẤN ĐỀ HIỆN TẠI

### **Form Web Chỉ Cập Nhật 1 Mức**

Hiện tại, form trong `thresholds.jsp` chỉ cho phép cập nhật **1 giá trị** cho mỗi sensor:

```jsp
<!-- Chỉ có 1 input cho mỗi sensor -->
<input type="number" name="mq1" value="300.0" />
<input type="number" name="temperature" value="32.0" />
<input type="number" name="humidity" value="70.0" />
```

### **Logic Cập Nhật (ThresholdService.java)**

Khi submit form, `ThresholdService.updateThresholds()` chỉ cập nhật **MaxValue của level "Bình thường"** (hoặc "An toàn", "Tốt"):

```java
// Tìm threshold có LevelName = "Bình thường"
if (threshold.getLevelName().contains("Bình thường") || 
    threshold.getLevelName().contains("An toàn") ||
    threshold.getLevelName().contains("Tốt")) {
    // Chỉ cập nhật MaxValue của level này
    normalThreshold.setMaxValue(thresholdValue.floatValue());
    thresholdDAO.update(normalThreshold, updatedBy);
}
```

**Ví dụ:**
- Nếu bạn nhập `temperature = 35.0` trong form
- Hệ thống sẽ tìm level "Bình thường" của Nhiệt độ (hiện tại: 18-32)
- Chỉ cập nhật MaxValue từ 32 → 35
- Các mức khác ("Lạnh", "Nóng", "Rất nóng") **KHÔNG THAY ĐỔI**

---

## 🔧 CÁCH CẬP NHẬT 4 MỨC CẢNH BÁO

### **Cách 1: Cập Nhật Trực Tiếp Trong Database (SQL)**

Bạn có thể chạy SQL để cập nhật từng mức:

```sql
-- Ví dụ: Cập nhật mức "Nóng" của Nhiệt độ
UPDATE Threshold
SET MinValue = 30.0, MaxValue = 40.0
WHERE SensorTypeID = 1 AND LevelName = N'Nóng';

-- Ví dụ: Cập nhật mức "Trung bình" của PM2.5
UPDATE Threshold
SET MinValue = 10.0, MaxValue = 40.0
WHERE SensorTypeID = 6 AND LevelName = N'Trung bình';

-- Hoặc sử dụng Stored Procedure (có ghi log)
EXEC UpdateThreshold
    @ThresholdID = 123,
    @LevelName = N'Nóng',
    @MinValue = 30.0,
    @MaxValue = 40.0,
    @AlertLevel = 1,
    @Message = N'Nhiệt độ cao',
    @UpdatedBy = 1;
```

### **Cách 2: Cải Thiện Form Web (Đề Xuất)**

Để cập nhật tất cả các mức qua web interface, cần:

1. **Cải thiện `thresholds.jsp`:**
   - Hiển thị tất cả các mức cho mỗi sensor
   - Input fields cho MinValue và MaxValue của từng mức
   - Có thể dùng accordion/collapse để gọn gàng

2. **Cải thiện `ThresholdService.updateThresholds()`:**
   - Nhận danh sách các mức cần cập nhật
   - Cập nhật từng mức một

3. **Ví dụ Form Mới:**
```jsp
<!-- Nhiệt độ - 4 mức -->
<div class="sensor-levels">
    <h5>Nhiệt độ (°C)</h5>
    
    <div class="level-item">
        <label>Mức 1: Lạnh</label>
        <input name="temp_level1_min" value="-40" />
        <input name="temp_level1_max" value="18" />
    </div>
    
    <div class="level-item">
        <label>Mức 2: Bình thường</label>
        <input name="temp_level2_min" value="18" />
        <input name="temp_level2_max" value="32" />
    </div>
    
    <div class="level-item">
        <label>Mức 3: Nóng</label>
        <input name="temp_level3_min" value="32" />
        <input name="temp_level3_max" value="39" />
    </div>
    
    <div class="level-item">
        <label>Mức 4: Rất nóng</label>
        <input name="temp_level4_min" value="39" />
        <input name="temp_level4_max" value="80" />
    </div>
</div>
```

---

## 📊 BẢNG TÓM TẮT CÁC MỨC

| Sensor | Số Mức | Mức 1 | Mức 2 | Mức 3 | Mức 4 | Mức 5 |
|--------|--------|-------|-------|-------|-------|-------|
| **PM2.5** | 5 | Tốt (0-12) | Trung bình (12-35) | Kém (35-55) | Xấu (55-150) | Nguy hiểm (150-500) |
| **CO** | 4 | An toàn (0-50) | Chú ý (50-200) | Nguy hiểm (200-800) | Rất nguy hiểm (800-2000) | - |
| **Gas/LPG** | 4 | An toàn (0-300) | Chú ý (300-1000) | Rò rỉ (1000-5000) | Nguy cơ nổ (5000-10000) | - |
| **Nhiệt độ** | 4 | Lạnh (-40-18) | Bình thường (18-32) | Nóng (32-39) | Rất nóng (39-80) | - |
| **Độ ẩm** | 3 | Khô (0-30) | Bình thường (30-70) | Ẩm (70-100) | - | - |
| **MQ135** | 5 | Tốt (0-200) | Trung bình (200-400) | Kém (400-600) | Xấu (600-1000) | Nguy hiểm (1000-5000) |

---

## 🔍 CÁCH KIỂM TRA CÁC MỨC HIỆN TẠI

### **Query SQL:**
```sql
-- Xem tất cả mức của một sensor
SELECT 
    t.ThresholdID,
    st.SensorName,
    t.LevelName,
    t.MinValue,
    t.MaxValue,
    t.AlertLevel,
    t.Message
FROM Threshold t
JOIN SensorType st ON t.SensorTypeID = st.SensorTypeID
WHERE st.SensorCode = 'TEMP_DHT11'  -- Hoặc 'HUM_DHT11', 'MQ135', ...
ORDER BY t.MinValue ASC;
```

### **Trong Code Java:**
```java
// Lấy tất cả mức của một sensor
SensorTypeDAO sensorTypeDAO = new SensorTypeDAO();
ThresholdDAO thresholdDAO = new ThresholdDAO();

SensorType sensorType = sensorTypeDAO.findByCode("TEMP_DHT11");
List<Threshold> thresholds = thresholdDAO.findBySensorTypeId(sensorType.getSensorTypeId());

for (Threshold t : thresholds) {
    System.out.println(t.getLevelName() + ": " + 
                      t.getMinValue() + " - " + t.getMaxValue() + 
                      " (AlertLevel=" + t.getAlertLevel() + ")");
}
```

---

## 💡 KẾT LUẬN

1. **4 mức cảnh báo được lưu trong bảng `Threshold`** (database)
2. **Hiện tại form web chỉ cập nhật được 1 mức** (MaxValue của level "Bình thường")
3. **Để cập nhật tất cả các mức:**
   - Cách 1: Cập nhật trực tiếp trong database bằng SQL
   - Cách 2: Cải thiện form web để hiển thị và cập nhật tất cả các mức

**Bạn có muốn tôi cải thiện form web để có thể cập nhật tất cả 4 mức không?**


