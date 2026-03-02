// apiBase is provided by index.jsp

// Utility functions
function showMessage(msg, type = 'success') {
    const box = document.getElementById('message-box');
    box.textContent = msg;
    box.className = '';
    box.classList.add(type === 'error' ? 'msg-error' : 'msg-success');
    box.classList.remove('hidden');
    setTimeout(() => box.classList.add('hidden'), 3500);
}

function showSection(id) {
    document.querySelectorAll('section').forEach(sec => sec.classList.add('hidden'));
    document.getElementById(id).classList.remove('hidden');
}

// Navigation Listeners
document.querySelectorAll('.menu-btn[data-target]').forEach(btn => {
    btn.addEventListener('click', (e) => {
        showSection(e.target.dataset.target);
    });
});

document.querySelectorAll('.back-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        showSection('menu-section');
    });
});

// Exit System
document.getElementById('exit-btn').addEventListener('click', () => {
    if (confirm('Are you sure you want to exit the system?')) {
        showMessage('Successfully logged out.', 'success');
        document.getElementById('logout-btn').classList.add('hidden');
        showSection('login-section');
    }
});

document.getElementById('logout-btn').addEventListener('click', () => {
    showMessage('Logged out.', 'success');
    document.getElementById('logout-btn').classList.add('hidden');
    document.getElementById('room-gallery-btn').classList.add('hidden');
    showSection('login-section');
});

// Room Gallery Logic
const rooms = [
    {
        type: 'Single',
        price: 100,
        image: CTX_PATH + '/images/room_single.jpg',
        desc: 'Perfect for solo travelers. Features a comfortable bed, workspace, and stunning window view.'
    },
    {
        type: 'Double',
        price: 150,
        image: CTX_PATH + '/images/room_double.jpg',
        desc: 'Ideal for couples. Spacious room with a king-size bed and elegant beach-themed decor.'
    },
    {
        type: 'Deluxe',
        price: 250,
        image: CTX_PATH + '/images/room_deluxe.png',
        desc: 'Premium experience with extra space, sunset balcony, and high-end wooden finishes.'
    },
    {
        type: 'Suite',
        price: 400,
        image: CTX_PATH + '/images/room_suite.png',
        desc: 'The ultimate luxury. Panoramic ocean views, separate living area, and top-tier amenities.'
    }
];

function populateRoomGallery() {
    const container = document.getElementById('room-cards-container');
    container.innerHTML = rooms.map(room => `
        <div class="room-card">
            <img src="${room.image}" alt="${room.type}">
            <div class="room-info">
                <h3>${room.type} Room</h3>
                <div class="room-price">Rs.${room.price.toFixed(2)} / night</div>
                <p class="room-desc">${room.desc}</p>
            </div>
        </div>
    `).join('');
}

document.getElementById('room-gallery-btn').addEventListener('click', () => {
    populateRoomGallery();
    document.getElementById('room-gallery-overlay').classList.remove('hidden');
});

document.getElementById('close-gallery-btn').addEventListener('click', () => {
    document.getElementById('room-gallery-overlay').classList.add('hidden');
});

// Close on click outside
document.getElementById('room-gallery-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'room-gallery-overlay') {
        document.getElementById('room-gallery-overlay').classList.add('hidden');
    }
});

