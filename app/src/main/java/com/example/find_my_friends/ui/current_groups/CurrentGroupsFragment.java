package com.example.find_my_friends.ui.current_groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.find_my_friends.R;

public class CurrentGroupsFragment extends Fragment {

    private CurrentGroupsViewModel currentGroupsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentGroupsViewModel =
                ViewModelProviders.of(this).get(CurrentGroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_current_groups, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        currentGroupsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}