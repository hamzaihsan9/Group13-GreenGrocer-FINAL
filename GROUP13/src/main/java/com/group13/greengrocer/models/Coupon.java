package com.group13.greengrocer.models;

import java.time.LocalDate;

public class Coupon {
    private int id;
    private String code;
    private double discountValue;
    private boolean isPercentage;
    private LocalDate expiryDate;
    private Integer assignedUserId;
    private User assignedUser;
    private boolean isUsed;

    public Coupon() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public boolean isPercentage() { return isPercentage; }
    public void setPercentage(boolean percentage) { isPercentage = percentage; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }

    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { 
        this.assignedUser = assignedUser;
        if (assignedUser != null) {
            this.assignedUserId = assignedUser.getId();
        }
    }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    public boolean isValid() {
        if (isUsed) return false;
        if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) return false;
        return true;
    }

    public double calculateDiscount(double cartTotal) {
        if (!isValid()) return 0;
        if (isPercentage) {
            return cartTotal * (discountValue / 100.0);
        }
        return Math.min(discountValue, cartTotal);
    }

    public String getDiscountDisplay() {
        if (isPercentage) {
            return String.format("%.0f%%", discountValue);
        }
        return String.format("₺%.2f", discountValue);
    }

    @Override
    public String toString() {
        return code + " - " + getDiscountDisplay();
    }
}