// 1. Login Logic
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const u = document.getElementById('username').value;
    const p = document.getElementById('password').value;

    try {
        const res = await fetch(`${apiBase}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p })
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message, 'success');
            window.userRole = data.role;
            localStorage.setItem('userRole', data.role);

            document.getElementById('login-form').reset();
            document.getElementById('logout-btn').classList.remove('hidden');
            document.getElementById('room-gallery-btn').classList.remove('hidden');

            // Show admin button if role is admin
            const adminBtn = document.getElementById('admin-manage-btn');
            if (data.role === 'admin') {
                adminBtn.classList.remove('hidden');
            } else {
                adminBtn.classList.add('hidden');
            }

            showSection('menu-section');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Connection to server failed.', 'error');
    }
});

// Staff Management Logic (Admin Only)
document.getElementById('admin-manage-btn').addEventListener('click', () => {
    loadStaffList();
});

async function loadStaffList() {
    try {
        const res = await fetch(`${apiBase}/staff`);
        const data = await res.json();
        const container = document.getElementById('staff-table-container');

        if (data.success) {
            if (data.staff.length === 0) {
                container.innerHTML = '<p>No staff members found.</p>';
                return;
            }

            let html = '<table class="staff-table"><thead><tr><th>Username</th><th>Email</th><th>Action</th></tr></thead><tbody>';
            data.staff.forEach(s => {
                html += `
                    <tr>
                        <td>${s.username}</td>
                        <td>${s.email || '-'}</td>
                        <td>
                            <button class="btn-primary small-btn" onclick="openEditStaffModal(${JSON.stringify(s).replace(/"/g, '&quot;')})">Edit</button>
                            <button class="btn-danger small-btn" onclick="deleteStaff(${s.id})">Remove</button>
                        </td>
                    </tr>
                `;
            });
            html += '</tbody></table>';
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p class="error">${data.message}</p>`;
        }
    } catch (err) {
        document.getElementById('staff-table-container').innerHTML = '<p class="error">Error loading staff list.</p>';
    }
}

window.deleteStaff = async (id) => {
    if (!confirm('Are you sure you want to remove this staff member?')) return;

    try {
        const res = await fetch(`${apiBase}/staff?id=${id}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message);
            loadStaffList();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error deleting staff.', 'error');
    }
};

document.getElementById('add-staff-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const u = document.getElementById('new-staff-username').value;
    const p = document.getElementById('new-staff-password').value;
    const emailStr = document.getElementById('new-staff-email').value;

    try {
        const res = await fetch(`${apiBase}/staff`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p, email: emailStr })
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message);
            document.getElementById('add-staff-form').reset();
            loadStaffList();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error adding staff.', 'error');
    }
});

// Admin Reports Logic
document.getElementById('generate-reports-btn').addEventListener('click', () => {
    generateReports();
});

async function generateReports() {
    try {
        const res = await fetch(`${apiBase}/reports`);
        const data = await res.json();
        const box = document.getElementById('reports-result');

        if (data.success) {
            let roomStatsHtml = data.roomStats.map(s => `
                <div class="stat-pill">
                    <strong>${s.roomType}:</strong> ${s.count} bookings
                </div>
            `).join('');

            box.innerHTML = `
                <div class="report-dashboard">
                    <div class="main-stats">
                        <div class="stat-card">
                            <span class="stat-label">Total Revenue</span>
                            <span class="stat-value">Rs.${data.totalRevenue.toLocaleString()}</span>
                        </div>
                        <div class="stat-card">
                            <span class="stat-label">Total Reservations</span>
                            <span class="stat-value">${data.totalReservations}</span>
                        </div>
                    </div>
                    <div class="room-stats">
                        <h4>Occupancy by Room Type</h4>
                        <div class="stats-grid">
                            ${roomStatsHtml || '<p>No data available.</p>'}
                        </div>
                    </div>
                </div>
            `;
            showSection('reports-section');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error generating report.', 'error');
    }
}

async function loadStaffList() {
    try {
        const res = await fetch(`${apiBase}/staff`);
        const data = await res.json();
        const container = document.getElementById('staff-table-container');

        if (data.success) {
            if (data.staff.length === 0) {
                container.innerHTML = '<p>No staff members found.</p>';
                return;
            }

            let html = '<table class="staff-table"><thead><tr><th>Username</th><th>Email</th><th>Action</th></tr></thead><tbody>';
            data.staff.forEach(s => {
                html += `
                    <tr>
                        <td>${s.username}</td>
                        <td>${s.email || '-'}</td>
                        <td>
                            <button class="btn-primary small-btn" onclick="openEditStaffModal(${JSON.stringify(s).replace(/"/g, '&quot;')})">Edit</button>
                            <button class="btn-danger small-btn" onclick="deleteStaff(${s.id})">Remove</button>
                        </td>
                    </tr>
                `;
            });
            html += '</tbody></table>';
            container.innerHTML = html;
        } else {
            container.innerHTML = `<p class="error">${data.message}</p>`;
        }
    } catch (err) {
        document.getElementById('staff-table-container').innerHTML = '<p class="error">Error loading staff list.</p>';
    }
}

window.deleteStaff = async (id) => {
    if (!confirm('Are you sure you want to remove this staff member?')) return;

    try {
        const res = await fetch(`${apiBase}/staff?id=${id}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message);
            loadStaffList();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error deleting staff.', 'error');
    }
};

