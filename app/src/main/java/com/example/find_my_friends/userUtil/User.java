package com.example.find_my_friends.userUtil;

public class User {
    private String UID;
    private String UserEmailAddress;
    private String UserPhotoURL;
    private String Username;

    public User() {
    }

    public User(String UID, String userEmailAddress, String userPhotoURL, String username) {
        this.UID = UID;
        UserEmailAddress = userEmailAddress;
        UserPhotoURL = userPhotoURL;
        Username = username;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUserEmailAddress() {
        return UserEmailAddress;
    }

    public void setUserEmailAddress(String userEmailAddress) {
        UserEmailAddress = userEmailAddress;
    }

    public String getUserPhotoURL() {
        return UserPhotoURL;
    }

    public void setUserPhotoURL(String userPhotoURL) {
        UserPhotoURL = userPhotoURL;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}