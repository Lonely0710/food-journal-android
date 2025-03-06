package com.example.tastylog.model;

public class FavoriteItem {
    private String id;
    private String name;
    private String imageUrl;
    private String cuisineType;
    private float rating;
    private String favoriteDate;

    public FavoriteItem(String id, String name, String imageUrl, String cuisineType, float rating, String favoriteDate) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.cuisineType = cuisineType;
        this.rating = rating;
        this.favoriteDate = favoriteDate;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getCuisineType() { return cuisineType; }
    public float getRating() { return rating; }
    public String getFavoriteDate() { return favoriteDate; }
} 