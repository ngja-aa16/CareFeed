package com.carefeed.android.carefeed;

public class Event {

    private String title, description, date, time, image, status;
    private int noOfPersonJoined;

    public Event() {
    }

    public Event(String title, String description, String date, String time, String image, int noOfPersonJoined, String status) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.image = image;
        this.noOfPersonJoined = noOfPersonJoined;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNoOfPersonJoined() {
        return noOfPersonJoined;
    }

    public void setNoOfPersonJoined(int noOfPersonJoined) {
        this.noOfPersonJoined = noOfPersonJoined;
    }
}
