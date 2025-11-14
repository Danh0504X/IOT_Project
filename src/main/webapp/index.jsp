<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IoT Web Application</title>
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
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 60px 40px;
            text-align: center;
            max-width: 600px;
            width: 100%;
        }
        h1 {
            color: #333;
            font-size: 2.5em;
            margin-bottom: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        p {
            color: #666;
            font-size: 1.1em;
            margin-bottom: 40px;
            line-height: 1.6;
        }
        .buttons {
            display: flex;
            gap: 20px;
            justify-content: center;
            flex-wrap: wrap;
        }
        .btn {
            padding: 15px 30px;
            text-decoration: none;
            border-radius: 10px;
            font-size: 1.1em;
            font-weight: 600;
            transition: all 0.3s ease;
            display: inline-block;
            cursor: pointer;
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }
        .btn-secondary {
            background: white;
            color: #667eea;
            border: 2px solid #667eea;
        }
        .btn-secondary:hover {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
        }
        .features {
            margin-top: 40px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
        }
        .feature {
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
            transition: transform 0.3s ease;
        }
        .feature:hover {
            transform: translateY(-5px);
        }
        .feature h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        .feature p {
            font-size: 0.9em;
            margin: 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌐 IoT Web Application</h1>
        <p>Hệ thống giám sát và quản lý dữ liệu cảm biến IoT thời gian thực</p>
        
        <div class="buttons">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-primary">
                📊 Xem Dashboard
            </a>
            <a href="<%= request.getContextPath() %>/admin/thresholds" class="btn btn-secondary">
                ⚙️ Quản lý Ngưỡng
            </a>
        </div>
        
        <div class="features">
            <div class="feature">
                <h3>📈 Real-time</h3>
                <p>Giám sát dữ liệu cảm biến theo thời gian thực</p>
            </div>
            <div class="feature">
                <h3>📊 Biểu đồ</h3>
                <p>Trực quan hóa dữ liệu bằng biểu đồ động</p>
            </div>
            <div class="feature">
                <h3>⚙️ Cấu hình</h3>
                <p>Quản lý ngưỡng cảnh báo dễ dàng</p>
            </div>
        </div>
    </div>
</body>
</html>
