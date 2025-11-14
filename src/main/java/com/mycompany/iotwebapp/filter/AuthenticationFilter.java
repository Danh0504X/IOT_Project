package com.mycompany.iotwebapp.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/dashboard", "/admin/*", "/poll"})
public class AuthenticationFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        
        // Check if user is logged in
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        
        if (isLoggedIn) {
            // User is logged in, continue to requested resource
            chain.doFilter(request, response);
        } else {
            // User is not logged in, redirect to login page
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/login");
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
