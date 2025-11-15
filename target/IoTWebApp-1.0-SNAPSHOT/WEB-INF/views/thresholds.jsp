<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Threshold Management - IoT Sensor Intelligence</title>
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
            padding-bottom: 1.5rem;
        }
        .badge-soft {
            background: linear-gradient(135deg, rgba(59,130,246,0.25), rgba(14,165,233,0.25));
            color: #bae6fd;
            border: 1px solid rgba(59, 130, 246, 0.3);
        }
        .current-values-card {
            background: linear-gradient(135deg, rgba(59,130,246,0.15), rgba(14,165,233,0.1));
            border-radius: 16px;
            border: 1px solid rgba(148, 163, 184, 0.2);
            padding: 1.5rem;
            margin-bottom: 2rem;
        }
        .value-item {
            background: rgba(15, 23, 42, 0.5);
            border-radius: 12px;
            padding: 1rem;
            border: 1px solid rgba(148, 163, 184, 0.15);
            transition: all 0.3s ease;
        }
        .value-item:hover {
            background: rgba(15, 23, 42, 0.7);
            border-color: rgba(59, 130, 246, 0.4);
            transform: translateY(-2px);
        }
        .value-label {
            color: #94a3b8;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 0.5rem;
        }
        .value-number {
            color: #60a5fa;
            font-size: 1.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, #60a5fa, #38bdf8);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .form-group {
            margin-bottom: 1.5rem;
        }
        .form-group label {
            color: #e2e8f0;
            font-weight: 600;
            margin-bottom: 0.75rem;
            display: block;
            font-size: 0.95rem;
        }
        .label-description {
            color: #94a3b8;
            font-weight: 400;
            font-size: 0.85rem;
            margin-left: 0.5rem;
        }
        .form-control, .form-control:focus {
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(148, 163, 184, 0.3);
            border-radius: 12px;
            color: #e2e8f0;
            padding: 0.75rem 1rem;
            transition: all 0.3s ease;
        }
        .form-control:focus {
            background: rgba(15, 23, 42, 0.8);
            border-color: rgba(59, 130, 246, 0.6);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
            color: #e2e8f0;
        }
        .form-control::placeholder {
            color: #64748b;
        }
        .btn-primary {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            border: none;
            border-radius: 12px;
            padding: 0.75rem 1.5rem;
            font-weight: 600;
            color: white;
            transition: all 0.3s ease;
            box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
        }
        .btn-primary:hover {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
            color: white;
        }
        .btn-secondary {
            background: rgba(108, 117, 125, 0.3);
            border: 1px solid rgba(148, 163, 184, 0.3);
            border-radius: 12px;
            padding: 0.75rem 1.5rem;
            font-weight: 600;
            color: #e2e8f0;
            transition: all 0.3s ease;
        }
        .btn-secondary:hover {
            background: rgba(108, 117, 125, 0.5);
            border-color: rgba(148, 163, 184, 0.5);
            transform: translateY(-2px);
            color: #e2e8f0;
        }
        .alert {
            border-radius: 12px;
            padding: 1rem 1.5rem;
            margin-bottom: 1.5rem;
            border: 1px solid;
            font-weight: 500;
        }
        .alert-success {
            background: rgba(16, 185, 129, 0.15);
            border-color: rgba(16, 185, 129, 0.3);
            color: #6ee7b7;
        }
        .alert-error {
            background: rgba(239, 68, 68, 0.15);
            border-color: rgba(239, 68, 68, 0.3);
            color: #fca5a5;
        }
        .divider {
            height: 1px;
            background: linear-gradient(90deg, rgba(148, 163, 184, 0), rgba(148, 163, 184, 0.35), rgba(148, 163, 184, 0));
            margin: 2rem 0;
        }
        .btn-outline-primary {
            border: 1px solid rgba(59, 130, 246, 0.5);
            color: #60a5fa;
            background: transparent;
            border-radius: 8px;
            padding: 0.5rem 1rem;
            font-size: 0.875rem;
            transition: all 0.3s ease;
        }
        .btn-outline-primary:hover {
            background: rgba(59, 130, 246, 0.2);
            border-color: rgba(59, 130, 246, 0.7);
            color: #93c5fd;
        }
        .btn-outline-danger {
            border: 1px solid rgba(239, 68, 68, 0.5);
            color: #f87171;
            background: transparent;
            border-radius: 8px;
            padding: 0.5rem 1rem;
            font-size: 0.875rem;
            transition: all 0.3s ease;
        }
        .btn-outline-danger:hover {
            background: rgba(239, 68, 68, 0.2);
            border-color: rgba(239, 68, 68, 0.7);
            color: #fca5a5;
        }
        
        /* Multi-level threshold styles */
        .sensor-section {
            margin-bottom: 2rem;
        }
        
        .sensor-header {
            transition: all 0.3s ease;
        }
        
        .sensor-header:hover {
            transform: translateX(4px);
        }
        
        .level-item {
            transition: all 0.2s ease;
        }
        
        .level-item:hover {
            border-left-width: 4px !important;
            transform: translateX(2px);
        }
        
        .form-control-sm {
            font-size: 0.875rem;
        }
        
        .levels-container {
            max-height: 600px;
            overflow-y: auto;
        }
        
        .levels-container::-webkit-scrollbar {
            width: 8px;
        }
        
        .levels-container::-webkit-scrollbar-track {
            background: rgba(15, 23, 42, 0.3);
            border-radius: 4px;
        }
        
        .levels-container::-webkit-scrollbar-thumb {
            background: rgba(148, 163, 184, 0.4);
            border-radius: 4px;
        }
        
        .levels-container::-webkit-scrollbar-thumb:hover {
            background: rgba(148, 163, 184, 0.6);
        }
    </style>
