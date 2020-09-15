package com.example.find_my_friends.groupUtil;

import android.content.SearchRecentSuggestionsProvider;
/**
 * sa class which provides the search suggestions for the search bar.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class GroupSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.find_my_friends";
    public final static int MODE = DATABASE_MODE_QUERIES;

    /**
     * construct a default groupsearchsuggestion provider, required by the searchprovider to be implemented in this manner, this allows for a search history to be display on the search groups page.
     */
    public GroupSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }


}