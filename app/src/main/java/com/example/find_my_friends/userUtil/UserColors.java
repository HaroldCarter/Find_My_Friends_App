package com.example.find_my_friends.userUtil;


import java.util.Random;

public enum UserColors {
    red("red"), blue("blue"), green("green"), black("black"), white("white"), gray("gray"), cyan("cyan"), magenta("magenta"), yellow("yellow"), lightgray("lightgray"), darkgray("darkgray"), grey("grey"), lightgrey("lightgrey"), darkgrey("darkgrey"), aqua("aqua"), fuchsia("fuchsia"), lime("lime"), maroon("maroon"), navy("navy"), olive("olive"), purple("purple"), silver("silver"), teal("teal");

    private String stringValue;

    UserColors(String stringValueInput) {
        this.stringValue = stringValueInput;
    }

    public String getStringValue() {
        return stringValue;
    }

    static public UserColors randomColor(){
        int index = new Random().nextInt(UserColors.values().length);
        return UserColors.values()[index];
    }
}
