package com.group13.greengrocer.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final double VAT_RATE = 0.18;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int customerId;
    private int carrierId;
    private User customer;
    private User carrier;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String status;
    private double subtotal;
    private double vatAmount;
    private double total;
    private double discount;
    private byte[] invoiceData;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.orderTime = LocalDateTime.now();
        this.status = STATUS_PENDING;
    }

    public Order(User customer) {
        this();
        this.customer = customer;
        this.customerId = customer.getId();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getCarrierId() { return carrierId; }
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { 
        this.customer = customer;
        if (customer != null) this.customerId = customer.getId();
    }

    public User getCarrier() { return carrier; }
    public void setCarrier(User carrier) { 
        this.carrier = carrier;
        if (carrier != null) this.carrierId = carrier.getId();
    }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getVatAmount() { return vatAmount; }
    public void setVatAmount(double vatAmount) { this.vatAmount = vatAmount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getTotalWithVAT() { return total; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public byte[] getInvoiceData() { return invoiceData; }
    public void setInvoiceData(byte[] invoiceData) { this.invoiceData = invoiceData; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public void addItem(OrderItem item) {
        for (OrderItem existing : items) {
            if (existing.getProductId() == item.getProductId()) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                recalculateTotals();
                return;
            }
        }
        items.add(item);
        recalculateTotals();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        recalculateTotals();
    }

    public void recalculateTotals() {
        subtotal = 0;
        for (OrderItem item : items) {
            subtotal += item.getSubtotal();
        }
        double discounted = subtotal - discount;
        if (discounted < 0) discounted = 0;
        vatAmount = discounted * VAT_RATE;
        total = discounted + vatAmount;
    }

    public boolean canCancel() {
        if (!STATUS_PENDING.equals(status) && !STATUS_AVAILABLE.equals(status)) {
            return false;
        }
        return LocalDateTime.now().isBefore(orderTime.plusHours(1));
    }

    public String getFormattedOrderTime() {
        return orderTime != null ? orderTime.format(FORMATTER) : "";
    }

    public String getFormattedDeliveryTime() {
        return deliveryTime != null ? deliveryTime.format(FORMATTER) : "";
    }

    @Override
    public String toString() {
        return "Order #" + id + " - " + status;
    }
}
