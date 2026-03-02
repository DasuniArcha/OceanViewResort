<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
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
            <button id="room-gallery-btn" class="hidden">
                <span class="bar"></span>
                <span class="bar"></span>
                <span class="bar"></span>
            </button>
            <button id="logout-btn" class="hidden btn-secondary">Logout</button>
            <div class="background-overlay"></div>
            <div class="container" id="app-container">
                <!-- Header -->
                <header>
                    <h1>Ocean View Resort</h1>
                    <p>Room Reservation System</p>
                </header>

                <!-- Messages -->
                <div id="message-box" class="hidden"></div>

                <!-- Login Section -->
                <section id="login-section" class="active-section glass-panel">
                    <h2> Login</h2>
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
                        <button class="menu-btn hidden" id="admin-manage-btn" data-target="staff-mgmt-section">5. Manage
                            Staff (Admin)</button>
                        <button class="menu-btn hidden" id="generate-reports-btn" data-target="reports-section">6.
                            Generate Reports (Admin)</button>
                        <button class="menu-btn btn-danger" id="exit-btn">7. Exit System</button>
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
                            <label>Email Address</label>
                            <input type="email" id="email" required placeholder="example@email.com">
                        </div>
                        <div class="form-group">
                            <label>Contact Number (e.g. 0712345678)</label>
                            <input type="tel" id="contactNumber" pattern="[0-9]{10}" title="10 digit phone number"
                                required>
                        </div>
                        <div class="form-group">
                            <label>Room Type</label>
                            <select id="roomType" required>
                                <option value="Single">Single (Rs.1000/night)</option>
                                <option value="Double">Double (Rs.1500/night)</option>
                                <option value="Deluxe">Deluxe (Rs.2500/night)</option>
                                <option value="Suite">Suite (Rs.4000/night)</option>
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
                        <input type="text" id="searchName" placeholder="Enter Guest Full Name" required>
                        <button type="submit" class="btn-primary">Search</button>
                    </form>
                    <div id="search-result" class="result-box hidden"></div>
                </section>

                <!-- Calculate Bill Section -->
                <section id="bill-section" class="hidden glass-panel">
                    <h2>Calculate Bill</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>
                    <form id="bill-form" class="inline-form">
                        <input type="text" id="billName" placeholder="Enter Guest Full Name" required>
                        <button type="submit" class="btn-primary">Generate Bill</button>
                    </form>
                    <div id="bill-result" class="result-box hidden"></div>
                    <button id="print-bill-btn" class="hidden btn-secondary" style="margin-top: 10px;">Print
                        Bill</button>
                </section>

                <!-- Payment Section -->
                <section id="payment-section" class="hidden glass-panel">
                    <h2>Credit Card Payment</h2>
                    <div id="payment-details-summary" class="result-box" style="margin-bottom: 20px;"></div>
                    <form id="payment-form">
                        <div class="form-group">
                            <label>Total Amount to Pay (Rs.)</label>
                            <input type="text" id="payAmountDisplay" readonly
                                style="font-weight: bold; color: #000000;">
                        </div>
                        <div class="form-group">
                            <label>Cardholder Name</label>
                            <input type="text" id="cardHolder" required>
                        </div>
                        <div class="form-group">
                            <label>Card Number</label>
                            <input type="text" id="cardNumber" pattern="[0-9]{16}" title="16 digit card number"
                                placeholder="0000 0000 0000 0000" required>
                        </div>
                        <div class="form-row" style="display: flex; gap: 10px;">
                            <div class="form-group" style="flex: 1;">
                                <label>Expiry Date</label>
                                <input type="text" id="expiryDate" placeholder="MM/YY"
                                    pattern="(0[1-9]|1[0-2])\/[0-9]{2}" title="MM/YY format" required>
                            </div>
                            <div class="form-group" style="flex: 1;">
                                <label>CVV</label>
                                <input type="password" id="cvv" pattern="[0-9]{3,4}" title="3 or 4 digit CVV"
                                    placeholder="***" required>
                            </div>
                        </div>
                        <button type="submit" class="btn-primary">Proceed to Payment</button>
                    </form>
                </section>

                <!-- OTP Verification Section -->
                <section id="otp-section" class="hidden glass-panel">
                    <h2>Email Verification</h2>
                    <p style="text-align: center; margin-bottom: 20px;">A 6-digit OTP has been sent to <span
                            id="display-email" style="font-weight: 600;"></span></p>
                    <form id="otp-form">
                        <div class="form-group">
                            <label>Enter 6-digit OTP</label>
                            <input type="text" id="otpInput" pattern="[0-9]{6}" title="6 digit OTP" maxlength="6"
                                style="text-align: center; font-size: 1.5rem; letter-spacing: 5px;" required>
                        </div>
                        <button type="submit" class="btn-primary">Verify & Pay</button>
                    </form>
                    <p style="text-align: center; margin-top: 15px;">
                        <button id="resend-otp-btn" class="btn-secondary"
                            style="width: auto; padding: 5px 15px; font-size: 0.8rem;">Resend OTP</button>
                    </p>
                </section>

                <!-- Staff Management Section (Admin Only) -->
                <section id="staff-mgmt-section" class="hidden glass-panel">
                    <h2>Staff Management</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>

                    <div class="staff-controls" style="margin-top: 20px;">
                        <h3>Add New Staff Member</h3>
                        <form id="add-staff-form" class="inline-form">
                            <input type="text" id="new-staff-username" placeholder="Username" required>
                            <input type="password" id="new-staff-password" placeholder="Password" required>
                            <input type="email" id="new-staff-email" placeholder="Email" required>
                            <button type="submit" class="btn-primary">Add Staff Member</button>
                        </form>
                    </div>

                    <div class="staff-list-container">
                        <h3>Current Staff Members</h3>
                        <div id="staff-table-container" class="result-box">
                            <!-- Table will be injected here -->
                            <p>Loading staff list...</p>
                        </div>
                    </div>
                </section>

                <!-- Edit Staff Modal -->
                <div id="edit-staff-modal" class="hidden overlay">
                    <div class="glass-panel modal-content">
                        <h3>Edit Staff Member</h3>
                        <form id="edit-staff-form">
                            <input type="hidden" id="edit-staff-id">
                            <div class="form-group">
                                <label>Username</label>
                                <input type="text" id="edit-staff-username" required>
                            </div>
                            <div class="form-group">
                                <label>Email Address</label>
                                <input type="email" id="edit-staff-email" required>
                            </div>
                            <div class="form-group">
                                <label>Set New Password (Leave blank to keep current)</label>
                                <input type="password" id="edit-staff-password" placeholder="New Password">
                            </div>
                            <div class="button-row">
                                <button type="submit" class="btn-primary">Update Staff</button>
                                <button type="button" id="close-edit-modal" class="btn-secondary">Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Admin Reports Section -->
                <section id="reports-section" class="hidden glass-panel">
                    <h2>Admin Reservation Reports</h2>
                    <button class="back-btn btn-secondary">Back to Menu</button>

                    <div id="reports-result" class="result-box" style="margin-top: 20px;">
                        <!-- Reports data will be injected here -->
                        <p>Loading report data...</p>
                    </div>
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

                <!-- Room Gallery Overlay -->
                <div id="room-gallery-overlay" class="hidden">
                    <div class="gallery-content glass-panel">
                        <button id="close-gallery-btn">&times;</button>
                        <h2>Our Premium Rooms</h2>
                        <div id="room-cards-container">
                            <!-- Cards will be injected by JavaScript -->
                        </div>
                    </div>
                </div>

            </div>
            <script>
                const CTX_PATH = '${pageContext.request.contextPath}';
                const apiBase = CTX_PATH + '/api';
            </script>
            <script src="${pageContext.request.contextPath}/app.js"></script>
        </body>


        </html>
