package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.service.ThresholdService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
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
            // Get deviceId from parameter (can be number or device name like "DEVICE001")
            String deviceIdStr = req.getParameter("deviceId");
            Integer deviceId;
            if (deviceIdStr == null || deviceIdStr.isEmpty()) {
                // If no deviceId provided, try to find first device or default to 1
                com.mycompany.iotwebapp.dao.DeviceInfoDAO deviceDAO = new com.mycompany.iotwebapp.dao.DeviceInfoDAO();
                java.util.List<com.mycompany.iotwebapp.model.DeviceInfo> devices = deviceDAO.findAll();
                if (!devices.isEmpty()) {
                    deviceId = devices.get(0).getDeviceId();
                    System.out.println("[ThresholdAdminServlet] No deviceId provided, using first device: " + deviceId);
                } else {
                    deviceId = 1; // Default to device ID 1
                    System.out.println("[ThresholdAdminServlet] No deviceId provided and no devices found, defaulting to: " + deviceId);
                }
            } else {
                try {
                    deviceId = Integer.parseInt(deviceIdStr);
                } catch (NumberFormatException e) {
                    // If deviceId is not a number (e.g., "DEVICE001"), try to find device by name
                    System.out.println("[ThresholdAdminServlet] deviceId '" + deviceIdStr + "' is not a number, trying to find by name...");
                    com.mycompany.iotwebapp.dao.DeviceInfoDAO deviceDAO = new com.mycompany.iotwebapp.dao.DeviceInfoDAO();
                    java.util.List<com.mycompany.iotwebapp.model.DeviceInfo> devices = deviceDAO.findAll();
                    com.mycompany.iotwebapp.model.DeviceInfo foundDevice = null;
                    for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                        if (deviceIdStr.equals(device.getDeviceName())) {
                            foundDevice = device;
                            break;
                        }
                    }
                    if (foundDevice != null) {
                        deviceId = foundDevice.getDeviceId();
                        System.out.println("[ThresholdAdminServlet] Found device by name: " + deviceIdStr + " -> DeviceID=" + deviceId);
                    } else {
                        // Default to first device or 1
                        if (!devices.isEmpty()) {
                            deviceId = devices.get(0).getDeviceId();
                            System.out.println("[ThresholdAdminServlet] Device not found by name, using first device: " + deviceId);
                        } else {
                            deviceId = 1;
                            System.out.println("[ThresholdAdminServlet] Device not found by name, defaulting to: " + deviceId);
                        }
                    }
                }
            }

            System.out.println("[ThresholdAdminServlet] Loading threshold settings for device: " + deviceId);

            // Get threshold map with sensor names
            Map<String, Double> thresholds = service.getThresholdMapForESP32(deviceId);
            System.out.println("[ThresholdAdminServlet] Retrieved thresholds: " + thresholds);
            
            req.setAttribute("deviceId", deviceId);
            req.setAttribute("thresholds", thresholds);
            
            // Check for success message
            String message = req.getParameter("message");
            if (message != null) {
                req.setAttribute("message", message);
            }
            
            req.getRequestDispatcher("/WEB-INF/views/thresholds.jsp").forward(req, resp);
            
        } catch (Exception e) {
            System.err.println("[ThresholdAdminServlet] Error loading threshold settings: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Error loading threshold settings", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer deviceId = 1;
        try {
            // Get deviceId from form (can be number or device name)
            String deviceIdStr = req.getParameter("deviceId");
            if (deviceIdStr != null && !deviceIdStr.isEmpty()) {
                try {
                    deviceId = Integer.parseInt(deviceIdStr);
                } catch (NumberFormatException e) {
                    // If deviceId is not a number, try to find device by name
                    System.out.println("[ThresholdAdminServlet] deviceId '" + deviceIdStr + "' is not a number, trying to find by name...");
                    com.mycompany.iotwebapp.dao.DeviceInfoDAO deviceDAO = new com.mycompany.iotwebapp.dao.DeviceInfoDAO();
                    java.util.List<com.mycompany.iotwebapp.model.DeviceInfo> devices = deviceDAO.findAll();
                    com.mycompany.iotwebapp.model.DeviceInfo foundDevice = null;
                    for (com.mycompany.iotwebapp.model.DeviceInfo device : devices) {
                        if (deviceIdStr.equals(device.getDeviceName())) {
                            foundDevice = device;
                            break;
                        }
                    }
                    if (foundDevice != null) {
                        deviceId = foundDevice.getDeviceId();
                        System.out.println("[ThresholdAdminServlet] Found device by name: " + deviceIdStr + " -> DeviceID=" + deviceId);
                    } else {
                        // Default to first device or 1
                        if (!devices.isEmpty()) {
                            deviceId = devices.get(0).getDeviceId();
                            System.out.println("[ThresholdAdminServlet] Device not found by name, using first device: " + deviceId);
                        } else {
                            deviceId = 1;
                            System.out.println("[ThresholdAdminServlet] Device not found by name, defaulting to: " + deviceId);
                        }
                    }
                }
            }

            System.out.println("[ThresholdAdminServlet] Processing POST request for device: " + deviceId);

            // Build threshold map from form parameters
            Map<String, Double> thresholds = new HashMap<>();
            
            // Parse form parameters for each sensor type
            String[] sensorNames = {"mq1", "mq2", "mq3", "temperature", "humidity", "dust"};
            for (String sensorName : sensorNames) {
                String valueParam = req.getParameter(sensorName);
                if (valueParam != null && !valueParam.isEmpty()) {
                    try {
                        Double value = Double.parseDouble(valueParam);
                        thresholds.put(sensorName, value);
                        System.out.println("[ThresholdAdminServlet] ✓ Parsed threshold: " + sensorName + " = " + value);
                    } catch (NumberFormatException e) {
                        System.err.println("[ThresholdAdminServlet] ✗ Invalid number format for " + sensorName + ": " + valueParam);
                        // Continue with other sensors
                    }
                } else {
                    System.out.println("[ThresholdAdminServlet] ⚠ No value provided for " + sensorName);
                }
            }

            if (thresholds.isEmpty()) {
                System.err.println("[ThresholdAdminServlet] ✗ No valid threshold values provided");
                resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
                return;
            }

            System.out.println("[ThresholdAdminServlet] Updating " + thresholds.size() + " thresholds for device: " + deviceId);
            System.out.println("[ThresholdAdminServlet] Thresholds to update: " + thresholds);
            
            // Update thresholds in database
            service.updateThresholds(deviceId, thresholds);

            System.out.println("[ThresholdAdminServlet] ✅ Successfully updated thresholds for device: " + deviceId);

            // Redirect with success message
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=success");
            
        } catch (NumberFormatException e) {
            System.err.println("[ThresholdAdminServlet] ✗ Number format error: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
        } catch (Exception e) {
            System.err.println("[ThresholdAdminServlet] ✗ Error updating thresholds: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
        }
    }
}
