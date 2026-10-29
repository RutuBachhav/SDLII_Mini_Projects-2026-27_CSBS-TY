package com.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(name = "price_per_night", nullable = false)
    private double pricePerNight;

    @Column(nullable = false)
    private boolean available;

    // One Room can have Many Bookings over time
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    public Room() {
    }

    public Room(String roomNumber, String roomType,
                double pricePerNight, boolean available) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    // Display helper only (not a database column):
    // shows "4500" instead of "4500.0" for whole-number prices
    public String getPriceDisplay() {
        if (pricePerNight == Math.floor(pricePerNight)) {
            return String.valueOf((long) pricePerNight);
        }
        return String.valueOf(pricePerNight);
    }
}
