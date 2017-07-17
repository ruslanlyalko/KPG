package com.example.android.kpgukraine.utils;

// Object Model
public class ObjectModel {

    public String key;
    public String title;
    public String imageUri;
    public String subCategoryName;
    public String subCategoryKey;

    public String location;
    public String timeOpened;
    // TODO add others fields


    public ObjectModel() {
        // Empty Constructor (Firebase required)
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

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryKey() {
        return subCategoryKey;
    }

    public void setSubCategoryKey(String subCategoryKey) {
        this.subCategoryKey = subCategoryKey;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeOpened() {
        return timeOpened;
    }

    public void setTimeOpened(String timeOpened) {
        this.timeOpened = timeOpened;
    }
}
