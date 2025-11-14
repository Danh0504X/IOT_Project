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

            <!-- Display current values -->
            <div class="current-values-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="mb-0"><i class="bi bi-info-circle me-2"></i>Giá trị ngưỡng hiện tại</h5>
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
                            <span class="value-label"><i class="bi bi-cloud-dust me-1"></i>Dust</span>
                            <div class="value-number">${thresholds['dust'] != null ? thresholds['dust'] : 50.0}</div>
                            <small class="text-secondary text-opacity-75">µg/m³</small>
                        </div>
                    </div>
                </div>
            </div>

            <div class="divider"></div>

            <!-- Threshold update form -->
            <form method="post" action="${pageContext.request.contextPath}/admin/thresholds">
                <input type="hidden" name="deviceId" value="${deviceId}">
                
                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="mq1">
                                <i class="bi bi-wind me-1"></i>MQ-135 Sensor (MQ1)
                                <span class="label-description">- Air Quality (ppm)</span>
                            </label>
                            <input type="number" 
                                   class="form-control" 
                                   id="mq1" 
                                   name="mq1" 
                                   step="0.1" 
                                   min="0" 
                                   value="${thresholds['mq1'] != null ? thresholds['mq1'] : 300.0}" 
                                   placeholder="Nhập giá trị ngưỡng MQ1"
                                   required>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="mq2">
                                <i class="bi bi-wind me-1"></i>MQ-7 Sensor (MQ2)
                                <span class="label-description">- Carbon Monoxide (ppm)</span>
                            </label>
                            <input type="number" 
                                   class="form-control" 
                                   id="mq2" 
                                   name="mq2" 
                                   step="0.1" 
                                   min="0" 
                                   value="${thresholds['mq2'] != null ? thresholds['mq2'] : 300.0}" 
                                   placeholder="Nhập giá trị ngưỡng MQ2"
                                   required>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="mq3">
                                <i class="bi bi-wind me-1"></i>MQ-2 Sensor (MQ3)
                                <span class="label-description">- Smoke/Gas (ppm)</span>
                            </label>
                            <input type="number" 
                                   class="form-control" 
                                   id="mq3" 
                                   name="mq3" 
                                   step="0.1" 
                                   min="0" 
                                   value="${thresholds['mq3'] != null ? thresholds['mq3'] : 600.0}" 
                                   placeholder="Nhập giá trị ngưỡng MQ3"
                                   required>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="temperature">
                                <i class="bi bi-thermometer-half me-1"></i>Temperature Limit
                                <span class="label-description">- Celsius (°C)</span>
                            </label>
                            <input type="number" 
                                   class="form-control" 
                                   id="temperature" 
                                   name="temperature" 
                                   step="0.1" 
                                   min="-50" 
                                   max="100" 
                                   value="${thresholds['temperature'] != null ? thresholds['temperature'] : 30.0}" 
                                   placeholder="Nhập nhiệt độ giới hạn"
                                   required>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="dust">
                                <i class="bi bi-cloud-dust me-1"></i>Dust Particulate Limit
                                <span class="label-description">- PM2.5 (µg/m³)</span>
                            </label>
                            <input type="number" 
                                   class="form-control" 
                                   id="dust" 
                                   name="dust" 
                                   step="0.1" 
                                   min="0" 
                                   value="${thresholds['dust'] != null ? thresholds['dust'] : 50.0}" 
                                   placeholder="Nhập ngưỡng bụi mịn"
                                   required>
                        </div>
                    </div>
                </div>

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
</body>
</html>
