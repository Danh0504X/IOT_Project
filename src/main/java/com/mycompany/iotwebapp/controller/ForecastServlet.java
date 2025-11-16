package com.mycompany.iotwebapp.controller;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/forecast")
public class ForecastServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=AirQualityManagement;encrypt=false;";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "123456";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Double tempNow = null, tempBefore = null;
        Double humNow = null, humBefore = null;
        Double mq2Now = null, mq2Before = null;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // --- Lấy bản ghi mới nhất ---
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT TOP 1 * FROM LatestSensorRecordsView ORDER BY Timestamp DESC"
            );
            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                tempNow = rs1.getDouble("Temperature");
                humNow = rs1.getDouble("Humidity");
                mq2Now = rs1.getDouble("MQ2");
            }

            // --- Lấy bản ghi 30 phút trước ---
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT TOP 1 * FROM LatestSensorRecordsView " +
                    "WHERE Timestamp <= DATEADD(MINUTE, -30, GETDATE()) " +
                    "ORDER BY Timestamp DESC"
            );
            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                tempBefore = rs2.getDouble("Temperature");
                humBefore = rs2.getDouble("Humidity");
                mq2Before = rs2.getDouble("MQ2");
            }

        } catch (Exception e) {
            req.setAttribute("forecastError", e.getMessage());
            e.printStackTrace();
        }

        // --- TÍNH DỰ ĐOÁN 1 GIỜ TỚI ---
        double tempForecast = predict1h(tempNow, tempBefore);
        double humForecast = predict1h(humNow, humBefore);
        double mq2Forecast = predict1h(mq2Now, mq2Before);

        req.setAttribute("forecastTemp", tempForecast);
        req.setAttribute("forecastHum", humForecast);
        req.setAttribute("forecastMQ2", mq2Forecast);

        RequestDispatcher rd =
                req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        rd.forward(req, resp);
    }

    private double predict1h(Double now, Double before) {
        if (now == null) return 0;
        if (before == null) return now;

        return now + ((now - before) * 2); // 30 phút × 2 = 1 giờ
    }
}