package com.example.smd_assigment_1;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String date;
    private double totalAmount;
    private List<OrderItem> items;
    private String status;

    public Order() {}

    public Order(String orderId, String userId, String date, double totalAmount, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.date = date;
        this.totalAmount = totalAmount;
        this.items = items;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public String getStatus() { return status; }
}
