<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Threshold Management - IoT Web App</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            max-width: 600px;
            width: 100%;
        }
        
        h1 {
            color: #333;
            margin-bottom: 10px;
            text-align: center;
            font-size: 28px;
        }
        
        .subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 20px;
            font-size: 14px;
        }
        
        .user-info {
            text-align: center;
            margin-bottom: 20px;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 8px;
        }
        
        .user-info span {
            color: #666;
            margin-right: 10px;
        }
        
        .btn-logout {
            display: inline-block;
            padding: 6px 15px;
            background: #dc3545;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-size: 13px;
            transition: all 0.3s ease;
        }
        
        .btn-logout:hover {
            background: #c82333;
            transform: translateY(-1px);
        }
        
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-weight: 500;
            text-align: center;
        }
        
        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .form-group {
            margin-bottom: 25px;
        }
        
        label {
            display: block;
            color: #333;
            font-weight: 600;
            margin-bottom: 8px;
            font-size: 14px;
        }
        
        .label-description {
            color: #666;
            font-weight: 400;
            font-size: 12px;
            margin-left: 5px;
        }
        
        input[type="number"], input[type="hidden"] {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s ease;
        }
        
        input[type="number"]:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .button-group {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }
        
        button {
            flex: 1;
            padding: 14px 25px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(108, 117, 125, 0.4);
        }
        
        .current-values {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        
        .current-values h3 {
            color: #333;
            margin-bottom: 15px;
            font-size: 18px;
        }
        
        .value-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
        }
        
        .value-item {
            display: flex;
            flex-direction: column;
        }
        
        .value-label {
            color: #666;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            margin-bottom: 5px;
        }
        
        .value-number {
            color: #667eea;
            font-size: 20px;
            font-weight: 700;
        }
        
        @media (max-width: 600px) {
            .container {
                padding: 25px;
            }
            
            .button-group {
                flex-direction: column;
            }
            
            h1 {
                font-size: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>⚙️ Threshold Management</h1>
        <p class="subtitle">Configure sensor alert thresholds for Device ID: ${deviceId}</p>
        
        <!-- User info and logout -->
        <div class="user-info">
            <span>👤 ${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Đăng xuất</a>
        </div>
        
        <!-- Display success/error messages -->
        <c:if test="${param.message == 'success'}">
            <div class="alert alert-success">
                ✓ Thresholds updated successfully!
            </div>
        </c:if>
        
        <c:if test="${param.message == 'error'}">
            <div class="alert alert-error">
                ✗ Error updating thresholds. Please check your input values.
            </div>
        </c:if>
        
        <!-- Display current values -->
        <div class="current-values">
            <h3>Current Threshold Values</h3>
            <div class="value-grid">
                <div class="value-item">
                    <span class="value-label">MQ-135 (MQ1)</span>
                    <span class="value-number">${thresholds['mq1'] != null ? thresholds['mq1'] : 300.0}</span>
                </div>
                <div class="value-item">
                    <span class="value-label">MQ-7 (MQ2)</span>
                    <span class="value-number">${thresholds['mq2'] != null ? thresholds['mq2'] : 300.0}</span>
                </div>
                <div class="value-item">
                    <span class="value-label">MQ-2 (MQ3)</span>
                    <span class="value-number">${thresholds['mq3'] != null ? thresholds['mq3'] : 600.0}</span>
                </div>
                <div class="value-item">
                    <span class="value-label">Temperature</span>
                    <span class="value-number">${thresholds['temperature'] != null ? thresholds['temperature'] : 30.0}°C</span>
                </div>
                <div class="value-item">
                    <span class="value-label">Dust</span>
                    <span class="value-number">${thresholds['dust'] != null ? thresholds['dust'] : 50.0} µg/m³</span>
                </div>
            </div>
        </div>
        
        <!-- Threshold update form -->
        <form method="post" action="${pageContext.request.contextPath}/admin/thresholds">
            <input type="hidden" name="deviceId" value="${deviceId}">
            
            <div class="form-group">
                <label for="mq1">
                    MQ-135 Sensor (MQ1)
                    <span class="label-description">- Air Quality (ppm)</span>
                </label>
                <input type="number" 
                       id="mq1" 
                       name="mq1" 
                       step="0.1" 
                       min="0" 
                       value="${thresholds['mq1'] != null ? thresholds['mq1'] : 300.0}" 
                       required>
            </div>
            
            <div class="form-group">
                <label for="mq2">
                    MQ-7 Sensor (MQ2)
                    <span class="label-description">- Carbon Monoxide (ppm)</span>
                </label>
                <input type="number" 
                       id="mq2" 
                       name="mq2" 
                       step="0.1" 
                       min="0" 
                       value="${thresholds['mq2'] != null ? thresholds['mq2'] : 300.0}" 
                       required>
            </div>
            
            <div class="form-group">
                <label for="mq3">
                    MQ-2 Sensor (MQ3)
                    <span class="label-description">- Smoke/Gas (ppm)</span>
                </label>
                <input type="number" 
                       id="mq3" 
                       name="mq3" 
                       step="0.1" 
                       min="0" 
                       value="${thresholds['mq3'] != null ? thresholds['mq3'] : 600.0}" 
                       required>
            </div>
            
            <div class="form-group">
                <label for="temperature">
                    Temperature Limit
                    <span class="label-description">- Celsius (°C)</span>
                </label>
                <input type="number" 
                       id="temperature" 
                       name="temperature" 
                       step="0.1" 
                       min="-50" 
                       max="100" 
                       value="${thresholds['temperature'] != null ? thresholds['temperature'] : 30.0}" 
                       required>
            </div>
            
            <div class="form-group">
                <label for="dust">
                    Dust Particulate Limit
                    <span class="label-description">- PM2.5 (µg/m³)</span>
                </label>
                <input type="number" 
                       id="dust" 
                       name="dust" 
                       step="0.1" 
                       min="0" 
                       value="${thresholds['dust'] != null ? thresholds['dust'] : 50.0}" 
                       required>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn-primary">💾 Save Changes</button>
                <button type="button" class="btn-secondary" onclick="window.location.href='${pageContext.request.contextPath}/'">
                    ← Back to Dashboard
                </button>
            </div>
        </form>
    </div>
</body>
</html>
