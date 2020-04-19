package com.example.find_my_friends;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupOverviewAdapter;
import com.example.find_my_friends.util.DatePickerFragment;
import com.example.find_my_friends.util.TimePickerFragment;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;

import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;

public class SearchGroupsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private Button backBTN;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");

    private GroupOverviewAdapter groupOverviewAdapter;
    private RecyclerView recyclerView;
    private TextView dateSpinnerSG;
    private TextView timeSpinnerSG;
    private Calendar calendar;
    private String filterGroupDate;
    private String filterGroupTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_groups);
        backBTN = findViewById(R.id.BackButtonSearchPage);
        recyclerView = findViewById(R.id.SearchGroupRecycler);
        dateSpinnerSG = findViewById(R.id.dateSpinnerSG);
        timeSpinnerSG = findViewById(R.id.timeSpinnerSG);

        calendar = Calendar.getInstance();


        handleBackBTN();

        setupRecyclerView();

        handleDateSpinnerSG();
        handleTimeSpinnerSG();


    }

    private void handleDateSpinnerSG(){
        dateSpinnerSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),DATEPICKER_TAG_KEY);
            }
        });

    }

    private void handleTimeSpinnerSG(){
        timeSpinnerSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKER_TAG_KEY);
            }
        });
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String setDate = DateFormat.getDateInstance().format(calendar.getTime());
        this.filterGroupDate = setDate;
        dateSpinnerSG.setText(setDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String setTime;
        //if single figure
        if(minute < 9){
            setTime  = hourOfDay + ":0" + minute;
        }else{
            setTime = hourOfDay + ":" + minute;
        }
        this.filterGroupTime = setTime;
        timeSpinnerSG.setText(setTime);
    }

    private void setupRecyclerView(){
        Query query = groupsRef.orderBy("groupTitle");
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        groupOverviewAdapter = new GroupOverviewAdapter(options);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupOverviewAdapter);

        groupOverviewAdapter.setOnItemClickListener(new GroupOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
               startActivity(new Intent(getApplicationContext(), GroupDetailsActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupOverviewAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupOverviewAdapter.stopListening();
    }

    private void handleBackBTN(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
