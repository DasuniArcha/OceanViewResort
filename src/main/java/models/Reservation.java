package models;

public class Reservation {
    private String reservationNumber;
    private String guestName;
    private String address;
    private String contactNumber;
    private String roomType;
    private String checkInDate;
    private String checkOutDate;
    private double totalCost;

    public Reservation(String reservationNumber, String guestName, String address, String contactNumber,
            String roomType, String checkInDate, String checkOutDate, double totalCost) {
        this.reservationNumber = reservationNumber;
        this.guestName = guestName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalCost = totalCost;
    }

    // Getters
    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
