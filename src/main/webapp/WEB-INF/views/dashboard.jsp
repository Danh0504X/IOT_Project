<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>IoT Sensor Intelligence</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        body {
            background: radial-gradient(circle at top left, #0f172a, #111827 55%, #020617);
            min-height: 100vh;
            color: #e2e8f0;
            font-family: "Inter", "Segoe UI", sans-serif;
        }
        .text-gradient {
            background: linear-gradient(135deg, #60a5fa, #38bdf8, #a855f7);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .glass-card {
            background: rgba(15, 23, 42, 0.75);
            border-radius: 18px;
            border: 1px solid rgba(148, 163, 184, 0.2);
            backdrop-filter: blur(16px);
            box-shadow: 0 25px 50px -12px rgba(15, 23, 42, 0.65);
        }
        .page-header {
            border-bottom: 1px solid rgba(148, 163, 184, 0.25);
            margin-bottom: 2.5rem;
        }
        .badge-soft {
            background: linear-gradient(135deg, rgba(59,130,246,0.25), rgba(14,165,233,0.25));
            color: #bae6fd;
        }
        .metric-card {
            padding: 1.5rem;
            border-radius: 16px;
            position: relative;
            overflow: hidden;
            background: linear-gradient(135deg, rgba(59,130,246,0.35), rgba(14,165,233,0.15));
        }
        .metric-card:before {
            content: "";
            position: absolute;
            inset: 0;
            backdrop-filter: blur(12px);
            opacity: 0.2;
        }
        .metric-card h2, .metric-card span {
            position: relative;
            z-index: 2;
        }
        .table thead {
            background: rgba(59, 130, 246, 0.12);
        }
        .table tbody tr {
            border-color: rgba(148, 163, 184, 0.1);
        }
        .table tbody tr:hover {
            background: rgba(30, 64, 175, 0.25);
        }
        .divider {
            height: 1px;
            background: linear-gradient(90deg, rgba(148, 163, 184, 0), rgba(148, 163, 184, 0.35), rgba(148, 163, 184, 0));
            margin: 2rem 0;
        }
        .chart-card canvas {
            max-height: 320px;
        }
        
        /* AI Prediction Section Styles */
        .prediction-content {
            min-height: 120px;
            padding: 1rem;
            border-radius: 12px;
            background: rgba(30, 41, 59, 0.4);
            border: 1px solid rgba(148, 163, 184, 0.15);
        }
        
        .prediction-item {
            padding: 0.75rem 1rem;
            margin-bottom: 0.75rem;
            border-radius: 8px;
            background: rgba(59, 130, 246, 0.1);
            border-left: 3px solid rgba(59, 130, 246, 0.5);
            transition: all 0.2s ease;
        }
        
        .prediction-item:hover {
            background: rgba(59, 130, 246, 0.15);
            border-left-color: rgba(59, 130, 246, 0.8);
        }
        
        .prediction-item:last-child {
            margin-bottom: 0;
        }
        
        .prediction-label {
            font-size: 0.875rem;
            color: rgba(148, 163, 184, 0.8);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 0.25rem;
        }
        
        .prediction-value {
            font-size: 1.125rem;
            font-weight: 600;
            color: #e2e8f0;
        }
        
        .prediction-trend {
            font-size: 0.75rem;
            margin-top: 0.25rem;
        }
        
        .prediction-trend.up {
            color: #10b981;
        }
        
        .prediction-trend.down {
            color: #ef4444;
        }
        
        .prediction-trend.stable {
            color: rgba(148, 163, 184, 0.7);
        }
        
        /* AQI Badge Styles */
        .aqi-badge {
            display: inline-block;
            padding: 0.4rem 0.8rem;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border: 1.5px solid;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            transition: all 0.2s ease;
        }
        
        /* AQI Level: Tốt (Good) - Green */
        .aqi-badge-good {
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
            border-color: #047857;
            box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
        }
        
        /* AQI Level: Trung bình (Average) - Yellow - Improved */
        .aqi-badge-average {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: #1f2937;
            border-color: #b45309;
            box-shadow: 0 2px 6px rgba(245, 158, 11, 0.4);
            font-weight: 700;
        }
        
        /* AQI Level: Kém (Poor) - Orange */
        .aqi-badge-poor {
            background: linear-gradient(135deg, #f97316, #ea580c);
            color: white;
            border-color: #c2410c;
            box-shadow: 0 2px 6px rgba(249, 115, 22, 0.3);
        }
        
        /* AQI Level: Xấu (Bad) - Red */
        .aqi-badge-bad {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            color: white;
            border-color: #b91c1c;
            box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3);
        }
        
        /* AQI Level: Rất xấu (Very Bad) - Purple */
        .aqi-badge-very-bad {
            background: linear-gradient(135deg, #a855f7, #9333ea);
            color: white;
            border-color: #7e22ce;
            box-shadow: 0 2px 6px rgba(168, 85, 247, 0.3);
        }
        
        /* AQI Level: Nguy hiểm (Dangerous) - Maroon/Dark Red */
        .aqi-badge-dangerous {
            background: linear-gradient(135deg, #991b1b, #7f1d1d);
            color: white;
            border-color: #6b1a1a;
            box-shadow: 0 2px 8px rgba(153, 27, 27, 0.5);
            font-weight: 700;
        }
        
        /* AQI Value Styles - For Metric Cards (larger size to match display-6) */
        .aqi-value-card {
            font-weight: 700;
            font-size: 2.5rem; /* Match Bootstrap display-6 size */
            line-height: 1.2;
        }
        
        /* AQI Value Styles - For Table (smaller size) */
        .aqi-value {
            font-weight: 700;
            font-size: 1rem; /* Smaller for table */
        }
        
        /* AQI Value Colors based on AQI range - For Cards */
        .aqi-value-card.aqi-value-good {
            color: #10b981 !important; /* Green - Tốt (0-50) */
        }
        
        .aqi-value-card.aqi-value-average {
            color: #f59e0b !important; /* Amber/Orange - Trung bình (51-100) */
        }
        
        .aqi-value-card.aqi-value-poor {
            color: #f97316 !important; /* Orange - Kém (101-150) */
        }
        
        .aqi-value-card.aqi-value-bad {
            color: #ef4444 !important; /* Red - Xấu (151-200) */
        }
        
        .aqi-value-card.aqi-value-very-bad {
            color: #a855f7 !important; /* Purple - Rất xấu (201-300) */
        }
        
        .aqi-value-card.aqi-value-dangerous {
            color: #dc2626 !important; /* Dark Red - Nguy hiểm (>300) */
        }
        
        /* AQI Value Colors based on AQI range - For Table */
        .aqi-value.aqi-value-good {
            color: #10b981 !important; /* Green - Tốt (0-50) */
        }
        
        .aqi-value.aqi-value-average {
            color: #f59e0b !important; /* Amber/Orange - Trung bình (51-100) */
        }
        
        .aqi-value.aqi-value-poor {
            color: #f97316 !important; /* Orange - Kém (101-150) */
        }
        
        .aqi-value.aqi-value-bad {
            color: #ef4444 !important; /* Red - Xấu (151-200) */
        }
        
        .aqi-value.aqi-value-very-bad {
            color: #a855f7 !important; /* Purple - Rất xấu (201-300) */
        }
        
        .aqi-value.aqi-value-dangerous {
            color: #dc2626 !important; /* Dark Red - Nguy hiểm (>300) */
        }
    </style>
</head>
<body>
<div class="container py-5">
    <div class="glass-card p-4 p-lg-5">
        <div class="page-header pb-4 mb-4">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
                <div>
                    <span class="badge badge-soft rounded-pill px-3 py-2 mb-2">IoT Control Center</span>
                    <h1 class="display-6 fw-bold mb-0 text-gradient">Realtime Sensor Intelligence</h1>
                    <p class="text-secondary text-opacity-75 mb-0">Theo dõi nhiệt độ, độ ẩm, chất lượng không khí và tín hiệu mạng từ ESP32 của bạn.</p>
                </div>
                <div class="text-end">
                    <div class="mb-2">
                        <span class="text-secondary text-opacity-75 me-2">👤 ${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username}</span>
                        <a href="${pageContext.request.contextPath}/admin/thresholds?deviceId=${param.deviceId != null ? param.deviceId : 'DEVICE001'}" class="btn btn-sm btn-outline-primary me-2">
                            <i class="bi bi-sliders"></i> Cài đặt ngưỡng
                        </a>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-box-arrow-right"></i> Đăng xuất
                        </a>
                    </div>
                    <p class="text-secondary text-opacity-75 mb-1">Last update</p>
                    <h5 class="mb-0" id="last-update-time">
                        <c:choose>
                            <c:when test="${not empty sensorData and not empty sensorData[0].timestamp}">
                                <fmt:formatDate value="${sensorData[0].timestampAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:when>
                            <c:otherwise>N/A</c:otherwise>
                        </c:choose>
                    </h5>
                    <small class="text-success text-opacity-75" id="realtime-status" style="display: none;">
                        <i class="bi bi-circle-fill" style="font-size: 8px; animation: blink 1s infinite;"></i> Real-time
                    </small>
                </div>
            </div>
        </div>

        <c:if test="${empty sensorData}">
            <div class="text-center py-5">
                <div class="mb-3">
                    <span class="badge rounded-pill bg-info bg-opacity-25 text-info fs-5">
                        <i class="bi bi-wifi-off me-2"></i>No sensor data available yet
                    </span>
                </div>
                <p class="text-secondary text-opacity-75">Hãy chắc chắn rằng ESP32 của bạn đang gửi dữ liệu JSON POST tới <code>/api/data</code>.</p>
            </div>
        </c:if>

        <c:if test="${not empty sensorData}">
            <div class="row g-4 mb-4">
                <div class="col-md-2">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Temperature</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].temperature}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> °C</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-thermometer-half me-1"></i>Thiết bị #${sensorData[0].deviceId}</span>
                    </div>
                </div>
                <div class="col-md-2">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Humidity</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].humidity}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> %</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-droplet-half me-1"></i>Độ ẩm hiện tại</span>
                    </div>
                </div>
                <div class="col-md-2">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Pm2.5</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].dust}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> µg/m³</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-wind me-1"></i>Bụi mịn</span>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="metric-card h-100" style="background: linear-gradient(135deg, rgba(168,85,247,0.35), rgba(139,92,246,0.15));">
                        <span class="text-uppercase text-secondary text-opacity-75 small">AQI Index</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <c:if test="${not empty sensorData}">
                                <c:set var="firstData" value="${sensorData[0]}" />
                                <c:choose>
                                    <c:when test="${firstData != null && firstData.aqi != null}">
                                        <c:set var="aqiValue" value="${firstData.aqi}" />
                                        <c:choose>
                                            <c:when test="${aqiValue <= 50}">
                                                <span class="aqi-value-card aqi-value-good">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:when>
                                            <c:when test="${aqiValue <= 100}">
                                                <span class="aqi-value-card aqi-value-average">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:when>
                                            <c:when test="${aqiValue <= 150}">
                                                <span class="aqi-value-card aqi-value-poor">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:when>
                                            <c:when test="${aqiValue <= 200}">
                                                <span class="aqi-value-card aqi-value-bad">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:when>
                                            <c:when test="${aqiValue <= 300}">
                                                <span class="aqi-value-card aqi-value-very-bad">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="aqi-value-card aqi-value-dangerous">
                                                    <fmt:formatNumber value="${firstData.aqi}" maxFractionDigits="0"/>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="fs-6 fw-semibold ms-2">${firstData.aqiLevel != null ? firstData.aqiLevel : 'N/A'}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-secondary">N/A</span>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <c:if test="${empty sensorData}">
                                <span class="text-secondary">N/A</span>
                            </c:if>
                        </h2>
                        <span class="text-secondary text-opacity-75">
                            <i class="bi bi-speedometer2 me-1"></i>
                            <c:if test="${not empty sensorData}">
                                <c:set var="firstData" value="${sensorData[0]}" />
                                <c:choose>
                                    <c:when test="${firstData != null && firstData.mainPollutant != null && not empty firstData.mainPollutant}">
                                        Chất ô nhiễm chính: ${firstData.mainPollutant}
                                    </c:when>
                                    <c:otherwise>
                                        Chỉ số chất lượng không khí
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <c:if test="${empty sensorData}">
                                Chỉ số chất lượng không khí
                            </c:if>
                        </span>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Wi-Fi Signal</span>
                        <h2 class="display-6 fw-bold mt-2">
                            ${sensorData[0].wifiSignal}<span class="fs-5 fw-semibold"> dBm</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-router me-1"></i>Tín hiệu ESP32</span>
                    </div>
                </div>
            </div>

            <div class="divider"></div>

            <div class="row g-4 mb-4">
                <div class="col-lg-6">
                    <div class="glass-card chart-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="mb-0"><i class="bi bi-graph-up-arrow me-2"></i>Nhiệt độ &amp; Độ ẩm</h5>
                            <span class="text-secondary text-opacity-75 small">Biểu đồ 10 lần đọc gần nhất</span>
                        </div>
                        <canvas id="climateChart"></canvas>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="glass-card chart-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="mb-0"><i class="bi bi-speedometer2 me-2"></i>MQ Gas Sensors</h5>
                            <span class="text-secondary text-opacity-75 small">Giá trị MQ1 / MQ2 / MQ3</span>
                        </div>
                        <canvas id="mqChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- AI Prediction Section -->
            <div class="glass-card p-4 mb-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="mb-0">
                        <i class="bi bi-robot me-2"></i>Dự đoán 1 giờ tới
                    </h5>
                    <span class="text-secondary text-opacity-75 small">
                        <i class="bi bi-clock-history me-1"></i>Dự đoán bằng AI
                    </span>
                </div>
                <div id="ai-prediction-content" class="prediction-content">
                    <!-- Placeholder: Đang tải dự đoán -->
                    <div class="text-center py-4">
                        <div class="mb-3">
                            <i class="bi bi-hourglass-split" style="font-size: 2.5rem; color: rgba(148, 163, 184, 0.5);"></i>
                        </div>
                        <p class="text-secondary text-opacity-75 mb-0">
                            Đang tải dự đoán...
                        </p>
                        <small class="text-secondary text-opacity-50">
                            Dữ liệu dự đoán sẽ được hiển thị tại đây
                        </small>
                    </div>
                    
                    <!-- 
                    Example HTML structure for AI predictions (uncomment and modify when integrating AI):
                    
                    <div class="row g-3">
                        <div class="col-md-3">
                            <div class="prediction-item">
                                <div class="prediction-label">Nhiệt độ</div>
                                <div class="prediction-value">29.5°C</div>
                                <div class="prediction-trend up">
                                    <i class="bi bi-arrow-up"></i> Tăng 1.2°C
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="prediction-item">
                                <div class="prediction-label">Độ ẩm</div>
                                <div class="prediction-value">68.5%</div>
                                <div class="prediction-trend stable">
                                    <i class="bi bi-dash"></i> Ổn định
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="prediction-item">
                                <div class="prediction-label">AQI</div>
                                <div class="prediction-value">65</div>
                                <div class="prediction-trend down">
                                    <i class="bi bi-arrow-down"></i> Giảm 3 điểm
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="prediction-item">
                                <div class="prediction-label">PM2.5</div>
                                <div class="prediction-value">12.5 µg/m³</div>
                                <div class="prediction-trend stable">
                                    <i class="bi bi-dash"></i> Ổn định
                                </div>
                            </div>
                        </div>
                    </div>
                    -->
                </div>
            </div>

            <div class="glass-card p-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="mb-0"><i class="bi bi-database-check me-2"></i>Bản ghi cảm biến gần nhất</h5>
                    <span class="text-secondary text-opacity-75 small">Hiển thị tối đa 10 mục mới nhất</span>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover table-borderless align-middle mb-0 text-light">
                        <thead>
                        <tr class="text-uppercase small text-secondary text-opacity-75">
                            <th>#</th>
                            <th>Device</th>
                            <th>Temp (°C)</th>
                            <th>Humidity (%)</th>
                            <th>MQ135</th>
                            <th>MQ7</th>
                            <th>MQ2</th>
                            <th>Dust (µg/m³)</th>
                            <th>AQI</th>
                            <th>AQI Level</th>
                            <th>Wi-Fi (dBm)</th>
                            <th>Uptime (s)</th>
                            <th>Timestamp</th>
                        </tr>
                        </thead>
                        <tbody class="text-secondary text-opacity-90">
                        <c:forEach var="item" items="${sensorData}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><span class="badge bg-primary bg-opacity-25 text-primary">#${item.deviceId}</span></td>
                                <td><fmt:formatNumber value="${item.temperature}" maxFractionDigits="2"/></td>
                                <td><fmt:formatNumber value="${item.humidity}" maxFractionDigits="2"/></td>
                                <td><fmt:formatNumber value="${item.mq1}" maxFractionDigits="0"/></td>
                                <td><fmt:formatNumber value="${item.mq2}" maxFractionDigits="0"/></td>
                                <td><fmt:formatNumber value="${item.mq3}" maxFractionDigits="0"/></td>
                                <td><fmt:formatNumber value="${item.dust}" maxFractionDigits="2"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.aqi != null}">
                                            <c:set var="aqiValue" value="${item.aqi}" />
                                            <c:choose>
                                                <c:when test="${aqiValue <= 50}">
                                                    <span class="aqi-value aqi-value-good">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:when>
                                                <c:when test="${aqiValue <= 100}">
                                                    <span class="aqi-value aqi-value-average">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:when>
                                                <c:when test="${aqiValue <= 150}">
                                                    <span class="aqi-value aqi-value-poor">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:when>
                                                <c:when test="${aqiValue <= 200}">
                                                    <span class="aqi-value aqi-value-bad">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:when>
                                                <c:when test="${aqiValue <= 300}">
                                                    <span class="aqi-value aqi-value-very-bad">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="aqi-value aqi-value-dangerous">
                                                        <fmt:formatNumber value="${item.aqi}" maxFractionDigits="0"/>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>N/A</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.aqiLevel != null && not empty item.aqiLevel}">
                                            <c:set var="aqiLevelLower" value="${fn:toLowerCase(item.aqiLevel)}" />
                                            <c:choose>
                                                <c:when test="${fn:contains(aqiLevelLower, 'tốt') || fn:contains(aqiLevelLower, 'good')}">
                                                    <span class="aqi-badge aqi-badge-good">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(aqiLevelLower, 'trung bình') || fn:contains(aqiLevelLower, 'average') || fn:contains(aqiLevelLower, 'trung')}">
                                                    <span class="aqi-badge aqi-badge-average">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(aqiLevelLower, 'kém') || fn:contains(aqiLevelLower, 'poor')}">
                                                    <span class="aqi-badge aqi-badge-poor">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(aqiLevelLower, 'xấu') || fn:contains(aqiLevelLower, 'bad')}">
                                                    <span class="aqi-badge aqi-badge-bad">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(aqiLevelLower, 'rất xấu') || fn:contains(aqiLevelLower, 'very bad')}">
                                                    <span class="aqi-badge aqi-badge-very-bad">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:when test="${fn:contains(aqiLevelLower, 'nguy hiểm') || fn:contains(aqiLevelLower, 'dangerous') || fn:contains(aqiLevelLower, 'nguy')}">
                                                    <span class="aqi-badge aqi-badge-dangerous">${item.aqiLevel}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="itemAqiColor" value="${item.aqiColor != null ? item.aqiColor : '#60a5fa'}" />
                                                    <span class="aqi-badge" style="background-color: ${itemAqiColor}; color: white; border-color: ${itemAqiColor};">
                                                        ${item.aqiLevel}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>N/A</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${item.wifiSignal}</td>
                                <td>${item.uptime}</td>
                                <td><fmt:formatDate value="${item.timestampAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>

