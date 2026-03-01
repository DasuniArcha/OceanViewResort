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
    showSection('login-section');
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
            document.getElementById('login-form').reset();
            document.getElementById('logout-btn').classList.remove('hidden');
            showSection('menu-section');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Connection to server failed.', 'error');
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
            const resultBox = document.getElementById('res-result');
            resultBox.innerHTML = `<strong>Success!</strong><br>Your Reservation Number is <span style="font-size: 1.2rem; color: #d32f2f;">${data.reservationNumber}</span><br>Please save this for future reference.`;
            resultBox.classList.remove('hidden');
        } else {
            showMessage(data.message, 'error');
        }
    } catch (err) {
        showMessage('Error saving reservation.', 'error');
    }
});

// 3. Display Reservation Logic
document.getElementById('search-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('searchId').value;

    try {
        const res = await fetch(`${apiBase}/reservations?id=${id}`);
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
    const id = document.getElementById('billId').value;

    try {
        const res = await fetch(`${apiBase}/bill?id=${id}`);
        const data = await res.json();
        const box = document.getElementById('bill-result');
        const printBtn = document.getElementById('print-bill-btn');
        if (data.success) {
            const b = data.bill;
            box.innerHTML = `
                <h3 style="text-align: center; margin-bottom: 15px;">Ocean View Resort Invoice</h3>
                <div class="bill-item"><span>Guest Name:</span> <span>${b.guestName}</span></div>
                <div class="bill-item"><span>Room Type:</span> <span>${b.roomType}</span></div>
                <div class="bill-item"><span>Number of Nights:</span> <span>${b.nights}</span></div>
                <div class="bill-item"><span>Cost per Night:</span> <span>$${b.costPerNight.toFixed(2)}</span></div>
                <div class="bill-item bill-total"><span>Total Amount:</span> <span>$${b.totalAmount.toFixed(2)}</span></div>
            `;
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