document.getElementById('add-staff-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const u = document.getElementById('new-staff-username').value;
    const p = document.getElementById('new-staff-password').value;
    const emailStr = document.getElementById('new-staff-email').value;

    try {
        const res = await fetch(`${apiBase}/staff`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: p, email: emailStr })
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message);
            document.getElementById('add-staff-form').reset();
            loadStaffList();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error adding staff.', 'error');
    }
});


// Edit Staff Logic
window.openEditStaffModal = (staff) => {
    document.getElementById('edit-staff-id').value = staff.id;
    document.getElementById('edit-staff-username').value = staff.username;
    document.getElementById('edit-staff-email').value = staff.email || '';
    document.getElementById('edit-staff-password').value = '';
    document.getElementById('edit-staff-modal').classList.remove('hidden');
};

document.getElementById('close-edit-modal').addEventListener('click', () => {
    document.getElementById('edit-staff-modal').classList.add('hidden');
});

document.getElementById('edit-staff-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const id = document.getElementById('edit-staff-id').value;
    const username = document.getElementById('edit-staff-username').value;
    const email = document.getElementById('edit-staff-email').value;
    const password = document.getElementById('edit-staff-password').value;

    const payload = { id: parseInt(id), username, email };
    if (password.trim()) {
        payload.password = password;
    }

    try {
        const res = await fetch(`${apiBase}/staff`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message);
            document.getElementById('edit-staff-modal').classList.add('hidden');
            loadStaffList();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error updating staff.', 'error');
    }
});

// 2. Add Reservation Logic
document.getElementById('add-res-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    // Front end validation happens natively with required/pattern but let's double check dates
    const ci = document.getElementById('checkInDate').value;
    const co = document.getElementById('checkOutDate').value;

    if (new Date(ci) >= new Date(co)) {
        showMessage('Check-out date must be after check-in date.', 'error');
        return;
    }

    const payload = {
        guestName: document.getElementById('guestName').value,
        address: document.getElementById('address').value,
        email: document.getElementById('email').value,
        contactNumber: document.getElementById('contactNumber').value,
        roomType: document.getElementById('roomType').value,
        checkInDate: ci,
        checkOutDate: co
    };

    try {
        const res = await fetch(`${apiBase}/reservations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message, 'success');
            document.getElementById('add-res-form').reset();

            // Just show success info, don't jump to payment
            const resultBox = document.getElementById('res-result');
            resultBox.innerHTML = `
                <div style="text-align: left; padding: 10px;">
                    <strong style="color: #4ade80;">Reservation Created Successfully!</strong><br>
                    <hr style="margin: 10px 0; border: 0; border-top: 1px solid #444;">
                    <strong>Reservation ID:</strong> ${data.reservationNumber}<br>
                    <strong>Total Amount Due:</strong> <span style="font-size: 1.2rem; color: #fbbf24;">Rs.${data.totalCost.toFixed(2)}</span><br>
                    <p style="margin-top: 10px; font-size: 0.9rem;">Please navigate to <strong>'Calculate & Print Bill'</strong> to proceed with your payment.</p>
                </div>
            `;
            resultBox.classList.remove('hidden');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error saving reservation.', 'error');
    }
});

// Payment Form Logic
document.getElementById('payment-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    // In a real app we'd trigger OTP send here
    try {
        const res = await fetch(`${apiBase}/payment`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `resNo=${window.currentResNo}&email=${window.currentEmail}&amount=${window.currentAmount}`
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message, 'success');
            document.getElementById('display-email').textContent = window.currentEmail;
            showSection('otp-section');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error initiating payment.', 'error');
    }
});

// OTP Form Logic
document.getElementById('otp-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const otp = document.getElementById('otpInput').value;
    const holder = document.getElementById('cardHolder').value;
    const cardNum = document.getElementById('cardNumber').value;
    const lastFour = cardNum.slice(-4);

    try {
        const res = await fetch(`${apiBase}/verify-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `otp=${otp}&cardHolder=${encodeURIComponent(holder)}&lastFour=${lastFour}`
        });
        const data = await res.json();
        if (data.success) {
            showMessage(data.message, 'success');
            // Show success result box
            showSection('menu-section');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error verifying OTP.', 'error');
    }
});

