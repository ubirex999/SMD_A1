package com.example.smd_assigment_1;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String buyerName;
    private String date;
    private double totalAmount;
    private List<OrderItem> items;
    private String status;

    public Order() {}

    public Order(String orderId, String userId, String buyerName, String date, double totalAmount, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.buyerName = buyerName;
        this.date = date;
        this.totalAmount = totalAmount;
        this.items = items;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
