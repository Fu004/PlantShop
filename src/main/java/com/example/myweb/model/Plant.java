package com.example.myweb.model;

public class Plant {
    private String name;
    private String imageUrl;
    private double price;

    public Plant(String name, String imageUrl, double price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }
}
