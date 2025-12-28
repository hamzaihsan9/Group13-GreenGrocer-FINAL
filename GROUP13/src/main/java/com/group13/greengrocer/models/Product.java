package com.group13.greengrocer.models;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

public class Product {
    private int id;
    private String name;
    private String type;
    private double price;
    private double stock;
    private byte[] imageData;
    private double threshold;

    public Product() {}

    public Product(int id, String name, String type, double price, double stock,
                   byte[] imageData, double threshold) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.imageData = imageData;
        this.threshold = threshold;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public byte[] getImage() { return imageData; }
    public void setImage(byte[] image) { this.imageData = image; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public double getEffectivePrice() {
        return (stock <= threshold && stock > 0) ? price * 2 : price;
    }

    public Image getJavaFXImage() {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null;
    }

    public boolean isLowStock() {
        return stock <= threshold;
    }

    @Override
    public String toString() {
        return name;
    }
}
