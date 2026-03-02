package servlets;

import database.DatabaseConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/reports")
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();
        Gson gson = new Gson();

        // 1. Session-based role check
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;

        if (role == null || !role.equalsIgnoreCase("admin")) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Unauthorized access. Admins only.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(jsonResponse));
            }
            return;
        }

        // 2. Fetch Aggregated Data
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                // Total Revenue & Total Reservations
                String summarySql = "SELECT SUM(total_cost) as totalRevenue, COUNT(*) as totalRes FROM Reservations";
                try (PreparedStatement ps = conn.prepareStatement(summarySql);
                        ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        jsonResponse.addProperty("totalRevenue", rs.getDouble("totalRevenue"));
                        jsonResponse.addProperty("totalReservations", rs.getInt("totalRes"));
                    }
                }

                // Room Type Usage
                String roomUsageSql = "SELECT room_type, COUNT(*) as usageCount FROM Reservations GROUP BY room_type";
                JsonArray roomStats = new JsonArray();
                try (PreparedStatement ps = conn.prepareStatement(roomUsageSql);
                        ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        JsonObject stat = new JsonObject();
                        stat.addProperty("roomType", rs.getString("room_type"));
                        stat.addProperty("count", rs.getInt("usageCount"));
                        roomStats.add(stat);
                    }
                }
                jsonResponse.add("roomStats", roomStats);
                jsonResponse.addProperty("success", true);
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}
