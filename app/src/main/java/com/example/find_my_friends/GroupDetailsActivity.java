package com.example.find_my_friends;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class GroupDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = findViewById(R.id.toolbarGD);
        setSupportActionBar(toolbar);
        ActionBar supportBar = getSupportActionBar();

        if(supportBar != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setIcon(R.drawable.svg_back_arrow_primary);
        }



    }



}
