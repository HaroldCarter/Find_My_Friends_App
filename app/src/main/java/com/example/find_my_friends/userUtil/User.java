package com.example.find_my_friends.userUtil;

import java.util.ArrayList;


import static com.example.find_my_friends.userUtil.CurrentUserUtil.notifyChangeListener;

/**
 * note - all functions such as setters and getters are used by the serialisation from the database, meaning that even though they are not strictly used by my program they are critical to the functionality of the application
 * A class representative of the User's of the application, this class stores relevant material in order to provide a platform from which the app can use to store its users, information such as UID, Email address, Username and location are all stored locally as an object.
 *
 * this object is also uploaded to the Firestore Database so that cross-platform consistency is maintained
 *
 * @author Harold Carter
 * @version v3.0
 */
public class User {


    private String UID;
    private String UserEmailAddress;
    private String UserPhotoURL;
    private String Username;
    private double UserLat;
    private double UserLong;
    private Boolean UserLocationUpToDate = true;

    private String UserColor = UserColors.randomColor().getStringValue();

    private ArrayList<String> usersMemberships;
    private ArrayList<String> usersRequestsMemberships;
    private String modeOfTransport = "Person";

    private Integer UserUpdateRate;


    /**
     * default blank constructor for the user class
     */
    public User() {
    }

    /**
     * overloaded constructor for the user class, allows some of the critical information about the user to be set upon initialization
     *
     * @param UID              String the unique id of the user you which to create (UUID)
     * @param userEmailAddress String the users registered Email address
     * @param userPhotoURL     String the URI containing the users profile photo
     * @param username         String the users display name
     */
    public User(String UID, String userEmailAddress, String userPhotoURL, String username) {
        this.UID = UID;
        this.UserEmailAddress = userEmailAddress;
        this.UserPhotoURL = userPhotoURL;
        this.Username = username;
    }

    /**
     * a function which allows all internal functions of a user to be set by handing a reference of an existing user.
     *
     * @param user User the user from which the internal variables will be copied
     */
    public void setUser(User user) {
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

    /**
     * gets the user's current update rate
     *
     * @return INT representing the current update rate of the user
     */
    public Integer getUserUpdateRate() {
        return UserUpdateRate;
    }

    /**
     * sets the update rate of the user
     *
     * @param userUpdateRate new update rate of the user
     */
    public void setUserUpdateRate(Integer userUpdateRate) {
        UserUpdateRate = userUpdateRate;
    }

    /**
     * gets the string which represents the color the user currently has selected
     *
     * @return the current users color String
     */
    public String getUserColor() {
        return UserColor;
    }

    /**
     * set the users color to the string passed as a parameter (note this should be from the enumerated set otherwise the app cannot interpret the color)
     *
     * @param userColor String representing the new color of the user
     */
    public void setUserColor(String userColor) {
        UserColor = userColor;
    }

    /**
     * returns the string representing the UUID of the user
     *
     * @return returns the string representing the UUID of the user
     */
    public String getUID() {
        return UID;
    }

    /**
     * sets the UID of the user, this should only be used when initialized a blank user, as changing UUID of an existing user will cause errors
     *
     * @param UID the string representing the UUID for the user
     */
    public void setUID(String UID) {
        this.UID = UID;
    }

    /**
     * returns the user's current email addresss
     *
     * @return returns the user's current email addresss
     */
    public String getUserEmailAddress() {
        return UserEmailAddress;
    }

    /**
     * sets the users current email address, note this shouldn't be changed without also changing the authentication layer's reference to the users email otherwise this could cause discrepancies between the database and the authentication layer
     *
     * @param userEmailAddress String input for the users new email address
     */
    public void setUserEmailAddress(String userEmailAddress) {
        UserEmailAddress = userEmailAddress;
    }

    /**
     * gets the users current photoURL
     *
     * @return returns a string representing the current users URI for the profile photo (public download URI)
     */
    public String getUserPhotoURL() {
        return UserPhotoURL;
    }

    /**
     * sets the users photo URI, note again this should be updated within the authentication layer before changing this variable as this could lead to discrepancies
     *
     * @param userPhotoURL String input for the new user Photo URI
     */
    public void setUserPhotoURL(String userPhotoURL) {
        UserPhotoURL = userPhotoURL;
    }

    /**
     * get the users current username (display name)
     *
     * @return String for the users current display name
     */
    public String getUsername() {
        return Username;
    }

    /**
     * set the users display name, this shouldn't be set without also changing the authentication layers value for this variable
     *
     * @param username string representing the new username (display name)
     */
    public void setUsername(String username) {
        Username = username;
    }


    /**
     * get the user's current latitude
     *
     * @return returns a double for the users latitude
     */
    public double getUserLat() {
        return UserLat;
    }

    /**
     * set the users latitude to that of the input
     *
     * @param userLat double representing the users new latitude
     */
    public void setUserLat(double userLat) {
        UserLat = userLat;
    }

    /**
     * get the user's current longitude
     *
     * @return a double representing the users current longitude
     */
    public double getUserLong() {
        return UserLong;
    }

    /**
     * set the user's longitude to the input double
     *
     * @param userLong double representing the users longitude
     */
    public void setUserLong(double userLong) {
        UserLong = userLong;
    }

    /**
     * get state of the given user's location, if it is uptodate this will return true, else false
     *
     * @return boolean the current update state
     */
    public Boolean getUserLocationUpToDate() {
        return UserLocationUpToDate;
    }

    /**
     * set the users update state to a new state
     *
     * @param userLocationUpToDate boolean for if the users location is currently uptodate or not  (if it is uptodate then true, else false)
     */
    public void setUserLocationUpToDate(Boolean userLocationUpToDate) {
        UserLocationUpToDate = userLocationUpToDate;
    }


    /**
     * get the user's current memberships
     *
     * @return ArrayList String containing the UID of the groups this users is a member of
     */
    public ArrayList<String> getUsersMemberships() {
        return usersMemberships;
    }

    /**
     * set the user's membership array list to an inputted arraylist
     *
     * @param usersMemberships array list of strings containing the group UID's which the user is currently a member of
     */
    public void setUsersMemberships(ArrayList<String> usersMemberships) {
        this.usersMemberships = usersMemberships;
    }

    /**
     * get the arraylist for the current requested memberships this user has (groupIDS)
     *
     * @return arraylist of strings containing the groupid's this user is currently a requested to be a member of
     */
    public ArrayList<String> getUsersRequestsMemberships() {
        return usersRequestsMemberships;
    }

    /**
     * set the array list of current requested memberships
     *
     * @param usersRequestsMemberships arrayList String for the groupID's the user has requested to join
     */
    public void setUsersRequestsMemberships(ArrayList<String> usersRequestsMemberships) {
        this.usersRequestsMemberships = usersRequestsMemberships;
    }


    /**
     * get the current mode of transport the user is using
     *
     * @return String representing the current mode of transport (Car,Bike,Person)
     */
    public String getModeOfTransport() {
        return modeOfTransport;
    }

    /**
     * set the current mode of transport for the user
     *
     * @param modeOfTransport String representing the current mode of transport for the user (Car,Bike,Person)
     */
    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }


}
