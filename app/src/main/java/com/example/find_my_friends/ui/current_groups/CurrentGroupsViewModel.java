package com.example.find_my_friends.ui.current_groups;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CurrentGroupsViewModel extends ViewModel {


    private MutableLiveData<String> mText;

    public CurrentGroupsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}