package servlets;

import com.google.gson.JsonObject;
import database.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/api/verify-otp")
public class VerifyOTPServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession();
        String userOtp = request.getParameter("otp");
        String sessionOtp = (String) session.getAttribute("otp");
        String resNo = (String) session.getAttribute("pendingResNo");
        String amount = (String) session.getAttribute("pendingAmount");

        // Basic validation
        if (userOtp == null || sessionOtp == null || !userOtp.equals(sessionOtp)) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid OTP. Please try again.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        // OTP is correct! Update Database
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Update Reservation status
            String updateResSql = "UPDATE Reservations SET payment_status = 'Paid' WHERE reservation_number = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(updateResSql)) {
                ps1.setString(1, resNo);
                ps1.executeUpdate();
            }

            // 2. Insert Payment record
            String insertPaySql = "INSERT INTO Payments (reservation_number, card_holder, card_last_four, amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(insertPaySql)) {
                ps2.setString(1, resNo);
                ps2.setString(2, request.getParameter("cardHolder"));
                ps2.setString(3, request.getParameter("lastFour"));
                if (amount == null || amount.equals("undefined") || amount.isEmpty()) {
                    throw new Exception("Payment amount is missing or invalid.");
                }
                ps2.setDouble(4, Double.parseDouble(amount));
                ps2.executeUpdate();
            }

            conn.commit();

            // Clear session
            session.removeAttribute("otp");
            session.removeAttribute("pendingResNo");

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Payment Successful! Your reservation is now confirmed.");

        } catch (Exception e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
