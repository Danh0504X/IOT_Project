package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.dao.UserDAO;
import com.mycompany.iotwebapp.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // If already logged in, redirect to dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Show login page
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }
        
        // Authenticate user
        User user = userDAO.authenticate(username.trim(), password);
        
        if (user != null) {
            // Login successful
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("role", user.getRole());
            
            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(30 * 60);
            
            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // Login failed
            request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
