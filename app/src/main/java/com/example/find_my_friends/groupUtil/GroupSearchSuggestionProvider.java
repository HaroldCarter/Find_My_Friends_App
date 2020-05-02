package com.example.find_my_friends.groupUtil;

import android.content.SearchRecentSuggestionsProvider;

import static android.content.SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;

public class GroupSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.find_my_friends";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public GroupSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }


}