package com.group13.greengrocer.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CarrierRating {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int carrierId;
    private int customerId;
    private int orderId;
    private User carrier;
    private User customer;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public CarrierRating() {
        this.createdAt = LocalDateTime.now();
    }

    public CarrierRating(int carrierId, int customerId, int orderId, int rating) {
        this();
        this.carrierId = carrierId;
        this.customerId = customerId;
        this.orderId = orderId;
        setRating(rating);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCarrierId() { return carrierId; }
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public User getCarrier() { return carrier; }
    public void setCarrier(User carrier) { 
        this.carrier = carrier;
        if (carrier != null) this.carrierId = carrier.getId();
    }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { 
        this.customer = customer;
        if (customer != null) this.customerId = customer.getId();
    }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        this.rating = rating;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStarDisplay() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public String toString() {
        return getStarDisplay() + " - Order #" + orderId;
    }
}
