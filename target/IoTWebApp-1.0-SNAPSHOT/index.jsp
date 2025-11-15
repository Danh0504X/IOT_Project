<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IoT Sensor Intelligence</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        body {
            background: radial-gradient(circle at top left, #0f172a, #111827 55%, #020617);
            min-height: 100vh;
            color: #e2e8f0;
            font-family: "Inter", "Segoe UI", sans-serif;
            padding: 2rem;
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
            padding: 3rem;
            max-width: 900px;
            margin: 0 auto;
        }
        
        .page-header {
            text-align: center;
            margin-bottom: 3rem;
        }
        
        .badge-soft {
            background: linear-gradient(135deg, rgba(59,130,246,0.25), rgba(14,165,233,0.25));
            color: #bae6fd;
            border: 1px solid rgba(59, 130, 246, 0.3);
            padding: 0.5rem 1.25rem;
            border-radius: 50px;
            font-size: 0.9rem;
            font-weight: 600;
            display: inline-block;
            margin-bottom: 1.5rem;
        }
        
        .page-header h1 {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 1rem;
        }
        
        .page-header p {
            color: #94a3b8;
            font-size: 1.1rem;
            line-height: 1.6;
        }
        
        .buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            flex-wrap: wrap;
            margin-bottom: 3rem;
        }
        
        .btn {
            padding: 0.875rem 2rem;
            text-decoration: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            border: none;
            cursor: pointer;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
        }
        
        .btn-primary:hover {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
            color: white;
        }
        
        .btn-secondary {
            background: rgba(15, 23, 42, 0.6);
            color: #60a5fa;
            border: 1px solid rgba(59, 130, 246, 0.5);
        }
        
        .btn-secondary:hover {
            background: rgba(59, 130, 246, 0.2);
            border-color: rgba(59, 130, 246, 0.7);
            color: #93c5fd;
            transform: translateY(-2px);
        }
        
        .divider {
            height: 1px;
            background: linear-gradient(90deg, rgba(148, 163, 184, 0), rgba(148, 163, 184, 0.35), rgba(148, 163, 184, 0));
            margin: 3rem 0;
        }
        
        .features {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
        }
        
        .feature {
            padding: 1.5rem;
            background: linear-gradient(135deg, rgba(59,130,246,0.15), rgba(14,165,233,0.1));
            border-radius: 16px;
            border: 1px solid rgba(148, 163, 184, 0.2);
            transition: all 0.3s ease;
            text-align: center;
        }
        
        .feature:hover {
            transform: translateY(-5px);
            background: linear-gradient(135deg, rgba(59,130,246,0.25), rgba(14,165,233,0.15));
            border-color: rgba(59, 130, 246, 0.4);
        }
        
        .feature-icon {
            font-size: 2.5rem;
            margin-bottom: 1rem;
            background: linear-gradient(135deg, #60a5fa, #38bdf8);
            -webkit-background-clip: text;
            background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        
        .feature h3 {
            color: #e2e8f0;
            font-size: 1.25rem;
            font-weight: 600;
            margin-bottom: 0.75rem;
        }
        
        .feature p {
            color: #94a3b8;
            font-size: 0.95rem;
            margin: 0;
            line-height: 1.5;
        }
        
        @media (max-width: 768px) {
            .glass-card {
                padding: 2rem 1.5rem;
            }
            
            .page-header h1 {
                font-size: 2rem;
            }
            
            .buttons {
                flex-direction: column;
            }
            
            .btn {
                width: 100%;
                justify-content: center;
            }
        }
    </style>
</head>
<body>
    <div class="glass-card">
        <div class="page-header">
            <span class="badge-soft"><i class="bi bi-cpu me-1"></i>IoT Platform</span>
            <h1 class="text-gradient">IoT Sensor Intelligence</h1>
            <p>Hệ thống giám sát và quản lý dữ liệu cảm biến IoT thời gian thực</p>
        </div>
        
        <div class="buttons">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-primary">
                <i class="bi bi-speedometer2"></i>
                <span>Xem Dashboard</span>
            </a>
            <a href="<%= request.getContextPath() %>/admin/thresholds" class="btn btn-secondary">
                <i class="bi bi-sliders"></i>
                <span>Quản lý Ngưỡng</span>
            </a>
        </div>
        
        <div class="divider"></div>
        
        <div class="features">
            <div class="feature">
                <div class="feature-icon">
                    <i class="bi bi-graph-up-arrow"></i>
                </div>
                <h3>Real-time Monitoring</h3>
                <p>Giám sát dữ liệu cảm biến theo thời gian thực với cập nhật tự động</p>
            </div>
            <div class="feature">
                <div class="feature-icon">
                    <i class="bi bi-bar-chart-line"></i>
                </div>
                <h3>Data Visualization</h3>
                <p>Trực quan hóa dữ liệu bằng biểu đồ động và báo cáo chi tiết</p>
            </div>
            <div class="feature">
                <div class="feature-icon">
                    <i class="bi bi-gear"></i>
                </div>
                <h3>Smart Configuration</h3>
                <p>Quản lý ngưỡng cảnh báo và cấu hình thiết bị dễ dàng</p>
            </div>
        </div>
    </div>
</body>
</html>
