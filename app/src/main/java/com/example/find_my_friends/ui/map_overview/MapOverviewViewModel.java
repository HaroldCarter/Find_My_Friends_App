package com.example.find_my_friends.ui.map_overview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapOverviewViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapOverviewViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}