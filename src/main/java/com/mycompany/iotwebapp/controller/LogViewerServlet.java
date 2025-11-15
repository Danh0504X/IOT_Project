package com.mycompany.iotwebapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Servlet to view threshold debug logs.
 * Access at: /admin/view-logs
 */
@WebServlet(name = "logViewerServlet", urlPatterns = "/admin/view-logs")
public class LogViewerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String LOG_FILE = "logs/threshold_debug.log";
    private static final int MAX_LINES = 500; // Show last 500 lines

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        html.append("<title>Threshold Debug Logs</title>");
        html.append("<style>");
        html.append("body { font-family: 'Courier New', monospace; background: #1e1e1e; color: #d4d4d4; padding: 20px; }");
        html.append("h1 { color: #4ec9b0; }");
        html.append(".log-container { background: #252526; border: 1px solid #3e3e42; padding: 15px; border-radius: 5px; max-height: 80vh; overflow-y: auto; }");
        html.append(".log-line { margin: 2px 0; white-space: pre-wrap; word-wrap: break-word; }");
        html.append(".log-info { color: #4ec9b0; }");
        html.append(".log-error { color: #f48771; }");
        html.append(".log-warn { color: #dcdcaa; }");
        html.append(".log-debug { color: #9cdcfe; }");
        html.append(".refresh-btn { background: #007acc; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin: 10px 0; }");
        html.append(".refresh-btn:hover { background: #005a9e; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<h1>🔍 Threshold Debug Logs</h1>");
        html.append("<button class='refresh-btn' onclick='location.reload()'>🔄 Refresh</button>");
        html.append("<div class='log-container'>");
        
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            html.append("<div class='log-line log-warn'>Log file not found: " + LOG_FILE + "</div>");
            html.append("<div class='log-line log-info'>Log file will be created when first log entry is written.</div>");
        } else {
            try {
                List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
                
                // Show last MAX_LINES
                int startIndex = Math.max(0, lines.size() - MAX_LINES);
                List<String> recentLines = lines.subList(startIndex, lines.size());
                
                if (lines.size() > MAX_LINES) {
                    html.append("<div class='log-line log-info'>Showing last " + MAX_LINES + " lines (total: " + lines.size() + ")</div>");
                }
                
                for (String line : recentLines) {
                    String cssClass = "log-info";
                    if (line.contains("[ERROR]")) {
                        cssClass = "log-error";
                    } else if (line.contains("[WARN]")) {
                        cssClass = "log-warn";
                    } else if (line.contains("[DEBUG]")) {
                        cssClass = "log-debug";
                    }
                    html.append("<div class='log-line " + cssClass + "'>").append(escapeHtml(line)).append("</div>");
                }
                
                if (recentLines.isEmpty()) {
                    html.append("<div class='log-line log-info'>Log file is empty.</div>");
                }
                
            } catch (IOException e) {
                html.append("<div class='log-line log-error'>Error reading log file: " + escapeHtml(e.getMessage()) + "</div>");
            }
        }
        
        html.append("</div>");
        html.append("<script>");
        html.append("setTimeout(function() { window.scrollTo(0, document.body.scrollHeight); }, 100);");
        html.append("</script>");
        html.append("</body></html>");
        
        resp.getWriter().write(html.toString());
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

