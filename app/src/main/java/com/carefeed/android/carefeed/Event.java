package com.carefeed.android.carefeed;

public class Event {

    private String title, description, date, time, image, noOfPersonJoined;


    public Event() {
    }

    public Event(String title, String description, String date, String time, String image, String noOfPersonJoined) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.image = image;
        this.noOfPersonJoined = noOfPersonJoined;
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

    public String getNoOfPersonJoined() {
        return noOfPersonJoined;
    }

    public void setNoOfPersonJoined(String noOfPersonJoined) {
        this.noOfPersonJoined = noOfPersonJoined;
    }

    public int countNoOfPersonJoined(){
        return Integer.parseInt(noOfPersonJoined);
    }
}
