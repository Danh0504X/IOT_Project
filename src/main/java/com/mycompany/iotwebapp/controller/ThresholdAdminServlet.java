package com.mycompany.iotwebapp.controller;

import com.mycompany.iotwebapp.service.ThresholdService;
import com.mycompany.iotwebapp.util.FileLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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

            // Get all threshold levels for all sensors
            // This will ALWAYS return default thresholds if database is empty (ensures form always displays)
            Map<String, java.util.List<com.mycompany.iotwebapp.model.Threshold>> allThresholdLevels = service.getAllThresholdLevels();
            System.out.println("[ThresholdAdminServlet] Retrieved " + allThresholdLevels.size() + " sensor types with threshold levels");
            
            // Ensure we have at least 6 sensors (one for each type)
            if (allThresholdLevels.isEmpty()) {
                System.err.println("[ThresholdAdminServlet] ⚠ WARNING: getAllThresholdLevels() returned empty map! Form may not display.");
            }
            
            // Also get simple threshold map for backward compatibility (display current values)
            Map<String, Double> simpleThresholds = service.getThresholdMapForESP32(deviceId);
            
            req.setAttribute("deviceId", deviceId);
            req.setAttribute("allThresholdLevels", allThresholdLevels);
            req.setAttribute("thresholds", simpleThresholds); // For backward compatibility
            
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
        FileLogger.separator();
        FileLogger.info("=== THRESHOLD UPDATE REQUEST START ===");
        
        Integer deviceId = 1;
        try {
            // Get deviceId from form (can be number or device name)
            String deviceIdStr = req.getParameter("deviceId");
            FileLogger.info("Received POST request with deviceId: " + deviceIdStr);
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

            // Get current user ID from session (for logging) - default to 1 if not available
            Integer updatedBy = 1; // TODO: Get from session
            if (req.getSession().getAttribute("userId") != null) {
                try {
                    updatedBy = Integer.parseInt(req.getSession().getAttribute("userId").toString());
                } catch (NumberFormatException e) {
                    System.out.println("[ThresholdAdminServlet] ⚠ Could not parse userId from session, using default: 1");
                }
            }

            // Parse all threshold levels from form parameters
            // Format: sensorName_levelIndex_minValue, sensorName_levelIndex_maxValue
            String[] sensorNames = {"temperature", "humidity", "mq1", "mq2", "mq3", "dust"};
            boolean hasUpdates = false;
            boolean hasErrors = false;
            StringBuilder errorDetails = new StringBuilder();
            
            // Get all threshold levels once (more efficient)
            FileLogger.info("Loading all threshold levels from service...");
            Map<String, java.util.List<com.mycompany.iotwebapp.model.Threshold>> allLevels = service.getAllThresholdLevels();
            FileLogger.info("Loaded " + (allLevels != null ? allLevels.size() : 0) + " sensor types");
            
            if (allLevels == null || allLevels.isEmpty()) {
                String errorMsg = "No threshold levels loaded from service";
                FileLogger.error(errorMsg);
                System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
                resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
                return;
            }
            
            for (String sensorName : sensorNames) {
                FileLogger.debug("Processing sensor: " + sensorName);
                // Get threshold levels for this sensor
                java.util.List<com.mycompany.iotwebapp.model.Threshold> sensorThresholds = allLevels.get(sensorName);
                
                if (sensorThresholds == null || sensorThresholds.isEmpty()) {
                    FileLogger.warn("No thresholds found for sensor: " + sensorName);
                    System.out.println("[ThresholdAdminServlet] ⚠ No thresholds found for sensor: " + sensorName);
                    continue;
                }
                
                FileLogger.info("Found " + sensorThresholds.size() + " levels for sensor: " + sensorName);
                
                // Update each level
                java.util.List<com.mycompany.iotwebapp.model.Threshold> updatedThresholds = new java.util.ArrayList<>();
                
                for (int i = 0; i < sensorThresholds.size(); i++) {
                    com.mycompany.iotwebapp.model.Threshold threshold = sensorThresholds.get(i);
                    
                    // Get form parameters: sensorName_levelIndex_minValue, sensorName_levelIndex_maxValue
                    String paramNameMin = sensorName + "_" + i + "_minValue";
                    String paramNameMax = sensorName + "_" + i + "_maxValue";
                    String minValueParam = req.getParameter(paramNameMin);
                    String maxValueParam = req.getParameter(paramNameMax);
                    
                    FileLogger.debug(String.format("  Level %d: %s=%s, %s=%s", i, paramNameMin, minValueParam, paramNameMax, maxValueParam));
                    
                    if (minValueParam != null && maxValueParam != null && 
                        !minValueParam.trim().isEmpty() && !maxValueParam.trim().isEmpty()) {
                        try {
                            Float minValue = Float.parseFloat(minValueParam.trim());
                            Float maxValue = Float.parseFloat(maxValueParam.trim());
                            
                            // Validate: minValue < maxValue
                            if (minValue >= maxValue) {
                                String errorMsg = sensorName + " level " + i + ": minValue (" + minValue + ") >= maxValue (" + maxValue + ")";
                                System.err.println("[ThresholdAdminServlet] ✗ Invalid range for " + errorMsg);
                                errorDetails.append(errorMsg).append("; ");
                                hasErrors = true;
                                continue;
                            }
                            
                            // Create a copy to avoid modifying the original (important for default thresholds)
                            com.mycompany.iotwebapp.model.Threshold thresholdCopy = new com.mycompany.iotwebapp.model.Threshold();
                            thresholdCopy.setThresholdId(threshold.getThresholdId()); // Preserve ID if exists
                            thresholdCopy.setSensorTypeId(threshold.getSensorTypeId());
                            thresholdCopy.setLevelName(threshold.getLevelName());
                            thresholdCopy.setMinValue(minValue);
                            thresholdCopy.setMaxValue(maxValue);
                            thresholdCopy.setAlertLevel(threshold.getAlertLevel());
                            thresholdCopy.setMessage(threshold.getMessage());
                            
                            updatedThresholds.add(thresholdCopy);
                            hasUpdates = true;
                            
                            String logMsg = String.format("✓ Parsed %s level %d (%s): %.2f - %.2f %s", 
                                sensorName, i, threshold.getLevelName(), minValue, maxValue,
                                threshold.getThresholdId() != null ? "(UPDATE ID=" + threshold.getThresholdId() + ")" : "(INSERT NEW)");
                            FileLogger.info(logMsg);
                            System.out.println("[ThresholdAdminServlet] " + logMsg);
                        } catch (NumberFormatException e) {
                            String errorMsg = sensorName + " level " + i + ": invalid number format (min=" + minValueParam + ", max=" + maxValueParam + ")";
                            FileLogger.error(errorMsg, e);
                            System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
                            errorDetails.append(errorMsg).append("; ");
                            hasErrors = true;
                        }
                    } else {
                        System.out.println("[ThresholdAdminServlet] ⚠ Missing parameters for " + sensorName + " level " + i + 
                                         " (minValueParam=" + minValueParam + ", maxValueParam=" + maxValueParam + ")");
                    }
                }
                
                // Update all levels for this sensor
                if (!updatedThresholds.isEmpty()) {
                    FileLogger.info("Attempting to update " + updatedThresholds.size() + " levels for sensor: " + sensorName);
                    try {
                        service.updateAllThresholdLevels(sensorName, updatedThresholds, updatedBy);
                        String successMsg = "✅ Successfully updated " + updatedThresholds.size() + " levels for " + sensorName;
                        FileLogger.info(successMsg);
                        System.out.println("[ThresholdAdminServlet] " + successMsg);
                    } catch (Exception e) {
                        String errorMsg = "Failed to update " + sensorName + ": " + e.getMessage();
                        FileLogger.error(errorMsg, e);
                        System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
                        errorDetails.append(errorMsg).append("; ");
                        hasErrors = true;
                        e.printStackTrace();
                    }
                } else {
                    FileLogger.warn("No updated thresholds to save for sensor: " + sensorName);
                }
            }

            if (!hasUpdates) {
                String errorMsg = "No valid threshold values provided";
                FileLogger.error(errorMsg);
                System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
                resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
                return;
            }
            
            if (hasErrors) {
                String errorMsg = "Some errors occurred during update: " + errorDetails.toString();
                FileLogger.error(errorMsg);
                System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
                resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
                return;
            }

            String successMsg = "✅ Successfully updated thresholds for device: " + deviceId;
            FileLogger.info(successMsg);
            FileLogger.info("=== THRESHOLD UPDATE REQUEST END (SUCCESS) ===");
            System.out.println("[ThresholdAdminServlet] " + successMsg);

            // Redirect with success message
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=success");
            
        } catch (NumberFormatException e) {
            String errorMsg = "Number format error: " + e.getMessage();
            FileLogger.error(errorMsg, e);
            System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
        } catch (Exception e) {
            String errorMsg = "Error updating thresholds: " + e.getMessage();
            FileLogger.error(errorMsg, e);
            FileLogger.info("=== THRESHOLD UPDATE REQUEST END (ERROR) ===");
            System.err.println("[ThresholdAdminServlet] ✗ " + errorMsg);
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/thresholds?deviceId=" + deviceId + "&message=error");
        }
    }
}
