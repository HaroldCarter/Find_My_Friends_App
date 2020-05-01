package com.example.find_my_friends;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupOverviewAdapter;
import com.example.find_my_friends.util.DatePickerFragment;
import com.example.find_my_friends.util.TimePickerFragment;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;



import java.text.DateFormat;

import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;

public class SearchGroupsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");

    private GroupOverviewAdapter groupOverviewAdapter;
    private RecyclerView recyclerView;
    private TextView dateSpinnerSG;
    private TextView timeSpinnerSG;
    private TextView distanceText;
    private SeekBar distanceSeekBar;
    private Calendar calendar;
    private String filterGroupDate;
    private String filterGroupTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_groups);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Groups");
        }


        recyclerView = findViewById(R.id.SearchGroupRecycler);
        dateSpinnerSG = findViewById(R.id.dateSpinnerSG);
        timeSpinnerSG = findViewById(R.id.timeSpinnerSG);
        distanceSeekBar = findViewById(R.id.SearchDistanceSeekBar);
        calendar = Calendar.getInstance();
        distanceText = findViewById(R.id.DistanceSearchTitle);



        handleDistanceSeekBar();
        setupRecyclerView();
        handleDateSpinnerSG();
        handleTimeSpinnerSG();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.search_aciton_bar);

        SearchView searchView = (SearchView) menuItem.getActionView();
        //search view cannot by styled by an XML document but need to be manually styled in code.
        //therefore best to style everything here rather than fragement styling.
        searchView.setBackgroundResource(R.drawable.dsg_textview_rounded_borded);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setImageResource(R.drawable.svg_search_primary);

        ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
        voiceIcon.setImageResource(R.drawable.svg_voice_primary);

        ImageView closeBTN = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeBTN.setImageResource(R.drawable.svg_cancel_primary);

        EditText editText =  searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        editText.setHintTextColor(getResources().getColor(R.color.colorAccent));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //if the system has an error just make the search menu un-reactive rather than crash.
        if(searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchGroupsActivity.class)));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //once the user has submitted the text
                    Snackbar.make(distanceText.getRootView(), query, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

        }

        return super.onCreateOptionsMenu(menu);
    }


    private void handleDistanceSeekBar(){
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress != 100) {
                    int distance = (int) (progress / 100.0 * 300.0);
                    distanceText.setText(("Distance : " + distance + "Miles"));
                }else{
                    distanceText.setText(("Distance : " +" INF"));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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




        Query query = groupsRef.whereArrayContains("groupTitleKeywords", "test");
        //Query query = groupsRef.whereGreaterThan("groupLatitude",bounds.get(0)).whereLessThan("groupLatitude",bounds.get(1));
        //.whereGreaterThan("groupLongitude", bounds.get(2)).whereLessThan("groupLongitude", bounds.get(3));
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        groupOverviewAdapter = new GroupOverviewAdapter(options);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupOverviewAdapter);

        groupOverviewAdapter.setOnItemClickListener(new GroupOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getApplicationContext(), GroupDetailsActivity.class);
                intent.putExtra("documentID",documentSnapshot.getId());
                startActivity(intent);
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

}
