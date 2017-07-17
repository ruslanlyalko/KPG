package com.example.android.kpgukraine.utils;


public class SubCategory {

    String key;
    String title;
    String imageUri;
    String categoryKey;
    String categoryTitle;

    public SubCategory() {
        // Empty Constructor (Firebase required)
    }

    public SubCategory(String key, String title, String imageUri, String categoryKey, String categoryTitle) {
        this.key = key;
        this.title = title;
        this.imageUri = imageUri;
        this.categoryKey = categoryKey;
        this.categoryTitle = categoryTitle;
    }


    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
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
