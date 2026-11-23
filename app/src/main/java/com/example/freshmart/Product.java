package com.example.freshmart;

public class Product {
    public int id;
    public String name, category, emoji;
    public double price;

    public Product(int id, String name, String category, double price, String emoji) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.emoji = emoji;
    }
}
