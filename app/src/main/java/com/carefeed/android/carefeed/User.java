package com.carefeed.android.carefeed;

public class User {
    private String age, introduction, profileImage, username;

    public User() {
    }

    public User(String age, String introduction, String profileImage, String username) {
        this.age = age;
        this.introduction = introduction;
        this.profileImage = profileImage;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
