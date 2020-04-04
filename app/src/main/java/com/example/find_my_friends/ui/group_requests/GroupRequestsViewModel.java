package com.example.find_my_friends.ui.group_requests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GroupRequestsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GroupRequestsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is share fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}