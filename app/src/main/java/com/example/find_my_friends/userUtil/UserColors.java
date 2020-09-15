package com.example.find_my_friends.userUtil;


import java.util.Random;

/**
 * an enum for the potential range of colours for the usermarker
 *
 * @author Harold Carter
 * @version 1.0
 */
public enum UserColors {
    red("red"), blue("blue"), green("green"), black("black"), white("white"), gray("gray"), cyan("cyan"), magenta("magenta"), yellow("yellow"), lightgray("lightgray"), darkgray("darkgray"), grey("grey"), lightgrey("lightgrey"), darkgrey("darkgrey"), aqua("aqua"), fuchsia("fuchsia"), lime("lime"), maroon("maroon"), navy("navy"), olive("olive"), purple("purple"), silver("silver"), teal("teal");

    private String stringValue;

    /**
     * returns the value for a given string value from the enumerated set
     *
     * @param stringValueInput the string that the value is requested from.
     */
    UserColors(String stringValueInput) {
        this.stringValue = stringValueInput;
    }

    /**
     * returns the string value for a given value from the enumerated list
     *
     * @return a string for the value of current enumerated value
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * generates a random color and returns an instance of this enum containing it
     *
     * @return instance of the enum containing the random color
     */
    static public UserColors randomColor() {
        int index = new Random().nextInt(UserColors.values().length);
        return UserColors.values()[index];
    }
}
