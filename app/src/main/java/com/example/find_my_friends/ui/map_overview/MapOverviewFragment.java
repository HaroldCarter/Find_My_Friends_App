package com.example.find_my_friends.ui.map_overview;

import android.content.Intent;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;

public class MapOverviewFragment extends Fragment {

    private MapOverviewViewModel mapOverviewViewModel;
    private boolean gpsToggle = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapOverviewViewModel =
                ViewModelProviders.of(this).get(MapOverviewViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map_overview, container, false);

        final TextView textView = root.findViewById(R.id.text_gallery);
        mapOverviewViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        FloatingActionButton addGroupPhotoFAB = (FloatingActionButton) root.findViewById(R.id.add_group_fab_map_overview);
        addGroupPhotoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity().getApplicationContext(), AddGroupActivity.class));
                } else {
                    Snackbar.make(view, "Main activity has terminated, app will crash.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton navigationDrawFAB = (FloatingActionButton) root.findViewById(R.id.nav_draw_fab_map_overview);
        navigationDrawFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });


        final FloatingActionButton gpsToggleFAB = (FloatingActionButton) root.findViewById(R.id.location_toggle_map_overview);
        gpsToggleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the GPS to grey & stop updating their location periodically (this will require realtime user database to implement this)
                if(gpsToggle){
                    gpsToggleFAB.setImageAlpha(50);
                    gpsToggle = false;
                }else{
                    gpsToggleFAB.setImageAlpha(255);
                    gpsToggle = true;
                }

            }
        });

        return root;
    }
}