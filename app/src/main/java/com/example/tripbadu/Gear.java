package com.example.tripbadu;

public class Gear {
    private int id;
    private String name;
    private double price;
    private String image;
    private double latitude;
    private double longitude;
    private String contact;
    private String status;
    private String ownerEmail;

    public Gear(int id, String name, double price, String image) {
        this(id, name, price, image, 0, 0, "");
    }

    public Gear(int id, String name, double price, String image, double latitude, double longitude, String contact) {
        this(id, name, price, image, latitude, longitude, contact, "approved", "");
    }

    public Gear(int id, String name, double price, String image, double latitude, double longitude, String contact, String status, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.status = status;
        this.ownerEmail = ownerEmail;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getContact() { return contact; }
    public String getStatus() { return status; }
    public String getOwnerEmail() { return ownerEmail; }
}
