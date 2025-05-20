package model;

import java.time.LocalDateTime;

public class RideBooking {
    private String bookingId;
    private String riderId;
    private String driverId;
    private String pickup;
    private String drop;
    private double amount;
    private LocalDateTime createdAt;
    private String status;

    public RideBooking(String bookingId, String riderId, String driverId, String pickup, String drop, double amount, LocalDateTime createdAt, String status) {
        this.bookingId = bookingId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.pickup = pickup;
        this.drop = drop;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getRiderId() { return riderId; }
    public String getDriverId() { return driverId; }
    public String getPickup() { return pickup; }
    public String getDrop() { return drop; }
    public double getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
}