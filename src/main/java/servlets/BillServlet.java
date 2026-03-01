package servlets;

import database.DatabaseConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@WebServlet("/api/bill")
public class BillServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String guestName = request.getParameter("name");
        JsonObject jsonResponse = new JsonObject();

        if (guestName == null || guestName.trim().isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing guest name.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            boolean success = false;
            String message = "Reservation not found.";
            int statusCode = HttpServletResponse.SC_NOT_FOUND;

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT * FROM Reservations WHERE guest_name LIKE ? ORDER BY check_in_date DESC LIMIT 1";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, "%" + guestName + "%");
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                statusCode = HttpServletResponse.SC_OK;
                                success = true;
                                message = "Success";

                                String cb = rs.getString("check_in_date");
                                String co = rs.getString("check_out_date");
                                long nights = ChronoUnit.DAYS.between(LocalDate.parse(cb), LocalDate.parse(co));
                                double totalCost = rs.getDouble("total_cost");
                                double rate = totalCost / nights;

                                JsonObject billObj = new JsonObject();
                                billObj.addProperty("reservationNumber", rs.getString("reservation_number"));
                                billObj.addProperty("guestName", rs.getString("guest_name"));
                                billObj.addProperty("roomType", rs.getString("room_type"));
                                billObj.addProperty("nights", nights);
                                billObj.addProperty("costPerNight", rate);
                                billObj.addProperty("totalAmount", totalCost);
                                billObj.addProperty("email", rs.getString("email"));
                                billObj.addProperty("paymentStatus", rs.getString("payment_status"));

                                jsonResponse.add("bill", billObj);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                message = "Database error.";
            }

            jsonResponse.addProperty("success", success);
            jsonResponse.addProperty("message", message);
            response.setStatus(statusCode);
        }

        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
