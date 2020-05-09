package com.example.find_my_friends.userUtil;
import java.util.ArrayList;


import static com.example.find_my_friends.userUtil.CurrentUserUtil.notifyChangeListener;

public class User {


    //maybe store a keyword of their display name so that the users can be indexed by name.

    private String UID;
    private String UserEmailAddress;
    private String UserPhotoURL;
    private String Username;
    private double UserLat;
    private double UserLong;
    //maybe change this to false, and request location permission upon pressing the button.
    private Boolean UserLocationUpToDate = true;

    private String UserColor = UserColors.randomColor().getStringValue();

    private ArrayList<String> usersMemberships;
    private ArrayList<String> usersRequestsMemberships;
    private String modeOfTransport = "Person";

    private Integer UserUpdateRate;


    public User() {
    }

    public User(String UID, String userEmailAddress, String userPhotoURL, String username) {
        this.UID = UID;
        this.UserEmailAddress = userEmailAddress;
        this.UserPhotoURL = userPhotoURL;
        this.Username = username;
    }

    public void setUser(User user){
        this.UID = user.UID;
        this.UserEmailAddress = user.UserEmailAddress;
        this.UserPhotoURL = user.UserPhotoURL;
        this.Username = user.Username;
        this.UserLat = user.UserLat;
        this.UserLong = user.UserLong;
        this.UserLocationUpToDate = user.UserLocationUpToDate;
        this.usersMemberships = user.usersMemberships;
        this.usersRequestsMemberships = user.usersRequestsMemberships;
        this.modeOfTransport = user.modeOfTransport;
        notifyChangeListener();
    }

    public Integer getUserUpdateRate() {
        return UserUpdateRate;
    }

    public void setUserUpdateRate(Integer userUpdateRate) {
        UserUpdateRate = userUpdateRate;
    }

    public String getUserColor() {
        return UserColor;
    }

    public void setUserColor(String userColor) {
        UserColor = userColor;
    }

    //user details functions.

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



    //location functions.
    public double getUserLat() {
        return UserLat;
    }

    public void setUserLat(double userLat) {
        UserLat = userLat;
    }

    public double getUserLong() {
        return UserLong;
    }

    public void setUserLong(double userLong) {
        UserLong = userLong;
    }

    public Boolean getUserLocationUpToDate() {
        return UserLocationUpToDate;
    }

    public void setUserLocationUpToDate(Boolean userLocationUpToDate) {
        UserLocationUpToDate = userLocationUpToDate;
    }



    //membership functions.

    public ArrayList<String> getUsersMemberships() {
        return usersMemberships;
    }

    public void setUsersMemberships(ArrayList<String> usersMemberships) {
        this.usersMemberships = usersMemberships;
    }

    public ArrayList<String> getUsersRequestsMemberships() {
        return usersRequestsMemberships;
    }

    public void setUsersRequestsMemberships(ArrayList<String> usersRequestsMemberships) {
        this.usersRequestsMemberships = usersRequestsMemberships;
    }


    //mode of transport functions.
    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }









}
