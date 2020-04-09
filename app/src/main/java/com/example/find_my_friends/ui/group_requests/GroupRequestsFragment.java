package com.example.find_my_friends.ui.group_requests;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;

public class GroupRequestsFragment extends Fragment {

    private GroupRequestsViewModel groupRequestsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        groupRequestsViewModel =
                ViewModelProviders.of(this).get(GroupRequestsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_group_requests, container, false);
        Toolbar toolbar = root.findViewById(R.id.groups_request_menubar);


        // getActivity().getActionBar().setIcon(R.drawable.svg_menu_primary);
        // getActivity().getActionBar().setTitle("Current Groups");

        //setting the generic toolbars settings.
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Group Requests");


        if(getActivity() != null) {
            getActivity().setActionBar(toolbar);
        }else{
            //display an error saying the program has lost reference to itself.
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
        return root;
    }
}