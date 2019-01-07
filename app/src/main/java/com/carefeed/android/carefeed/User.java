package com.carefeed.android.carefeed;

public class User {
    private String age, introduction, profile_image, username;

    public User() {
    }

    public User(String age, String introduction, String profile_image, String username) {
        this.age = age;
        this.introduction = introduction;
        this.profile_image = profile_image;
        this.username = username;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
