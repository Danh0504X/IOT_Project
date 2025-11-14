<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - IoT Web App</title>
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
        
        .login-container {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            max-width: 400px;
            width: 100%;
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .login-header h1 {
            color: #333;
            font-size: 28px;
            margin-bottom: 10px;
        }
        
        .login-header p {
            color: #666;
            font-size: 14px;
        }
        
        .alert {
            padding: 12px 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .alert-error {
            background-color: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }
        
        .alert-info {
            background-color: #e7f3ff;
            color: #0066cc;
            border: 1px solid #b3d9ff;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            color: #333;
            font-weight: 600;
            margin-bottom: 8px;
            font-size: 14px;
        }
        
        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s ease;
        }
        
        input[type="text"]:focus,
        input[type="password"]:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 20px;
        }
        
        input[type="checkbox"] {
            width: 18px;
            height: 18px;
            cursor: pointer;
        }
        
        .checkbox-group label {
            margin-bottom: 0;
            font-weight: 400;
            cursor: pointer;
        }
        
        .btn-login {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        .btn-login:active {
            transform: translateY(0);
        }
        
        .login-footer {
            margin-top: 25px;
            text-align: center;
            color: #666;
            font-size: 13px;
        }
        
        .default-accounts {
            margin-top: 20px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
            font-size: 13px;
        }
        
        .default-accounts h3 {
            color: #333;
            font-size: 14px;
            margin-bottom: 10px;
        }
        
        .default-accounts ul {
            list-style: none;
            padding: 0;
        }
        
        .default-accounts li {
            padding: 5px 0;
            color: #666;
        }
        
        .default-accounts strong {
            color: #667eea;
        }
        
        @media (max-width: 480px) {
            .login-container {
                padding: 30px 25px;
            }
            
            .login-header h1 {
                font-size: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>🔐 Đăng nhập</h1>
            <p>IoT Monitoring System</p>
        </div>
        
        <!-- Error message -->
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                ❌ ${error}
            </div>
        </c:if>
        
        <!-- Info message for first time users -->
        <c:if test="${empty error}">
            <div class="alert alert-info">
                ℹ️ Sử dụng tài khoản mặc định để đăng nhập
            </div>
        </c:if>
        
        <!-- Login form -->
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group">
                <label for="username">Tên đăng nhập</label>
                <input type="text" 
                       id="username" 
                       name="username" 
                       value="${username != null ? username : ''}"
                       placeholder="Nhập tên đăng nhập"
                       required 
                       autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password" 
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
                Đăng nhập
            </button>
        </form>
        
        <!-- Default accounts info -->
        <div class="default-accounts">
            <h3>📋 Tài khoản mặc định:</h3>
            <ul>
                <li><strong>Admin:</strong> admin / admin123</li>
                <li><strong>User:</strong> user / user123</li>
            </ul>
        </div>
        
        <div class="login-footer">
            © 2025 IoT Web Application
        </div>
    </div>
</body>
</html>
