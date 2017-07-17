package com.example.android.kpgukraine.models;

// Object Model
public class ObjectModel {

    public String key;
    public String subCategoryKey;

    public String title;
    public String description;
    public String address;
    public String dinner;
    public String phone;
    public String timeOpened;
    public String imageUri;
    public String latitude;
    public String longitude;

    public ObjectModel() {
        // Empty Constructor (Firebase required)
    }

    public ObjectModel( String subCategoryKey, String title, String description, String address, String dinner, String phone, String timeOpened, String imageUri, String latitude, String longitude) {
        this.subCategoryKey = subCategoryKey;
        this.title = title;
        this.description = description;
        this.address = address;
        this.dinner = dinner;
        this.phone = phone;
        this.timeOpened = timeOpened;
        this.imageUri = imageUri;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ObjectModel(String objectKey, String title, String imageUri, String subCategoryKey) {
        this.key = objectKey;
        this.subCategoryKey = subCategoryKey;
        this.title = title;
        this.imageUri = imageUri;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSubCategoryKey() {
        return subCategoryKey;
    }

    public void setSubCategoryKey(String subCategoryKey) {
        this.subCategoryKey = subCategoryKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDinner() {
        return dinner;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTimeOpened() {
        return timeOpened;
    }

    public void setTimeOpened(String timeOpened) {
        this.timeOpened = timeOpened;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
