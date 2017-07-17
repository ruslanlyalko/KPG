package com.example.android.kpgukraine.models;


public class SubCategory {

    public String key;
    public String title;
    public String imageUri;
    public String categoryKey;

    public SubCategory() {
        // Empty Constructor (Firebase required)
    }

    public SubCategory(String key, String title, String imageUri, String categoryKey) {
        this.key = key;
        this.title = title;
        this.imageUri = imageUri;
        this.categoryKey = categoryKey;
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

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }
}
