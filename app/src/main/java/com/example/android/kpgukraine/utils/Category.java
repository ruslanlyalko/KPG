package com.example.android.kpgukraine.utils;


public class Category {

    public String key;
    public String title;
    public String imageUri;

    public Category() {
        // Empty Constructor (Firebase required)
    }

    public Category(String key, String title, String imageUri) {
        this.key = key;
        this.title = title;
        this.imageUri = imageUri;
    }

    public Category(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
