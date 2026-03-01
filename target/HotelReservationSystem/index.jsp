<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Ocean View Resort - Reservation System</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600&display=swap" rel="stylesheet">
        </head>

        <body>
            <div class="background-overlay"></div>
            <div class="container" id="app-container">
                <!-- Header -->
                <header>
                    <h1>Ocean View Resort</h1>
                    <p>Room Reservation System</p>
                    <button id="logout-btn" class="hidden btn-secondary">Logout</button>
                </header>

                <!-- Messages -->
                <div id="message-box" class="hidden"></div>

                <!-- Login Section -->
                <section id="login-section" class="active-section glass-panel">
                    <h2>Staff Login</h2>
                    <form id="login-form">
                        <div class="form-group">
                            <label for="username">Username</label>
                            <input type="text" id="username" required>
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input type="password" id="password" required>
                        </div>
                        <button type="submit" class="btn-primary">Login</button>
                    </form>
                </section>

                <!-- Main Menu Section -->
                <section id="menu-section" class="hidden glass-panel">
                    <h2>Main Menu</h2>
                    <div class="menu-grid">
                        <button class="menu-btn" data-target="add-res-section">1. Add New Reservation</button>
                        <button class="menu-btn" data-target="view-res-section">2. Display Reservation</button>
                        <button class="menu-btn" data-target="bill-section">3. Calculate & Print Bill</button>
                        <button class="menu-btn" data-target="help-section">4. Help Section</button>
                        <button class="menu-btn btn-danger" id="exit-btn">5. Exit System</button>
                    </div>
                </section>

                <!-- Add Reservation Section -->
                <section id="add-res-section" class="hidden glass-panel">
                    <h2>Add New Reservation</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>
                    <form id="add-res-form">
                        <div class="form-group">
                            <label>Guest Name</label>
                            <input type="text" id="guestName" required>
                        </div>
                        <div class="form-group">
                            <label>Address</label>
                            <input type="text" id="address" required>
                        </div>
                        <div class="form-group">
                            <label>Contact Number (e.g. 0712345678)</label>
                            <input type="tel" id="contactNumber" pattern="[0-9]{10}" title="10 digit phone number"
                                required>
                        </div>
                        <div class="form-group">
                            <label>Room Type</label>
                            <select id="roomType" required>
                                <option value="Single">Single ($100/night)</option>
                                <option value="Double">Double ($150/night)</option>
                                <option value="Deluxe">Deluxe ($250/night)</option>
                                <option value="Suite">Suite ($400/night)</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Check-in Date</label>
                            <input type="date" id="checkInDate" required>
                        </div>
                        <div class="form-group">
                            <label>Check-out Date</label>
                            <input type="date" id="checkOutDate" required>
                        </div>
                        <button type="submit" class="btn-primary">Submit Reservation</button>
                    </form>
                    <div id="res-result" class="result-box hidden"></div>
                </section>

                <!-- View Reservation Section -->
                <section id="view-res-section" class="hidden glass-panel">
                    <h2>Display Reservation</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>
                    <form id="search-form" class="inline-form">
                        <input type="text" id="searchId" placeholder="Enter Reservation Number (e.g. RES-XXX)" required>
                        <button type="submit" class="btn-primary">Search</button>
                    </form>
                    <div id="search-result" class="result-box hidden"></div>
                </section>

                <!-- Calculate Bill Section -->
                <section id="bill-section" class="hidden glass-panel">
                    <h2>Calculate Bill</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>
                    <form id="bill-form" class="inline-form">
                        <input type="text" id="billId" placeholder="Enter Reservation Number" required>
                        <button type="submit" class="btn-primary">Generate Bill</button>
                    </form>
                    <div id="bill-result" class="result-box hidden"></div>
                    <button id="print-bill-btn" class="hidden btn-secondary" style="margin-top: 10px;">Print
                        Bill</button>
                </section>

                <!-- Help Section -->
                <section id="help-section" class="hidden glass-panel">
                    <h2>Help & Instructions</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>
                    <div class="help-content text-left">
                        <h3>Menu Options:</h3>
                        <ul>
                            <li><strong>1. Add New Reservation:</strong> Enter guest details, select room type, and
                                choose check-in/out dates. Validations will prevent booking conflicts.</li>
                            <li><strong>2. Display Reservation:</strong> Search for an existing reservation using the
                                unique Reservation Number generated during booking.</li>
                            <li><strong>3. Calculate & Print Bill:</strong> Generates a detailed breakdown of costs
                                based on room type and number of nights stayed.</li>
                            <li><strong>4. Help Section:</strong> Displays this guide.</li>
                            <li><strong>5. Exit System:</strong> Securely logs you out and closes the session.</li>
                        </ul>
                    </div>
                </section>

            </div>
            <script>
                const CTX_PATH = '${pageContext.request.contextPath}';
                const apiBase = CTX_PATH + '/api';
            </script>
            <script src="${pageContext.request.contextPath}/app.js"></script>
        </body>

        </html>