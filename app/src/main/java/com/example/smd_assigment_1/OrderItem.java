package com.example.smd_assigment_1;

public class OrderItem {
    private String productName;
    private int quantity;
    private double price;

    public OrderItem() {}

    public OrderItem(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
