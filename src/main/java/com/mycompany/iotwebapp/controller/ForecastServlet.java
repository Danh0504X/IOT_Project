package com.mycompany.iotwebapp.controller;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // --- Lấy 10 bản ghi gần nhất ---
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 10 * FROM LatestSensorRecordsView ORDER BY Timestamp DESC"
            );
            ResultSet rs = ps.executeQuery();

            List<Double> temps = new ArrayList<>();
            List<Double> hums = new ArrayList<>();
            List<Double> mq2s = new ArrayList<>();

            while (rs.next()) {
                temps.add(rs.getDouble("Temperature"));
                hums.add(rs.getDouble("Humidity"));
                mq2s.add(rs.getDouble("MQ2"));
            }

            // Đảo danh sách (chronological)
            java.util.Collections.reverse(temps);
            java.util.Collections.reverse(hums);
            java.util.Collections.reverse(mq2s);

            double tempForecast = predict1Hour(temps);
            double humForecast  = predict1Hour(hums);
            double mq2Forecast  = predict1Hour(mq2s);

            req.setAttribute("forecastTemp", tempForecast);
            req.setAttribute("forecastHum", humForecast);
            req.setAttribute("forecastMQ2", mq2Forecast);

        } catch (Exception e) {
            req.setAttribute("forecastError", e.getMessage());
            e.printStackTrace();
        }

        RequestDispatcher rd =
                req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        rd.forward(req, resp);
    }

    /**
     * Dự đoán 1 giờ bằng cách tính SLOPE (độ dốc)
     * slope = (avg(last 3) - avg(first 3)) / 0.5 giờ
     */
    private double predict1Hour(List<Double> values) {
        if (values == null || values.size() < 3) return 0;

        // Tính trung bình 3 giá trị đầu và cuối
        double startAvg = (values.get(0) + values.get(1) + values.get(2)) / 3.0;
        double endAvg = (values.get(values.size()-1)
                + values.get(values.size()-2)
                + values.get(values.size()-3)) / 3.0;

        double delta = endAvg - startAvg;   // thay đổi trong 30 phút
        double slopePerHour = delta * 2;    // đổi sang 1 giờ

        return endAvg + slopePerHour;
    }
}