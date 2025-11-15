<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - IoT Sensor Intelligence</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        body {
            background: radial-gradient(circle at top left, #0f172a, #111827 55%, #020617);
            min-height: 100vh;
            color: #e2e8f0;
            font-family: "Inter", "Segoe UI", sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
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
            padding: 2.5rem;
            max-width: 450px;
            width: 100%;
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 2rem;
        }
        
        .login-header h1 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }
        
        .login-header p {
            color: #94a3b8;
            font-size: 0.95rem;
        }
        
        .badge-soft {
            background: linear-gradient(135deg, rgba(59,130,246,0.25), rgba(14,165,233,0.25));
            color: #bae6fd;
            border: 1px solid rgba(59, 130, 246, 0.3);
            padding: 0.5rem 1rem;
            border-radius: 50px;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-block;
            margin-bottom: 1rem;
        }
        
        .alert {
            border-radius: 12px;
            padding: 1rem 1.25rem;
            margin-bottom: 1.5rem;
            border: 1px solid;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        
        .alert-error {
            background: rgba(239, 68, 68, 0.15);
            border-color: rgba(239, 68, 68, 0.3);
            color: #fca5a5;
        }
        
        .alert-info {
            background: rgba(59, 130, 246, 0.15);
            border-color: rgba(59, 130, 246, 0.3);
            color: #93c5fd;
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
        
        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            margin-bottom: 1.5rem;
        }
        
        .checkbox-group input[type="checkbox"] {
            width: 18px;
            height: 18px;
            cursor: pointer;
            accent-color: #3b82f6;
        }
        
        .checkbox-group label {
            margin-bottom: 0;
            font-weight: 400;
            cursor: pointer;
            color: #cbd5e1;
        }
        
        .btn-login {
            width: 100%;
            padding: 0.875rem 1.5rem;
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            border: none;
            border-radius: 12px;
            font-weight: 600;
            color: white;
            transition: all 0.3s ease;
            box-shadow: 0 4px 6px rgba(59, 130, 246, 0.3);
            font-size: 1rem;
        }
        
        .btn-login:hover {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
            color: white;
        }
        
        .btn-login:active {
            transform: translateY(0);
        }
        
        .default-accounts {
            margin-top: 1.5rem;
            padding: 1.25rem;
            background: rgba(59, 130, 246, 0.1);
            border-radius: 12px;
            border: 1px solid rgba(59, 130, 246, 0.2);
        }
        
        .default-accounts h3 {
            color: #93c5fd;
            font-size: 0.9rem;
            font-weight: 600;
            margin-bottom: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .default-accounts ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        
        .default-accounts li {
            padding: 0.5rem 0;
            color: #cbd5e1;
            font-size: 0.875rem;
        }
        
        .default-accounts strong {
            color: #60a5fa;
            font-weight: 600;
        }
        
        .login-footer {
            margin-top: 1.5rem;
            text-align: center;
            color: #64748b;
            font-size: 0.85rem;
        }
        
        @media (max-width: 480px) {
            .glass-card {
                padding: 2rem 1.5rem;
            }
            
            .login-header h1 {
                font-size: 1.75rem;
            }
        }
    </style>
</head>
<body>
    <div class="glass-card">
        <div class="login-header">
            <span class="badge-soft"><i class="bi bi-shield-lock me-1"></i>Secure Access</span>
            <h1 class="text-gradient">Đăng nhập</h1>
            <p>IoT Sensor Intelligence System</p>
        </div>
        
        <!-- Error message -->
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <i class="bi bi-exclamation-circle"></i>
                <span>${error}</span>
            </div>
        </c:if>
        
        <!-- Info message for first time users -->
        <c:if test="${empty error}">
            <div class="alert alert-info">
                <i class="bi bi-info-circle"></i>
                <span>Sử dụng tài khoản mặc định để đăng nhập</span>
            </div>
        </c:if>
        
        <!-- Login form -->
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group">
                <label for="username"><i class="bi bi-person me-1"></i>Tên đăng nhập</label>
                <input type="text" 
                       class="form-control"
                       id="username" 
                       name="username" 
                       value="${username != null ? username : ''}"
                       placeholder="Nhập tên đăng nhập"
                       required 
                       autofocus>
            </div>
            
            <div class="form-group">
                <label for="password"><i class="bi bi-lock me-1"></i>Mật khẩu</label>
                <input type="password" 
                       class="form-control"
                       id="password" 
                       name="password" 
                       placeholder="Nhập mật khẩu"
                       required>
            </div>
            
            <div class="checkbox-group">
                <input type="checkbox" id="rememberMe" name="rememberMe" value="true">
                <label for="rememberMe">Ghi nhớ đăng nhập</label>
            </div>
            
            <button type="submit" class="btn-login">
                <i class="bi bi-box-arrow-in-right me-2"></i>Đăng nhập
            </button>
        </form>
        
        <!-- Default accounts info -->
        <div class="default-accounts">
            <h3><i class="bi bi-info-circle me-1"></i>Tài khoản mặc định</h3>
            <ul>
                <li><strong>Admin:</strong> admin / admin123</li>
                <li><strong>User:</strong> user / user123</li>
            </ul>
        </div>
        
        <div class="login-footer">
            © 2025 IoT Sensor Intelligence
        </div>
    </div>
</body>
</html>
