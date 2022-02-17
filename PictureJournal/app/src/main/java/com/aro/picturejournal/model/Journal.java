package com.aro.picturejournal.model;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.List;

public class Journal {
    /*
    this is the data class (model) for journals
     */

    private String title;
    private String entry;
    private String imageUrl;
    private String userId;
    private Timestamp timeAdded;
    private String username;
    private List<String> tags;
    private String uniqueRefName;



    //must have empty constructor for firebase
    public Journal() {}


    public Journal(String title, String entry, String imageUrl, String userId, Timestamp timeAdded, String username, List<String> tags, String uniqueRefName) {
        this.title = title;
        this.entry = entry;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.username = username;
        this.tags = tags;
        this.uniqueRefName = uniqueRefName;


    }

    @NonNull
    @Override
    public String toString() {
        return "Journal{" +
                "title='" + title + '\'' +
                ", entry='" + entry + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", userId='" + userId + '\'' +
                ", timeAdded=" + timeAdded +
                ", username='" + username + '\'' +
                ", tags=" + tags +
                ", uniqueRefName='" + uniqueRefName + '\'' +
                '}';
    }

    public String getUniqueRefName() {
        return uniqueRefName;
    }

    public void setUniqueRefName(String uniqueRefName) {
        this.uniqueRefName = uniqueRefName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
