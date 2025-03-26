package com.example.tastylog.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 食物数据模型
 * 包含食物的所有属性和转换方法
 */
public class FoodItem implements Parcelable, Serializable {
    private String id;
    private String title;
    private String time;
    private float rating;
    private String price;
    private List<String> tags;
    private String imageUrl;
    private String notes;
    private String location;
    private String content = "";
    private String documentId;

    // 默认构造函数
    public FoodItem() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
    }

    // 带参数的构造函数
    public FoodItem(String title, String time, float rating, String price, List<String> tags, String imageUrl, String notes, String location) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.time = time;
        this.rating = rating;
        this.price = price;
        this.tags = tags;
        this.imageUrl = imageUrl;
        this.notes = notes;
        this.location = location;
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Parcelable实现
    protected FoodItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        time = in.readString();
        rating = in.readFloat();
        price = in.readString();
        tags = new ArrayList<>();
        in.readStringList(tags);
        imageUrl = in.readString();
        notes = in.readString();
        location = in.readString();
        content = in.readString();
        documentId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeFloat(rating);
        dest.writeString(price);
        dest.writeStringList(tags);
        dest.writeString(imageUrl);
        dest.writeString(notes);
        dest.writeString(location);
        dest.writeString(content);
        dest.writeString(documentId);
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
}