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

    public Gear() {} // Default constructor required for Firebase

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
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
