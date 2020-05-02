package com.example.find_my_friends.groupUtil;

import android.content.Context;

import androidx.appcompat.widget.SearchView;


public class GroupSearchView extends SearchView {

    OnSearchViewCollapsedEventListener mSearchViewCollapsedEventListener;
    OnSearchViewExpandedEventListener mOnSearchViewExpandedEventListener;

    public GroupSearchView(Context context) {
        super(context);
    }

    @Override
    public void onActionViewCollapsed() {
        if (mSearchViewCollapsedEventListener != null)
            mSearchViewCollapsedEventListener.onSearchViewCollapsed();
        super.onActionViewCollapsed();
    }

    @Override
    public void onActionViewExpanded() {
        if (mOnSearchViewExpandedEventListener != null)
            mOnSearchViewExpandedEventListener.onSearchViewExpanded();
        super.onActionViewExpanded();
    }

    public interface OnSearchViewCollapsedEventListener {
        public void onSearchViewCollapsed();
    }

    public interface OnSearchViewExpandedEventListener {
        public void onSearchViewExpanded();
    }

    public void setOnSearchViewCollapsedEventListener(OnSearchViewCollapsedEventListener eventListener) {
        mSearchViewCollapsedEventListener = eventListener;
    }

    public void setOnSearchViewExpandedEventListener(OnSearchViewExpandedEventListener eventListener) {
        mOnSearchViewExpandedEventListener = eventListener;
    }


}