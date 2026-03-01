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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@WebServlet("/api/reservations")
public class ReservationServlet extends HttpServlet {

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

        String guestName = getJsonString(jsonObject, "guestName");
        String address = getJsonString(jsonObject, "address");
        String contactNumber = getJsonString(jsonObject, "contactNumber");
        String roomType = getJsonString(jsonObject, "roomType");
        String checkInDateStr = getJsonString(jsonObject, "checkInDate");
        String checkOutDateStr = getJsonString(jsonObject, "checkOutDate");
        String email = getJsonString(jsonObject, "email");

        String responseMessage = "";
        boolean success = false;
        String resNo = "";
        double finalTotalCost = 0.0;

        if (guestName != null)
            guestName = guestName.trim();
        if (address != null)
            address = address.trim();
        if (contactNumber != null)
            contactNumber = contactNumber.trim();

        if (guestName == null || address == null || contactNumber == null || roomType == null || checkInDateStr == null
                || checkOutDateStr == null || email == null) {
            responseMessage = "Missing required fields.";
        } else {
            try {
                LocalDate checkIn = LocalDate.parse(checkInDateStr);
                LocalDate checkOut = LocalDate.parse(checkOutDateStr);

                if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
                    responseMessage = "Check-out date must be after check-in date.";
                } else {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            boolean hasConflict = checkConflict(conn, roomType, checkIn, checkOut);
                            if (hasConflict) {
                                responseMessage = "Room type is fully booked for these dates.";
                            } else {
                                double rate = getRoomRate(conn, roomType);
                                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                                double totalCost = rate * nights;

                                resNo = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                                String insertSql = "INSERT INTO Reservations (reservation_number, guest_name, email, address, contact_number, room_type, check_in_date, check_out_date, total_cost, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')";
                                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                                    pstmt.setString(1, resNo);
                                    pstmt.setString(2, guestName);
                                    pstmt.setString(3, email);
                                    pstmt.setString(4, address);
                                    pstmt.setString(5, contactNumber);
                                    pstmt.setString(6, roomType);
                                    pstmt.setString(7, checkInDateStr);
                                    pstmt.setString(8, checkOutDateStr);
                                    pstmt.setDouble(9, totalCost);
                                    pstmt.executeUpdate();

                                    finalTotalCost = totalCost;
                                    success = true;
                                    responseMessage = "Reservation successful! ID: " + resNo;
                                }
                            }
                        } else {
                            responseMessage = "Database error.";
                        }
                    } catch (SQLException e) {
                        responseMessage = "Database exception: " + e.getMessage();
                    }
                }
            } catch (DateTimeParseException e) {
                responseMessage = "Invalid date format.";
            }
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", responseMessage);
        jsonResponse.addProperty("reservationNumber", resNo);
        if (success) {
            jsonResponse.addProperty("totalCost", finalTotalCost);
        }

        response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
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

                                JsonObject resObj = new JsonObject();
                                resObj.addProperty("reservationNumber", rs.getString("reservation_number"));
                                resObj.addProperty("guestName", rs.getString("guest_name"));
                                resObj.addProperty("address", rs.getString("address"));
                                resObj.addProperty("contactNumber", rs.getString("contact_number"));
                                resObj.addProperty("roomType", rs.getString("room_type"));
                                resObj.addProperty("checkInDate", rs.getString("check_in_date"));
                                resObj.addProperty("checkOutDate", rs.getString("check_out_date"));
                                resObj.addProperty("totalCost", rs.getDouble("total_cost"));
                                resObj.addProperty("email", rs.getString("email"));
                                resObj.addProperty("paymentStatus", rs.getString("payment_status"));

                                jsonResponse.add("reservation", resObj);
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

    private String getJsonString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : null;
    }

    private boolean checkConflict(Connection conn, String roomType, LocalDate checkIn, LocalDate checkOut)
            throws SQLException {
        String sql = "SELECT count(*) FROM Reservations WHERE room_type = ? AND check_in_date < ? AND check_out_date > ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(sql)) {
            checkStmt.setString(1, roomType);
            checkStmt.setString(2, checkOut.toString());
            checkStmt.setString(3, checkIn.toString());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private double getRoomRate(Connection conn, String roomType) throws SQLException {
        String sql = "SELECT rate_per_night FROM Rooms WHERE room_type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        }
        return 100.0;
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
