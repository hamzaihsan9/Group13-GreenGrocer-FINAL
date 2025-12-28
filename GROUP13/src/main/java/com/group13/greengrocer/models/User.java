package com.group13.greengrocer.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String address;
    private String phone;
    private String email;
    private int loyaltyPoints;
    private int completedOrders;

    public User() {}

    public User(int id, String username, String password, String role, 
                String fullName, String address, String phone, String email,
                int loyaltyPoints, int completedOrders) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
        this.completedOrders = completedOrders;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public boolean isCustomer() { return "customer".equals(role); }
    public boolean isCarrier() { return "carrier".equals(role); }
    public boolean isOwner() { return "owner".equals(role); }

    public double getLoyaltyDiscount() {
        if (completedOrders >= 10) return 10.0;
        if (completedOrders >= 5) return 5.0;
        if (completedOrders >= 3) return 2.0;
        return 0.0;
    }

    @Override
    public String toString() {
        return fullName != null && !fullName.isEmpty() ? fullName : username;
    }
}
