package models;

public class Room {
    private int id;
    private String roomType;
    private double ratePerNight;

    public Room(int id, String roomType, double ratePerNight) {
        this.id = id;
        this.roomType = roomType;
        this.ratePerNight = ratePerNight;
    }

    public int getId() { return id; }
    public String getRoomType() { return roomType; }
    public double getRatePerNight() { return ratePerNight; }
}
