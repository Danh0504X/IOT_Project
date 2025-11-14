package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.model.SensorSettings;
import com.mycompany.iotwebapp.service.ThresholdService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet to handle threshold administration.
 * GET: Display threshold editing form (thresholds.jsp)
 * POST: Update thresholds from form submission
 */
@WebServlet(name = "thresholdAdminServlet", urlPatterns = "/admin/thresholds")
public class ThresholdAdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ThresholdService service = new ThresholdService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Get deviceId from parameter (default to DEVICE001)
            String deviceId = req.getParameter("deviceId");
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = "DEVICE001";
            }

            // Get current settings from database
            List<SensorSettings> settingsList = service.getSettings(deviceId);
            
            // Convert to map for easy access in JSP
            Map<String, Double> thresholdMap = new HashMap<>();
            for (SensorSettings setting : settingsList) {
                // Need to get sensor name - for now, use a helper method
                thresholdMap.put("setting_" + setting.getSensorTypeId(), setting.getThresholdValue());
            }
            
            // Get threshold map with sensor names
            Map<String, Double> thresholds = service.getThresholdMapForESP32(deviceId);
            
            req.setAttribute("deviceId", deviceId);
            req.setAttribute("thresholds", thresholds);
            req.setAttribute("settingsList", settingsList);
            
            // Check for success message
            String message = req.getParameter("message");
            if (message != null) {
                req.setAttribute("message", message);
            }
            
            req.getRequestDispatcher("/WEB-INF/views/thresholds.jsp").forward(req, resp);
            
        } catch (Exception e) {
            throw new ServletException("Error loading threshold settings", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Get deviceId from form
            String deviceId = req.getParameter("deviceId");
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = "DEVICE001";
            }

            // Build threshold map from form parameters
            Map<String, Double> thresholds = new HashMap<>();
            
            // Parse form parameters for each sensor type
            String[] sensorNames = {"mq1", "mq2", "mq3", "temperature", "dust"};
            for (String sensorName : sensorNames) {
                String valueParam = req.getParameter(sensorName);
                if (valueParam != null && !valueParam.isEmpty()) {
                    Double value = Double.parseDouble(valueParam);
                    thresholds.put(sensorName, value);
                }
            }

            // Update thresholds in database
            service.updateThresholds(deviceId, thresholds);

            // Redirect with success message
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=success");
            
        } catch (NumberFormatException e) {
            // Redirect with error message
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?message=error");
        } catch (Exception e) {
            throw new ServletException("Error updating thresholds", e);
        }
    }
}
