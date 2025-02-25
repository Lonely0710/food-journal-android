package com.example.tastylog.model;

import java.util.List;

public class FoodItem {
    private String imageUrl;
    private String title;
    private String time;
    private float rating;
    private String price;
    private List<String> tags;

    // 构造函数
    public FoodItem(String imageUrl, String title, String time, float rating, String price, List<String> tags) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.time = time;
        this.rating = rating;
        this.price = price;
        this.tags = tags;
    }

    // Getter方法
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public float getRating() { return rating; }
    public String getPrice() { return price; }
    public List<String> getTags() { return tags; }
}