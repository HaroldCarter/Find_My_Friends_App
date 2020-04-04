package com.example.find_my_friends.ui.group_requests;

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

public class GroupRequestsFragment extends Fragment {

    private GroupRequestsViewModel groupRequestsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        groupRequestsViewModel =
                ViewModelProviders.of(this).get(GroupRequestsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_group_requests, container, false);
        final TextView textView = root.findViewById(R.id.text_share);
        groupRequestsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}