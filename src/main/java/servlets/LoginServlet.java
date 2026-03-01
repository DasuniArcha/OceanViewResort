package servlets;

import database.DatabaseConnection;
import com.google.gson.Gson;
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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);

        String username = jsonObject.has("username") ? jsonObject.get("username").getAsString() : null;
        String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;

        if (username != null)
            username = username.trim();
        if (password != null)
            password = password.trim();

        boolean isValid = false;
        String message = "Invalid credentials";
        JsonObject jsonResponse = new JsonObject();

        if (username != null && password != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(sql)) {
                        checkStmt.setString(1, username);
                        checkStmt.setString(2, password);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                isValid = true;
                                message = "Login successful";
                                jsonResponse.addProperty("role", rs.getString("role"));
                            }
                        }
                    }
                } else {
                    message = "Database connection failed.";
                }
            } catch (SQLException e) {
                message = "Database error: " + e.getMessage();
            }
        }

        jsonResponse.addProperty("success", isValid);
        jsonResponse.addProperty("message", message);

        response.setStatus(isValid ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
