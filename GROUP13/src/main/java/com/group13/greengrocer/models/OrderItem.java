package com.group13.greengrocer.models;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private Product product;
    private String productName;
    private double quantity;
    private double unitPrice;
    private double subtotal;

    public OrderItem() {}

    public OrderItem(Product product, double quantity) {
        this.product = product;
        this.productId = product.getId();
        this.productName = product.getName();
        this.quantity = quantity;
        this.unitPrice = product.getEffectivePrice();
        calculateSubtotal();
    }

    public OrderItem(int id, int orderId, int productId, String productName,
                     double quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getId();
            this.productName = product.getName();
        }
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { 
        this.quantity = quantity;
        calculateSubtotal();
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public void calculateSubtotal() {
        this.subtotal = quantity * unitPrice;
    }

    @Override
    public String toString() {
        return productName + " x " + quantity + " kg";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem item = (OrderItem) o;
        return productId == item.productId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(productId);
    }
}
