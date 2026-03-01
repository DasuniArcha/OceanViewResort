package servlets;

import database.DatabaseConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/staff")
public class StaffServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, username, role, email FROM Users WHERE role = 'staff'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {
                JsonArray staffList = new JsonArray();
                while (rs.next()) {
                    JsonObject staff = new JsonObject();
                    staff.addProperty("id", rs.getInt("id"));
                    staff.addProperty("username", rs.getString("username"));
                    staff.addProperty("role", rs.getString("role"));
                    staff.addProperty("email", rs.getString("email"));
                    staffList.add(staff);
                }
                jsonResponse.addProperty("success", true);
                jsonResponse.add("staff", staffList);
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
        }
        out.print(gson.toJson(jsonResponse));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
        String username = jsonObject.get("username").getAsString();
        String password = jsonObject.get("password").getAsString();
        String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Users (username, password, role, email) VALUES (?, ?, 'staff', ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.executeUpdate();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Staff account created successfully.");
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error adding staff: " + e.getMessage());
        }
        out.print(gson.toJson(jsonResponse));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
        int id = jsonObject.get("id").getAsInt();
        String username = jsonObject.get("username").getAsString();
        String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
        String email = jsonObject.get("email").getAsString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            if (password != null && !password.trim().isEmpty()) {
                sql = "UPDATE Users SET username = ?, password = ?, email = ? WHERE id = ? AND role = 'staff'";
            } else {
                sql = "UPDATE Users SET username = ?, email = ? WHERE id = ? AND role = 'staff'";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                if (password != null && !password.trim().isEmpty()) {
                    pstmt.setString(2, password);
                    pstmt.setString(3, email);
                    pstmt.setInt(4, id);
                } else {
                    pstmt.setString(2, email);
                    pstmt.setInt(3, id);
                }

                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Staff account updated successfully.");
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Staff member not found or role mismatch.");
                }
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error updating staff: " + e.getMessage());
        }
        out.print(gson.toJson(jsonResponse));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        int id = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Users WHERE id = ? AND role = 'staff'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int deleted = pstmt.executeUpdate();
                if (deleted > 0) {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Staff account removed.");
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Staff member not found or cannot be deleted.");
                }
            }
        } catch (SQLException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error removing staff: " + e.getMessage());
        }
        out.print(gson.toJson(jsonResponse));
    }
}