<!-- Real-time Update Script: Cập nhật dữ liệu trực tiếp mà không cần reload trang -->
<script>
    // Cấu hình
    const REFRESH_INTERVAL = 5000; // 5 giây
    let climateChart = null;
    let mqChart = null;
    let refreshTimer = null;
    let refreshIndicator = null;

    // CSS cho spinner animation
    if (!document.getElementById('refresh-styles')) {
        const style = document.createElement('style');
        style.id = 'refresh-styles';
        style.textContent = 
            '@keyframes spin {' +
                'from { transform: rotate(0deg); }' +
                'to { transform: rotate(360deg); }' +
            '}' +
            '.spin {' +
                'animation: spin 1s linear infinite;' +
                'display: inline-block;' +
            '}' +
            '.fade-in {' +
                'animation: fadeIn 0.3s ease-in;' +
            '}' +
            '@keyframes fadeIn {' +
                'from { opacity: 0; }' +
                'to { opacity: 1; }' +
            '}' +
            '@keyframes blink {' +
                '0%, 100% { opacity: 1; }' +
                '50% { opacity: 0.3; }' +
            '}';
        document.head.appendChild(style);
    }

    // Tạo hoặc hiển thị refresh indicator
    function showRefreshIndicator() {
        if (!refreshIndicator) {
            refreshIndicator = document.createElement('div');
            refreshIndicator.id = 'refresh-indicator';
            refreshIndicator.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Đang cập nhật...';
            refreshIndicator.style.cssText = 'position:fixed;top:20px;right:20px;background:rgba(59,130,246,0.9);color:white;padding:10px 20px;border-radius:8px;z-index:9999;font-size:14px;box-shadow:0 4px 6px rgba(0,0,0,0.3);';
            document.body.appendChild(refreshIndicator);
        }
        refreshIndicator.style.display = 'block';
    }

    function hideRefreshIndicator() {
        if (refreshIndicator) {
            refreshIndicator.style.display = 'none';
        }
    }

    // Cập nhật timestamp "Last update"
    function updateLastUpdateTime(timestamp) {
        const timeElement = document.getElementById('last-update-time');
        const statusElement = document.getElementById('realtime-status');
        
        if (timeElement) {
            const timeString = timestamp || new Date().toLocaleString('vi-VN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            }).replace(',', '');
            timeElement.textContent = timestamp || timeString;
        }
        
        // Hiển thị real-time status indicator
        if (statusElement) {
            statusElement.style.display = 'inline-block';
        }
    }

    // Format số với số chữ số thập phân
    function formatNumber(value, decimals = 1) {
        if (value == null || isNaN(value)) return '0';
        return parseFloat(value).toFixed(decimals);
    }
    
    // Get AQI Value CSS class based on AQI number
    function getAQIValueClass(aqi) {
        if (aqi == null || isNaN(aqi)) return '';
        const value = parseFloat(aqi);
        if (value <= 50) return 'aqi-value-good';
        if (value <= 100) return 'aqi-value-average';
        if (value <= 150) return 'aqi-value-poor';
        if (value <= 200) return 'aqi-value-bad';
        if (value <= 300) return 'aqi-value-very-bad';
        return 'aqi-value-dangerous';
    }
    
    // Get AQI Badge HTML based on level
    function getAQIBadgeHTML(level, color) {
        if (!level) return 'N/A';
        const levelLower = level.toLowerCase();
        let badgeClass = '';
        
        if (levelLower.includes('tốt') || levelLower.includes('good')) {
            badgeClass = 'aqi-badge-good';
        } else if (levelLower.includes('trung bình') || levelLower.includes('average') || levelLower.includes('trung')) {
            badgeClass = 'aqi-badge-average';
        } else if (levelLower.includes('kém') || levelLower.includes('poor')) {
            badgeClass = 'aqi-badge-poor';
        } else if (levelLower.includes('xấu') || levelLower.includes('bad')) {
            badgeClass = 'aqi-badge-bad';
        } else if (levelLower.includes('rất xấu') || levelLower.includes('very bad')) {
            badgeClass = 'aqi-badge-very-bad';
        } else if (levelLower.includes('nguy hiểm') || levelLower.includes('dangerous') || levelLower.includes('nguy')) {
            badgeClass = 'aqi-badge-dangerous';
        } else {
            // Fallback to default color
            const defaultColor = color || '#60a5fa';
            return '<span class="aqi-badge" style="background-color: ' + defaultColor + '; color: white; border-color: ' + defaultColor + ';">' + level + '</span>';
        }
        
        return '<span class="aqi-badge ' + badgeClass + '">' + level + '</span>';
    }

    // Cập nhật metric cards
    function updateMetricCards(data) {
        if (!data || data.length === 0) return;

        const latest = data[0]; // Dữ liệu mới nhất

        // Temperature card
        const tempCard = document.querySelector('.metric-card h2.display-6');
        if (tempCard && latest.temperature != null) {
            tempCard.innerHTML = formatNumber(latest.temperature) + '<span class="fs-5 fw-semibold"> °C</span>';
        }

        // Humidity card
        const humidityCards = document.querySelectorAll('.metric-card');
        if (humidityCards.length >= 2 && latest.humidity != null) {
            const humidityCard = humidityCards[1].querySelector('h2.display-6');
            if (humidityCard) {
                humidityCard.innerHTML = formatNumber(latest.humidity) + '<span class="fs-5 fw-semibold"> %</span>';
            }
        }

        // Air Quality card
        if (humidityCards.length >= 3 && latest.dust != null) {
            const dustCard = humidityCards[2].querySelector('h2.display-6');
            if (dustCard) {
                dustCard.innerHTML = formatNumber(latest.dust) + '<span class="fs-5 fw-semibold"> µg/m³</span>';
            }
        }

        // AQI Index card
        if (humidityCards.length >= 4 && latest.aqi != null) {
            const aqiCard = humidityCards[3].querySelector('h2.display-6');
            if (aqiCard) {
                const aqiValueClass = getAQIValueClass(latest.aqi);
                const aqiLevel = latest.aqiLevel || 'N/A';
                aqiCard.innerHTML = '<span class="aqi-value-card ' + aqiValueClass + '">' + formatNumber(latest.aqi, 0) + '</span>' +
                                   '<span class="fs-6 fw-semibold ms-2">' + aqiLevel + '</span>';
            }
        }

        // WiFi Signal card
        if (humidityCards.length >= 5 && latest.wifiSignal != null) {
            const wifiCard = humidityCards[4].querySelector('h2.display-6');
            if (wifiCard) {
                wifiCard.innerHTML = latest.wifiSignal + '<span class="fs-5 fw-semibold"> dBm</span>';
            }
        }
    }

    // Cập nhật bảng dữ liệu
    function updateTable(data) {
        const tbody = document.querySelector('table tbody');
        if (!tbody || !data || data.length === 0) return;

        // Giới hạn 10 bản ghi mới nhất
        const recentData = data.slice(0, 10);
        tbody.innerHTML = '';

        recentData.forEach((item, index) => {
            const row = document.createElement('tr');
            row.className = 'fade-in';
            row.innerHTML = 
                '<td>' + (index + 1) + '</td>' +
                '<td><span class="badge bg-primary bg-opacity-25 text-primary">#' + (item.deviceId || 'N/A') + '</span></td>' +
                '<td>' + formatNumber(item.temperature, 2) + '</td>' +
                '<td>' + formatNumber(item.humidity, 2) + '</td>' +
                '<td>' + formatNumber(item.mq1, 0) + '</td>' +
                '<td>' + formatNumber(item.mq2, 0) + '</td>' +
                '<td>' + formatNumber(item.mq3, 0) + '</td>' +
                '<td>' + formatNumber(item.dust, 2) + '</td>' +
                '<td>' + (item.aqi != null ? '<span class="aqi-value ' + getAQIValueClass(item.aqi) + '">' + formatNumber(item.aqi, 0) + '</span>' : 'N/A') + '</td>' +
                '<td>' + (item.aqiLevel != null ? getAQIBadgeHTML(item.aqiLevel, item.aqiColor) : 'N/A') + '</td>' +
                '<td>' + (item.wifiSignal || 0) + '</td>' +
                '<td>' + (item.uptime || 0) + '</td>' +
                '<td>' + (item.timestamp || 'N/A') + '</td>';
            tbody.appendChild(row);
        });
    }

    // Cập nhật biểu đồ
    function updateCharts(data) {
        if (!data || data.length === 0) return;

        // Lấy 10 bản ghi gần nhất (đảo ngược để hiển thị từ cũ đến mới)
        const chartData = data.slice(0, 10).reverse();

        // Chuẩn bị dữ liệu cho biểu đồ
        const timestamps = chartData.map(item => {
            if (!item.timestamp) return '';
            const date = new Date(item.timestamp);
            return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        });
        const temps = chartData.map(item => item.temperature || 0);
        const humidities = chartData.map(item => item.humidity || 0);
        const mq1Values = chartData.map(item => item.mq1 || 0);
        const mq2Values = chartData.map(item => item.mq2 || 0);
        const mq3Values = chartData.map(item => item.mq3 || 0);

        // Cập nhật biểu đồ nhiệt độ & độ ẩm
        if (climateChart) {
            climateChart.data.labels = timestamps;
            climateChart.data.datasets[0].data = temps;
            climateChart.data.datasets[1].data = humidities;
            climateChart.update('none'); // 'none' mode để không có animation
        }

        // Cập nhật biểu đồ radar MQ
        if (mqChart) {
            const latest = data[0];
            const avgMq1 = mq1Values.reduce((a, b) => a + b, 0) / mq1Values.length || 0;
            const avgMq2 = mq2Values.reduce((a, b) => a + b, 0) / mq2Values.length || 0;
            const avgMq3 = mq3Values.reduce((a, b) => a + b, 0) / mq3Values.length || 0;

            mqChart.data.datasets[0].data = [
                latest.mq1 || 0,
                latest.mq2 || 0,
                latest.mq3 || 0
            ];
            mqChart.data.datasets[1].data = [avgMq1, avgMq2, avgMq3];
            mqChart.update('none');
        }
    }

    // Fetch dữ liệu từ API
    async function fetchDashboardData() {
        try {
            console.log('[Dashboard] Fetching data...');
            showRefreshIndicator();

            const urlParams = new URLSearchParams(window.location.search);
            const deviceId = urlParams.get('deviceId') || 'DEVICE001';
            const timestamp = new Date().getTime();
            
            // Đảm bảo sử dụng đúng path (có thể có contextPath)
            let apiPath = window.location.pathname;
            const apiUrl = apiPath + '?deviceId=' + encodeURIComponent(deviceId) + '&format=json&_t=' + timestamp;
            
            console.log('[Dashboard] Fetching from:', apiUrl);

            const response = await fetch(apiUrl, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Cache-Control': 'no-cache',
                    'Pragma': 'no-cache'
                },
                credentials: 'same-origin' // Đảm bảo gửi cookies/session
            });

            console.log('[Dashboard] Response status:', response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('[Dashboard] HTTP error response:', errorText);
                throw new Error('HTTP error! status: ' + response.status + ', body: ' + errorText);
            }

            const result = await response.json();
            console.log('[Dashboard] Received data:', result);

            if (result.success && result.data && result.data.length > 0) {
                console.log('[Dashboard] Updating UI with', result.data.length, 'records');
                
                // Cập nhật UI với dữ liệu mới
                updateMetricCards(result.data);
                updateTable(result.data);
                updateCharts(result.data);

                // Cập nhật timestamp
                if (result.data[0].timestamp) {
                    updateLastUpdateTime(result.data[0].timestamp);
                }

                // Ẩn thông báo "No data"
                const noDataDiv = document.querySelector('.text-center.py-5');
                if (noDataDiv) {
                    noDataDiv.style.display = 'none';
                }

                // Hiển thị các phần tử dữ liệu nếu chúng bị ẩn
                const dataSections = document.querySelectorAll('[id="data-sections"]');
                document.querySelectorAll('.row.g-4, .glass-card').forEach(el => {
                    el.style.display = '';
                });
                
                console.log('[Dashboard] UI updated successfully');
            } else {
                console.warn('[Dashboard] No data available or invalid response:', result);
            }
        } catch (error) {
            console.error('[Dashboard] Error fetching dashboard data:', error);
            console.error('[Dashboard] Error details:', error.message, error.stack);
        } finally {
            hideRefreshIndicator();
        }
    }

    // Khởi tạo charts và lưu tham chiếu
    function initializeCharts() {
        // Lấy dữ liệu ban đầu từ JSP hoặc khởi tạo với mảng rỗng
        let timestamps = [];
        let temps = [];
        let humidities = [];
        let mq1 = [];
        let mq2 = [];
        let mq3 = [];

        <c:if test="${not empty sensorData}">
        timestamps = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">
                "<fmt:formatDate value='${item.timestampAsDate}' pattern='HH:mm:ss'/>'"<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        ].reverse();
        temps = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.temperature}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        humidities = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.humidity}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        mq1 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq1}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        mq2 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq2}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        mq3 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq3}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        </c:if>

        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            animation: false, // Tắt animation để update mượt hơn
            plugins: {
                legend: {
                    labels: { color: '#e2e8f0' }
                }
            },
            scales: {
                x: {
                    ticks: { color: '#94a3b8' },
                    grid: { color: 'rgba(148, 163, 184, 0.15)' }
                },
                y: {
                    ticks: { color: '#94a3b8' },
                    grid: { color: 'rgba(148, 163, 184, 0.12)' }
                }
            }
        };

        // Khởi tạo biểu đồ nhiệt độ & độ ẩm
        const climateCtx = document.getElementById('climateChart');
        if (climateCtx && !climateChart) {
            climateChart = new Chart(climateCtx, {
                type: 'line',
                data: {
                    labels: timestamps.length > 0 ? timestamps : [''],
                    datasets: [
                        {
                            label: 'Temperature (°C)',
                            data: temps.length > 0 ? temps : [0],
                            borderColor: '#f97316',
                            backgroundColor: 'rgba(249, 115, 22, 0.35)',
                            tension: 0.4,
                            fill: true
                        },
                        {
                            label: 'Humidity (%)',
                            data: humidities.length > 0 ? humidities : [0],
                            borderColor: '#38bdf8',
                            backgroundColor: 'rgba(56, 189, 248, 0.35)',
                            tension: 0.4,
                            fill: true
                        }
                    ]
                },
                options: chartOptions
            });
        }

        // Khởi tạo biểu đồ radar MQ
        const mqCtx = document.getElementById('mqChart');
        if (mqCtx && !mqChart) {
            const latestMq1 = mq1.length > 0 ? mq1[mq1.length - 1] : 0;
            const latestMq2 = mq2.length > 0 ? mq2[mq2.length - 1] : 0;
            const latestMq3 = mq3.length > 0 ? mq3[mq3.length - 1] : 0;
            const avgMq1 = mq1.length > 0 ? mq1.reduce((a, b) => a + b, 0) / mq1.length : 0;
            const avgMq2 = mq2.length > 0 ? mq2.reduce((a, b) => a + b, 0) / mq2.length : 0;
            const avgMq3 = mq3.length > 0 ? mq3.reduce((a, b) => a + b, 0) / mq3.length : 0;

            mqChart = new Chart(mqCtx, {
                type: 'radar',
                data: {
                    labels: ['MQ1', 'MQ2', 'MQ3'],
                    datasets: [
                        {
                            label: 'Latest Reading',
                            data: [latestMq1, latestMq2, latestMq3],
                            borderColor: '#a855f7',
                            backgroundColor: 'rgba(168, 85, 247, 0.35)',
                            borderWidth: 2
                        },
                        {
                            label: 'Average (last 10)',
                            data: [avgMq1, avgMq2, avgMq3],
                            borderColor: '#22d3ee',
                            backgroundColor: 'rgba(34, 211, 238, 0.25)',
                            borderWidth: 2
                        }
                    ]
                },
                options: {
                    responsive: true,
                    animation: false,
                    plugins: {
                        legend: {
                            labels: { color: '#e2e8f0' }
                        }
                    },
                    scales: {
                        r: {
                            angleLines: { color: 'rgba(148, 163, 184, 0.2)' },
                            grid: { color: 'rgba(148, 163, 184, 0.2)' },
                            pointLabels: { color: '#e2e8f0' },
                            ticks: { color: '#94a3b8', backdropColor: 'transparent' }
                        }
                    }
                }
            });
        }
    }

    // Bắt đầu real-time updates
    function startRealTimeUpdates() {
        console.log('[Dashboard] Starting real-time updates...');
        
        // Kiểm tra xem Chart.js đã load chưa
        if (typeof Chart === 'undefined') {
            console.error('[Dashboard] Chart.js not loaded! Waiting...');
            setTimeout(startRealTimeUpdates, 100);
            return;
        }
        
        // Khởi tạo charts
        try {
            initializeCharts();
            console.log('[Dashboard] Charts initialized');
        } catch (error) {
            console.error('[Dashboard] Error initializing charts:', error);
        }

        // Fetch dữ liệu lần đầu sau một chút delay để đảm bảo DOM đã sẵn sàng
        setTimeout(function() {
            console.log('[Dashboard] Starting first fetch...');
            fetchDashboardData();
        }, 500);

        // Set interval để fetch dữ liệu định kỳ
        if (refreshTimer) {
            clearInterval(refreshTimer);
        }
        refreshTimer = setInterval(function() {
            console.log('[Dashboard] Scheduled fetch triggered');
            fetchDashboardData();
        }, REFRESH_INTERVAL);

        console.log('[Dashboard] Real-time updates enabled: Dashboard will update every ' + (REFRESH_INTERVAL/1000) + ' seconds');
    }

    // Dừng updates khi user rời khỏi trang
    window.addEventListener('beforeunload', function() {
        console.log('[Dashboard] Page unloading, stopping updates');
        if (refreshTimer) {
            clearInterval(refreshTimer);
            refreshTimer = null;
        }
    });

    // Khởi động khi trang load xong - sử dụng nhiều phương pháp để đảm bảo
    function initDashboard() {
        if (document.readyState === 'loading') {
            console.log('[Dashboard] Document still loading, waiting for DOMContentLoaded');
            document.addEventListener('DOMContentLoaded', function() {
                console.log('[Dashboard] DOMContentLoaded fired');
                startRealTimeUpdates();
            });
        } else {
            console.log('[Dashboard] Document already loaded, starting immediately');
            // Đảm bảo DOM đã sẵn sàng
            if (document.body) {
                startRealTimeUpdates();
            } else {
                setTimeout(startRealTimeUpdates, 100);
            }
        }
    }

    // Bắt đầu khởi tạo
    initDashboard();
</script>
</body>
</html>

