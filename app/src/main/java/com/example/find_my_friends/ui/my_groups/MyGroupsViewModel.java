package com.example.find_my_friends.ui.my_groups;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyGroupsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MyGroupsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}