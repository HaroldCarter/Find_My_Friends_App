package com.example.find_my_friends;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.find_my_friends.util.DatePickerFragment;
import com.example.find_my_friends.util.TimePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;

import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;

public class AddGroupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private final Calendar groupCalender = Calendar.getInstance();
    private TextView dateSpinnerAG;
    private TextView timeSpinnerAG;
    private Button addLocationButton;
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

        dateSpinnerAG = (TextView) findViewById(R.id.dateSpinnerAG);
        timeSpinnerAG = (TextView) findViewById(R.id.timeSpinnerAG);

        addLocationButton = (Button) findViewById(R.id.addLocationAG);

        dateSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),DATEPICKER_TAG_KEY);
            }
        });


        timeSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKER_TAG_KEY);
            }
        });

        addBackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //maybe save the group information ?
                finish();
            }
        });

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SetLocationActivity.class));
            }
        });

    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        groupCalender.set(Calendar.YEAR, year);
        groupCalender.set(Calendar.MONTH, month);
        groupCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String setDate = DateFormat.getDateInstance().format(groupCalender.getTime());
        dateSpinnerAG.setText(setDate);



    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        groupCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
        groupCalender.set(Calendar.MINUTE, minute);
        String setTime = hourOfDay + ":" + minute;
        timeSpinnerAG.setText(setTime);
    }
}
