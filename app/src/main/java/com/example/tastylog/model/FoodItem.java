package com.example.tastylog.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class FoodItem implements Parcelable {
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

    protected FoodItem(Parcel in) {
        imageUrl = in.readString();
        title = in.readString();
        time = in.readString();
        rating = in.readFloat();
        price = in.readString();
        tags = new ArrayList<>();
        in.readStringList(tags);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeFloat(rating);
        dest.writeString(price);
        dest.writeStringList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodItem> CREATOR = new Creator<FoodItem>() {
        @Override
        public FoodItem createFromParcel(Parcel in) {
            return new FoodItem(in);
        }

        @Override
        public FoodItem[] newArray(int size) {
            return new FoodItem[size];
        }
    };

    // Getter方法
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public float getRating() { return rating; }
    public String getPrice() { return price; }
    public List<String> getTags() { return tags; }

    // Setter方法
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTitle(String title) { this.title = title; }
    public void setTime(String time) { this.time = time; }
    public void setRating(float rating) { this.rating = rating; }
    public void setPrice(String price) { this.price = price; }
    public void setTags(List<String> tags) { this.tags = tags; }
}