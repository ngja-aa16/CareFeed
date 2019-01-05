package com.carefeed.android.carefeed;

public class Post {
    private String date, description, postImage, time, title, userID, userProfileImage, username;

    public Post() {
    }

    public Post(String date, String description, String postImage, String time, String title, String userID, String userProfileImage, String username) {
        this.date = date;
        this.description = description;
        this.postImage = postImage;
        this.time = time;
        this.title = title;
        this.userID = userID;
        this.userProfileImage = userProfileImage;
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