document.getElementById('resend-otp-btn').addEventListener('click', () => {
    document.getElementById('payment-form').dispatchEvent(new Event('submit'));
});

// 3. Display Reservation Logic
document.getElementById('search-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('searchName').value;

    try {
        const res = await fetch(`${apiBase}/reservations?name=${encodeURIComponent(name)}`);
        const data = await res.json();
        const box = document.getElementById('search-result');
        if (data.success) {
            const r = data.reservation;
            box.innerHTML = `
                <h3>Reservation Details</h3>
                <p><strong>Res No:</strong> ${r.reservationNumber}</p>
                <p><strong>Guest:</strong> ${r.guestName}</p>
                <p><strong>Address:</strong> ${r.address}</p>
                <p><strong>Contact:</strong> ${r.contactNumber}</p>
                <p><strong>Room:</strong> ${r.roomType}</p>
                <p><strong>Dates:</strong> ${r.checkInDate} to ${r.checkOutDate}</p>
            `;
            box.classList.remove('hidden');
        } else {
            showMessage(data.message, 'error');
            box.classList.add('hidden');
        }
    } catch (err) {
        showMessage('Error fetching reservation.', 'error');
    }
});

// 4. Calculate Bill Logic
document.getElementById('bill-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('billName').value;

    try {
        const res = await fetch(`${apiBase}/bill?name=${encodeURIComponent(name)}`);
        const data = await res.json();
        const box = document.getElementById('bill-result');
        const printBtn = document.getElementById('print-bill-btn');
        if (data.success) {
            const b = data.bill;
            box.innerHTML = `
                <h3 style="text-align: center; margin-bottom: 15px;">Ocean View Resort Invoice</h3>
                <div class="bill-item"><span>Reservation ID:</span> <span>${b.reservationNumber}</span></div>
                <div class="bill-item"><span>Guest Name:</span> <span>${b.guestName}</span></div>
                <div class="bill-item"><span>Room Type:</span> <span>${b.roomType}</span></div>
                <div class="bill-item"><span>Number of Nights:</span> <span>${b.nights}</span></div>
                <div class="bill-item"><span>Cost per Night:</span> <span>Rs.${b.costPerNight.toFixed(2)}</span></div>
                <div class="bill-item bill-total"><span>Total Amount:</span> <span>Rs.${b.totalAmount.toFixed(2)}</span></div>
                <div class="bill-item"><span>Payment Status:</span> <span style="color: ${b.paymentStatus === 'Paid' ? '#4ade80' : '#fbbf24'}">${b.paymentStatus}</span></div>
            `;

            if (b.paymentStatus === 'Pending') {
                const payBtn = document.createElement('button');
                payBtn.textContent = 'Proceed to Payment';
                payBtn.className = 'btn-primary';
                payBtn.style.marginTop = '15px';
                payBtn.onclick = () => {
                    window.currentResNo = b.reservationNumber;
                    window.currentEmail = b.email;
                    window.currentAmount = b.totalAmount;

                    const paySummary = document.getElementById('payment-details-summary');
                    paySummary.innerHTML = `
                        <strong>Reservation ID:</strong> ${b.reservationNumber}<br>
                        <strong>Guest:</strong> ${b.guestName}<br>
                        <strong>Total Amount:</strong> Rs.${b.totalAmount.toFixed(2)}
                    `;

                    // Set amount in the form input field
                    const payAmountInput = document.getElementById('payAmountDisplay');
                    if (payAmountInput) {
                        payAmountInput.value = b.totalAmount.toFixed(2);
                    }

                    showSection('payment-section');
                };
                box.appendChild(payBtn);
            }

            box.classList.remove('hidden');
            printBtn.classList.remove('hidden');
        } else {
            showMessage(data.message, 'error');
            box.classList.add('hidden');
            printBtn.classList.add('hidden');
        }
    } catch (err) {
        showMessage('Error fetching bill.', 'error');
    }
});

document.getElementById('print-bill-btn').addEventListener('click', () => {
    window.print();
});

