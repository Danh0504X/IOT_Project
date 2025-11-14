<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-box-arrow-right"></i> Đăng xuất
                        </a>
                    </div>
                    <p class="text-secondary text-opacity-75 mb-1">Last update</p>
                    <h5 class="mb-0">
                        <c:choose>
                            <c:when test="${not empty sensorData and not empty sensorData[0].timestamp}">
                                <fmt:formatDate value="${sensorData[0].timestampAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </c:when>
                            <c:otherwise>N/A</c:otherwise>
                        </c:choose>
                    </h5>
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
                <div class="col-md-3">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Temperature</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].temperature}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> °C</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-thermometer-half me-1"></i>Thiết bị #${sensorData[0].deviceId}</span>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Humidity</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].humidity}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> %</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-droplet-half me-1"></i>Độ ẩm hiện tại</span>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="metric-card h-100">
                        <span class="text-uppercase text-secondary text-opacity-75 small">Air Quality</span>
                        <h2 class="display-6 fw-bold mt-2">
                            <fmt:formatNumber value="${sensorData[0].dust}" maxFractionDigits="1"/><span class="fs-5 fw-semibold"> µg/m³</span>
                        </h2>
                        <span class="text-secondary text-opacity-75"><i class="bi bi-wind me-1"></i>Bụi mịn</span>
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
                            <th>MQ1</th>
                            <th>MQ2</th>
                            <th>MQ3</th>
                            <th>Dust (µg/m³)</th>
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
<c:if test="${not empty sensorData}">
    <script>
        const timestamps = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">
                "<fmt:formatDate value='${item.timestampAsDate}' pattern='HH:mm:ss'/>'"<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        ].reverse();
        const temps = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.temperature}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        const humidities = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.humidity}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        const mq1 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq1}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        const mq2 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq2}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();
        const mq3 = [
            <c:forEach var="item" items="${sensorData}" varStatus="loop">${item.mq3}<c:if test="${!loop.last}">,</c:if></c:forEach>
        ].reverse();

        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
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

        const climateCtx = document.getElementById('climateChart');
        if (climateCtx) {
            new Chart(climateCtx, {
                type: 'line',
                data: {
                    labels: timestamps,
                    datasets: [
                        {
                            label: 'Temperature (°C)',
                            data: temps,
                            borderColor: '#f97316',
                            backgroundColor: 'rgba(249, 115, 22, 0.35)',
                            tension: 0.4,
                            fill: true
                        },
                        {
                            label: 'Humidity (%)',
                            data: humidities,
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

        const mqCtx = document.getElementById('mqChart');
        if (mqCtx) {
            new Chart(mqCtx, {
                type: 'radar',
                data: {
                    labels: ['MQ1', 'MQ2', 'MQ3'],
                    datasets: [
                        {
                            label: 'Latest Reading',
                            data: [mq1[mq1.length - 1], mq2[mq2.length - 1], mq3[mq3.length - 1]],
                            borderColor: '#a855f7',
                            backgroundColor: 'rgba(168, 85, 247, 0.35)',
                            borderWidth: 2
                        },
                        {
                            label: 'Average (last 10)',
                            data: [
                                mq1.reduce((a, b) => a + b, 0) / mq1.length,
                                mq2.reduce((a, b) => a + b, 0) / mq2.length,
                                mq3.reduce((a, b) => a + b, 0) / mq3.length
                            ],
                            borderColor: '#22d3ee',
                            backgroundColor: 'rgba(34, 211, 238, 0.25)',
                            borderWidth: 2
                        }
                    ]
                },
                options: {
                    responsive: true,
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
    </script>
</c:if>

<!-- Auto-refresh script: Tự động reload dashboard mỗi 5 giây để cập nhật dữ liệu real-time từ ESP32 -->
<script>
    // Auto-refresh dashboard mỗi 5 giây (5000ms)
    const REFRESH_INTERVAL = 5000; // 5 giây
    
    // Cập nhật "Last update" timestamp
    function updateLastUpdateTime() {
        const now = new Date();
        const timeString = now.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        
        // Tìm element "Last update" và cập nhật
        const lastUpdateElements = document.querySelectorAll('*');
        for (let elem of lastUpdateElements) {
            if (elem.textContent && elem.textContent.includes('Last update')) {
                const parent = elem.closest('.d-flex, .text-end, span');
                if (parent) {
                    // Tìm phần N/A hoặc timestamp cũ và thay thế
                    const text = parent.textContent;
                    if (text.includes('N/A') || text.match(/\d{4}-\d{2}-\d{2}/)) {
                        parent.innerHTML = parent.innerHTML.replace(
                            /(Last update[:\s]*)(N/A|[\d\s:-]+)/i,
                            `Last update: ${timeString}`
                        );
                    }
                }
            }
        }
    }
    
    // Thêm indicator khi đang refresh
    let refreshIndicator = null;
    function showRefreshIndicator() {
        if (!refreshIndicator) {
            refreshIndicator = document.createElement('div');
            refreshIndicator.id = 'refresh-indicator';
            refreshIndicator.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Đang cập nhật...';
            refreshIndicator.style.cssText = 'position:fixed;top:20px;right:20px;background:rgba(59,130,246,0.9);color:white;padding:10px 20px;border-radius:8px;z-index:9999;font-size:14px;';
            document.body.appendChild(refreshIndicator);
        }
        refreshIndicator.style.display = 'block';
    }
    
    function hideRefreshIndicator() {
        if (refreshIndicator) {
            refreshIndicator.style.display = 'none';
        }
    }
    
    // CSS cho spinner animation
    if (!document.getElementById('refresh-styles')) {
        const style = document.createElement('style');
        style.id = 'refresh-styles';
        style.textContent = `
            @keyframes spin {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
            }
            .spin {
                animation: spin 1s linear infinite;
                display: inline-block;
            }
        `;
        document.head.appendChild(style);
    }
    
    // Bắt đầu auto-refresh
    let refreshTimer = null;
    
    function startAutoRefresh() {
        // Cập nhật timestamp lần đầu
        updateLastUpdateTime();
        
        // Set interval để tự động reload
        refreshTimer = setInterval(function() {
            showRefreshIndicator();
            
            // Reload trang sau 500ms để hiển thị indicator
            setTimeout(function() {
                // Lấy deviceId từ URL hoặc từ form
                const urlParams = new URLSearchParams(window.location.search);
                const deviceId = urlParams.get('deviceId') || 'DEVICE001';
                
                // Reload với deviceId
                window.location.href = window.location.pathname + '?deviceId=' + deviceId + '&_t=' + new Date().getTime();
            }, 300);
        }, REFRESH_INTERVAL);
    }
    
    // Dừng auto-refresh khi user rời khỏi trang
    window.addEventListener('beforeunload', function() {
        if (refreshTimer) {
            clearInterval(refreshTimer);
        }
    });
    
    // Bắt đầu auto-refresh khi trang load xong
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', startAutoRefresh);
    } else {
        startAutoRefresh();
    }
    
    // Cập nhật timestamp mỗi giây
    setInterval(updateLastUpdateTime, 1000);
    
    console.log('Auto-refresh enabled: Dashboard will refresh every ' + (REFRESH_INTERVAL/1000) + ' seconds');
</script>
</body>
</html>

