package com.example.find_my_friends;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class AddGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);


        FloatingActionButton addGroupPhotoFAB = (FloatingActionButton) findViewById(R.id.AddGroupPhotoFABAG);
        addGroupPhotoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton addBackFAB = (FloatingActionButton) findViewById(R.id.AddGroupBackFBAG);

        addBackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //maybe save the group information ?
                finish();
            }
        });

    }
}
