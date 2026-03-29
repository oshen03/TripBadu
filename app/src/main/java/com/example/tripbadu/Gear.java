package com.example.tripbadu;

public class Gear {
    private int id;
    private String name;
    private double price;
    private String image;

    public Gear(int id, String name, double price, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
}
