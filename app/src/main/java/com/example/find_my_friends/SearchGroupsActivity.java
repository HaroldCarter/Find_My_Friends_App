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
import com.example.find_my_friends.groupUtil.GroupSearchSuggestionProvider;
import com.example.find_my_friends.recyclerAdapters.GroupAdapter;
import com.example.find_my_friends.util.DatePickerFragment;
import com.example.find_my_friends.util.TimePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.SearchRecentSuggestions;
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
import java.util.ArrayList;

import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;
import uk.co.mgbramwell.geofire.android.query.GeoFireQuery;

import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;

public class SearchGroupsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");

    //private GroupOverviewAdapter groupOverviewAdapter;
    private GroupAdapter groupAdapter;
    private RecyclerView recyclerView;
    private TextView dateSpinnerSG;
    private TextView timeSpinnerSG;
    private TextView distanceText;
    private SeekBar distanceSeekBar;
    private ImageView dateSelectionCancelBTN;
    private ImageView timeSelectionCancelBTN;
    private Calendar calendar;
    private String filterGroupDate;
    private String filterGroupTime;
    private Query query;


    private SearchView searchView;
    private EditText searchViewEditText;



    private  GeoFire geoFire = new GeoFire(groupsRef);
    private double distance = 0.0;
    private String searchText = "";
    private String searchDate = "any";
    private String searchTime = "any";

    private ArrayList<Group> groups = new ArrayList<>();

    //because the activity is running in single top mode, each new launch request is managed by this F.
    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchText = intent.getStringExtra(SearchManager.QUERY);
            updateSearch();
            if(searchViewEditText != null){
                searchViewEditText.setText(searchText);
                searchViewEditText.setSelection(searchText.length());
            }


        }
        super.onNewIntent(intent);
    }

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
        dateSelectionCancelBTN = findViewById(R.id.dateSelectorCancelBTN);
        timeSelectionCancelBTN = findViewById(R.id.timeSelectorCancelBTN);

        calendar = Calendar.getInstance();
        distanceText = findViewById(R.id.DistanceSearchTitle);

        handleDistanceSeekBar();
        setupRecyclerView();
        handleDateSpinnerSG();
        handleTimeSpinnerSG();
        handleDateSelectionCancelBTN();
        handleTimeSelectionCancelBTN();
    }


    private void handleDateSelectionCancelBTN(){
        dateSelectionCancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSelectionCancelBTN.setVisibility(View.INVISIBLE);
                dateSpinnerSG.setText(("any"));
                searchDate = "any";
                updateSearch();
            }
        });
    }

    private void handleTimeSelectionCancelBTN(){
        timeSelectionCancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeSelectionCancelBTN.setVisibility(View.INVISIBLE);
                timeSpinnerSG.setText(("any"));
                searchTime = "any";
                updateSearch();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.search_aciton_bar);

        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchText = "";
                updateSearch();
                return false;
            }
        });
        //search view cannot by styled by an XML document but need to be manually styled in code.
        //therefore best to style everything here rather than fragement styling.
        if(searchView != null) {
            searchView.setBackgroundResource(R.drawable.dsg_textview_rounded_borded);

            ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
            searchIcon.setImageResource(R.drawable.svg_search_primary);

            ImageView voiceIcon = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn);
            voiceIcon.setImageResource(R.drawable.svg_voice_primary);

            ImageView closeBTN = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            closeBTN.setImageResource(R.drawable.svg_cancel_primary);


            searchViewEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchViewEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            searchViewEditText.setHintTextColor(getResources().getColor(R.color.colorAccent));
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //if the system has an error just make the search menu un-reactive rather than crash.
        if(searchManager != null && searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchGroupsActivity.class)));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    //once the user has submitted the text
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchGroupsActivity.this,GroupSearchSuggestionProvider.AUTHORITY, GroupSearchSuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query, null);
                    Snackbar.make(distanceText.getRootView(), query, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    searchText = query;
                    updateSearch();
                    //this works but need to figure out a way to custom style the suggestion, and to make it actually search once an option is clicked.
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
                int distanceInt = (int) (progress / 100.0 * 200.0);
                distanceText.setText(("Distance : " + distanceInt + "Miles"));
                distance = distanceInt;
                updateSearch();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateSearch(){
        GeoFireQuery geoFireQuery;
        Distance searchDistance = new Distance(distance, DistanceUnit.MILES);
        QueryLocation queryLocation = QueryLocation.fromDegrees(currentUser.getUserLat(), currentUser.getUserLong());
        geoFireQuery =  geoFire.query();


        if(searchDate != null && !searchDate.equals("any")){
            geoFireQuery = geoFireQuery.whereEqualTo("groupMeetDate", searchDate);
        }
        if(searchTime != null && !searchTime.equals("any")){
            geoFireQuery = geoFireQuery.whereEqualTo("groupMeetTime", searchTime);
        }
        if(searchText != null && !searchText.equals("")){
            geoFireQuery = geoFireQuery.whereArrayContains("groupTitleKeywords", searchText);
        }

        geoFireQuery.whereNearTo(queryLocation, searchDistance);

        geoFireQuery.build().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    groups.clear();
                    if(task.getResult() != null) {
                        groups.addAll(task.getResult().toObjects(Group.class));
                    }
                    //no catch required, if the group returns empty then just don't displaying anything.
                    groupAdapter.notifyDataSetChanged();
                }
            }
        });
        groupAdapter = new GroupAdapter(groups);
        groupAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchGroupsActivity.this));
        recyclerView.setAdapter(groupAdapter);
        handleAdapterOnClick();


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
        searchDate = setDate;
        updateSearch();
        dateSelectionCancelBTN.setVisibility(View.VISIBLE);
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
        searchTime = setTime;
        updateSearch();
        timeSelectionCancelBTN.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(){
        groups = new ArrayList<>();
        groupAdapter = new GroupAdapter(groups);
        groupAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchGroupsActivity.this));
        recyclerView.setAdapter(groupAdapter);
        handleAdapterOnClick();


    }


    private void handleAdapterOnClick(){
        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick( int position) {
                Intent intent = new Intent(getApplicationContext(), GroupDetailsActivity.class);
                intent.putExtra("documentID",groups.get(position).getGroupID());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

}
