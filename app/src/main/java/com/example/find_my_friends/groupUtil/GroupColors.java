package com.example.find_my_friends.groupUtil;


import java.util.Random;

/**
 * an enum for the potential range of colours for the Group Marker
 *
 * @author Harold Carter
 * @version v2.0
 */
public enum GroupColors {
    red("red"), blue("blue"), green("green"), black("black"), white("white"), gray("gray"), cyan("cyan"), magenta("magenta"), yellow("yellow"), lightgray("lightgray"), darkgray("darkgray"), grey("grey"), lightgrey("lightgrey"), darkgrey("darkgrey"), aqua("aqua"), fuchsia("fuchsia"), lime("lime"), maroon("maroon"), navy("navy"), olive("olive"), purple("purple"), silver("silver"), teal("teal");

    private String stringValue;

    /**
     * default constructor for enum
     *
     * @param stringValueInput the String from which the enums instance's value will be set
     */
    GroupColors(String stringValueInput) {
        this.stringValue = stringValueInput;
    }

    /**
     * get the string value for a given enumerated value
     *
     * @return String representing the group color
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * generates a random color and returns this as an instance of the enum
     *
     * @return GroupColors instance for a random value from the enumerated set
     */
    static public GroupColors randomColor() {
        int index = new Random().nextInt(GroupColors.values().length);
        return GroupColors.values()[index];
    }
}
