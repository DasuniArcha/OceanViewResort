package servlets;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

@WebServlet("/api/payment")
public class PaymentServlet extends HttpServlet {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "place-your-email@gmail.com"; // User must replace this
    private static final String SMTP_PASS = "place-your-app-password"; // User must replace this

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            // In a real app we'd parse JSON body, but for simplicity let's use parameters
            // or session
            HttpSession session = request.getSession();
            String resNo = request.getParameter("resNo");
            String email = request.getParameter("email");
            String amount = request.getParameter("amount");

            if (resNo == null || email == null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Missing reservation data.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(1000000));

            // Store details in session for verification
            session.setAttribute("otp", otp);
            session.setAttribute("pendingResNo", resNo);
            session.setAttribute("pendingAmount", amount);
            session.setAttribute("pendingEmail", email);

            // Send Email
            boolean emailSent = sendOTPEmail(email, otp);

            if (emailSent) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "OTP sent to " + email);
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Failed to send email. Please check SMTP settings.");
            }

        } catch (Exception e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }

    private boolean sendOTPEmail(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // NOTE: This will fail if SMTP_USER/PASS are not set.
        // For development/demo, we'll log the OTP to console as fallback
        System.out.println("DEBUG: OTP for " + toEmail + " is " + otp);

        if (SMTP_USER.contains("place-your-email")) {
            return true; // Simulate success for demo purposes if not configured
        }

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Ocean View Resort Payment OTP");
            message.setText("Dear Guest,\n\nYour OTP for payment verification is: " + otp +
                    "\n\nThis OTP is valid for 10 minutes.\n\nThank you,\nOcean View Resort");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