</head>
<body>
    <div class="container py-5">
        <div class="glass-card p-4 p-lg-5">
            <div class="page-header">
                <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
                    <div>
                        <span class="badge badge-soft rounded-pill px-3 py-2 mb-2">⚙️ Configuration Center</span>
                        <h1 class="display-6 fw-bold mb-0 text-gradient">Threshold Management</h1>
                        <p class="text-secondary text-opacity-75 mb-0">Cấu hình ngưỡng cảnh báo cảm biến cho thiết bị: <span class="badge bg-primary bg-opacity-25 text-primary">${deviceId}</span></p>
                    </div>
                    <div class="text-end">
                        <div class="mb-2">
                            <span class="text-secondary text-opacity-75 me-2">👤 ${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username}</span>
                            <a href="${pageContext.request.contextPath}/dashboard?deviceId=${deviceId}" class="btn btn-sm btn-outline-primary me-2">
                                <i class="bi bi-speedometer2"></i> Dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger">
                                <i class="bi bi-box-arrow-right"></i> Đăng xuất
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Display success/error messages -->
            <c:if test="${param.message == 'success'}">
                <div class="alert alert-success">
                    <i class="bi bi-check-circle me-2"></i><strong>Thành công!</strong> Ngưỡng cảnh báo đã được cập nhật.
                </div>
            </c:if>

            <c:if test="${param.message == 'error'}">
                <div class="alert alert-error">
                    <i class="bi bi-exclamation-circle me-2"></i><strong>Lỗi!</strong> Không thể cập nhật ngưỡng cảnh báo. Vui lòng kiểm tra lại giá trị nhập vào.
                </div>
            </c:if>

            <!-- Display current values summary -->
            <div class="current-values-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="mb-0"><i class="bi bi-info-circle me-2"></i>Tổng quan ngưỡng cảnh báo</h5>
                </div>
                <div class="row g-3">
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-wind me-1"></i>MQ-135 (MQ1)</span>
                            <div class="value-number">${thresholds['mq1'] != null ? thresholds['mq1'] : 300.0}</div>
                            <small class="text-secondary text-opacity-75">ppm</small>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-wind me-1"></i>MQ-7 (MQ2)</span>
                            <div class="value-number">${thresholds['mq2'] != null ? thresholds['mq2'] : 300.0}</div>
                            <small class="text-secondary text-opacity-75">ppm</small>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-wind me-1"></i>MQ-2 (MQ3)</span>
                            <div class="value-number">${thresholds['mq3'] != null ? thresholds['mq3'] : 600.0}</div>
                            <small class="text-secondary text-opacity-75">ppm</small>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-thermometer-half me-1"></i>Temperature</span>
                            <div class="value-number">${thresholds['temperature'] != null ? thresholds['temperature'] : 30.0}</div>
                            <small class="text-secondary text-opacity-75">°C</small>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-moisture me-1"></i>Humidity</span>
                            <div class="value-number">${thresholds['humidity'] != null ? thresholds['humidity'] : 70.0}</div>
                            <small class="text-secondary text-opacity-75">%</small>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6">
                        <div class="value-item">
                            <span class="value-label"><i class="bi bi-cloud-dust me-1"></i>Dust</span>
                            <div class="value-number">${thresholds['dust'] != null ? thresholds['dust'] : 50.0}</div>
                            <small class="text-secondary text-opacity-75">µg/m³</small>
                        </div>
                    </div>
                </div>
            </div>

            <div class="divider"></div>

            <!-- Multi-Level Threshold Configuration Form -->
            <form method="post" action="${pageContext.request.contextPath}/admin/thresholds" id="thresholdForm">
                <input type="hidden" name="deviceId" value="${deviceId}">
                
                <div class="mb-4">
                    <h5 class="mb-3"><i class="bi bi-sliders me-2"></i>Cấu hình ngưỡng theo từng mức cảnh báo</h5>
                    <p class="text-secondary text-opacity-75 small mb-4">
                        Mỗi cảm biến có nhiều mức cảnh báo. Vui lòng cấu hình MinValue và MaxValue cho từng mức.
                    </p>
                </div>

                <c:set var="sensorConfigs" value="${allThresholdLevels}" />
                
                <!-- Debug: Check if allThresholdLevels is loaded -->
                <c:if test="${empty allThresholdLevels}">
                    <div class="alert alert-warning mb-4">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        <strong>Cảnh báo:</strong> Không thể tải dữ liệu ngưỡng từ server. 
                        <br><small>Vui lòng kiểm tra server logs. Service có thể không trả về dữ liệu.</small>
                        <br><small>Debug: allThresholdLevels = ${allThresholdLevels}</small>
                    </div>
                </c:if>
                
                <!-- Debug: Show what we have -->
                <c:if test="${not empty allThresholdLevels}">
                    <div class="alert alert-success mb-2" style="font-size: 0.85rem; padding: 0.5rem;">
                        <small>✓ Đã tải ${allThresholdLevels.size()} loại cảm biến. 
                        <c:forEach var="entry" items="${allThresholdLevels}">
                            ${entry.key}(${entry.value.size()} levels) 
                        </c:forEach>
                        </small>
                    </div>
                </c:if>
                
                <!-- Show info if no data exists in database (form will still display with defaults) -->
                <c:set var="hasAnyData" value="false" />
                <c:if test="${not empty allThresholdLevels}">
                    <c:forEach var="entry" items="${allThresholdLevels}">
                        <c:forEach var="threshold" items="${entry.value}">
                            <c:if test="${threshold.thresholdId != null}">
                                <c:set var="hasAnyData" value="true" />
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </c:if>
                <c:if test="${not hasAnyData and not empty allThresholdLevels}">
                    <div class="alert alert-info mb-4">
                        <i class="bi bi-info-circle me-2"></i>
                        <strong>Thông tin:</strong> Chưa có dữ liệu ngưỡng trong database. 
                        <br><small>Form hiển thị với các giá trị mặc định. Bạn có thể nhập và lưu để tạo dữ liệu mới. Sau khi lưu, ESP32 sẽ tự động nhận ngưỡng mới khi polling (mỗi 10 giây).</small>
                    </div>
                </c:if>
                
                <!-- Temperature - Always display (form always shows) -->
                <c:set var="tempLevels" value="${allThresholdLevels != null ? allThresholdLevels['temperature'] : null}" />
                <c:if test="${tempLevels != null and not empty tempLevels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(59,130,246,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(59,130,246,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-thermometer-half me-2"></i>Nhiệt độ (°C)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${tempLevels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(59,130,246,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="temperature_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="temperature_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <!-- Humidity -->
                <c:set var="humLevels" value="${allThresholdLevels != null ? allThresholdLevels['humidity'] : null}" />
                <c:if test="${humLevels != null and not empty humLevels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(14,165,233,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(14,165,233,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-moisture me-2"></i>Độ ẩm (%)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${humLevels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(14,165,233,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="humidity_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="humidity_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <!-- MQ1 (MQ135) -->
                <c:set var="mq1Levels" value="${allThresholdLevels != null ? allThresholdLevels['mq1'] : null}" />
                <c:if test="${mq1Levels != null and not empty mq1Levels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(16,185,129,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(16,185,129,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-wind me-2"></i>MQ-135 - Chất lượng không khí (ppm)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${mq1Levels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(16,185,129,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq1_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq1_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <!-- MQ2 (MQ7 - CO) -->
                <c:set var="mq2Levels" value="${allThresholdLevels != null ? allThresholdLevels['mq2'] : null}" />
                <c:if test="${mq2Levels != null and not empty mq2Levels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(239,68,68,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(239,68,68,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-wind me-2"></i>MQ-7 - Khí CO (ppm)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${mq2Levels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(239,68,68,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq2_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq2_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <!-- MQ3 (MQ2 - Gas/LPG) -->
                <c:set var="mq3Levels" value="${allThresholdLevels != null ? allThresholdLevels['mq3'] : null}" />
                <c:if test="${mq3Levels != null and not empty mq3Levels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(245,158,11,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(245,158,11,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-wind me-2"></i>MQ-2 - Gas/LPG (ppm)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${mq3Levels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(245,158,11,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq3_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="mq3_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <!-- Dust (PM2.5) -->
                <c:set var="dustLevels" value="${allThresholdLevels != null ? allThresholdLevels['dust'] : null}" />
                <c:if test="${dustLevels != null and not empty dustLevels}">
                        <div class="sensor-section mb-4">
                            <div class="sensor-header" style="background: rgba(168,85,247,0.15); border-radius: 12px; padding: 1rem; margin-bottom: 1rem; border: 1px solid rgba(168,85,247,0.3);">
                                <h6 class="mb-0">
                                    <i class="bi bi-cloud-dust me-2"></i>PM2.5 - Bụi mịn (µg/m³)
                                </h6>
                            </div>
                            <div class="levels-container" style="background: rgba(15, 23, 42, 0.4); border-radius: 12px; padding: 1.5rem; border: 1px solid rgba(148, 163, 184, 0.15);">
                                <c:forEach var="threshold" items="${dustLevels}" varStatus="status">
                                <div class="level-item mb-3" style="background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 1rem; border-left: 3px solid rgba(168,85,247,0.5);">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <span class="fw-semibold">${threshold.levelName}</span>
                                            <span class="badge bg-primary bg-opacity-25 text-primary ms-2" style="font-size: 0.7rem;">
                                                Alert Level: ${threshold.alertLevel}
                                            </span>
                                        </div>
                                        <small class="text-secondary text-opacity-75">${threshold.message}</small>
                                    </div>
                                    <div class="row g-2">
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Min Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="dust_${status.index}_minValue"
                                                   value="${threshold.minValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-5">
                                            <label class="small text-secondary text-opacity-75 mb-1">Max Value</label>
                                            <input type="number" 
                                                   class="form-control form-control-sm" 
                                                   name="dust_${status.index}_maxValue"
                                                   value="${threshold.maxValue}"
                                                   step="0.1"
                                                   min="0"
                                                   required>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <span class="badge bg-secondary bg-opacity-25 text-secondary w-100 text-center" style="padding: 0.5rem;">
                                                ${threshold.minValue} - ${threshold.maxValue}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                </c:forEach>
                            </div>
                        </div>
                </c:if>

                <div class="divider"></div>

                <div class="d-flex gap-3 justify-content-end">
                    <button type="button" class="btn btn-secondary" onclick="window.location.href='${pageContext.request.contextPath}/dashboard?deviceId=${deviceId}'">
                        <i class="bi bi-arrow-left me-1"></i>Quay lại Dashboard
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-save me-1"></i>Lưu thay đổi
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Form validation: Ensure minValue < maxValue for each level
        document.getElementById('thresholdForm').addEventListener('submit', function(e) {
            let isValid = true;
            const errorMessages = [];
            
            // Get all min/max input pairs
            const minInputs = document.querySelectorAll('input[name$="_minValue"]');
            const maxInputs = document.querySelectorAll('input[name$="_maxValue"]');
            
            for (let i = 0; i < minInputs.length; i++) {
                const minInput = minInputs[i];
                const maxInput = maxInputs[i];
                
                const minValue = parseFloat(minInput.value);
                const maxValue = parseFloat(maxInput.value);
                
                if (isNaN(minValue) || isNaN(maxValue)) {
                    continue; // Skip if empty (will be caught by HTML5 required)
                }
                
                if (minValue >= maxValue) {
                    isValid = false;
                    minInput.classList.add('border-danger');
                    maxInput.classList.add('border-danger');
                    errorMessages.push(`Mức "${minInput.closest('.level-item').querySelector('.fw-semibold').textContent}": MinValue (${minValue}) phải nhỏ hơn MaxValue (${maxValue})`);
                } else {
                    minInput.classList.remove('border-danger');
                    maxInput.classList.remove('border-danger');
                }
            }
            
            if (!isValid) {
                e.preventDefault();
                alert('Lỗi validation:\n\n' + errorMessages.join('\n') + '\n\nVui lòng kiểm tra lại các giá trị nhập vào.');
                return false;
            }
            
            return true;
        });
        
        // Real-time validation on input change
        document.querySelectorAll('input[name$="_minValue"], input[name$="_maxValue"]').forEach(function(input) {
            input.addEventListener('blur', function() {
                const name = this.name;
                const isMin = name.endsWith('_minValue');
                const baseName = name.replace(isMin ? '_minValue' : '_maxValue', '');
                const otherInput = document.querySelector('input[name="' + baseName + (isMin ? '_maxValue' : '_minValue') + '"]');
                
                if (otherInput && this.value && otherInput.value) {
                    const thisValue = parseFloat(this.value);
                    const otherValue = parseFloat(otherInput.value);
                    
                    if (isMin && thisValue >= otherValue) {
                        this.classList.add('border-danger');
                        otherInput.classList.add('border-danger');
                    } else if (!isMin && thisValue <= otherValue) {
                        this.classList.add('border-danger');
                        otherInput.classList.add('border-danger');
                    } else {
                        this.classList.remove('border-danger');
                        otherInput.classList.remove('border-danger');
                    }
                }
            });
        });
    </script>
</body>
</html>
