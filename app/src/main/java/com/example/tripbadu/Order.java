package com.example.tripbadu;

import java.util.List;

public class Order {
    private String customerName;
    private String email;
    private List<Gear> items;
    private double totalAmount;

    public Order(String customerName, String email, List<Gear> items, double totalAmount) {
        this.customerName = customerName;
        this.email = email;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public List<Gear> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
}
