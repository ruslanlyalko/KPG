package com.example.android.kpgukraine.models;

public class EventModel {
    public String key;
    public String title;
    public String description;
    public String objectKey;

    public EventModel() {
        // Firebase required
    }

    public EventModel( String objectKey, String title, String description) {
        this.objectKey = objectKey;
        this.title = title;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
