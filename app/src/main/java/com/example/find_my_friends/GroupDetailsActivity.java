package com.example.find_my_friends;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.UserAdapter;
import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private LatLng groupLatLng = new LatLng(0,0);

    private String TAG = "Group Details Activity :";

    private TextView groupTitle;
    private TextView groupCreatorTitle;
    private TextView groupCreatorEmail;
    private TextView groupDate;
    private TextView groupTime;
    private TextView groupLocation;
    private TextView groupDesc;
    private ImageView groupPhoto;
    private ImageView groupCreatorPhoto;



    private UserAdapter userAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");
    private Button requestToJoinBTN;
    private Group group;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean currentUserGroupMember = false;

    private DocumentReference docRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        recyclerView = findViewById(R.id.group_details_user_recyclerview);
        Toolbar toolbar = findViewById(R.id.toolbarGD);
        toolbar.setNavigationIcon(R.drawable.svg_back_arrow_primary);
        setSupportActionBar(toolbar);
        ActionBar supportBar = getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        requestToJoinBTN = findViewById(R.id.group_details_request_to_join);


        if (supportBar != null) {
            getSupportActionBar().setTitle("");
            //default theme sets the title incorrectly

        }

        groupTitle = findViewById(R.id.group_details_title);
        groupCreatorTitle = findViewById(R.id.group_details_creator_title);
        groupCreatorEmail = findViewById(R.id.group_details_creator_email);
        groupDesc = findViewById(R.id.group_details_desc_text_box);
        groupLocation = findViewById(R.id.group_details_location_text_box);
        groupDate = findViewById(R.id.group_details_date_field);
        groupTime = findViewById(R.id.group_details_time_field);

        groupPhoto = findViewById(R.id.group_details_group_photo);
        groupCreatorPhoto = findViewById(R.id.group_creator_profile_photo);


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.group_details_map_view);
        mapView.onCreate(mapViewBundle);

        //load the group data in here then call the map ready at the end.


        handleRequestBTN();
        handleLoadingData();
        //setupRecyclerView();
    }



    public void handleLoadingData(){
        String documentID =getIntent().getStringExtra("documentID");
            if (documentID!= null){
                this.docRef = db.collection("Groups").document(documentID);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        group = documentSnapshot.toObject(Group.class);
                        updateUI();
                        mapView.getMapAsync(GroupDetailsActivity.this);
                        checkCurrentMemberIsGroupMember();
                        setupRecyclerView();
                    }
                });
            }
    }


    public void updateUI(){
        groupCreatorTitle.setText(group.getGroupCreatorDisplayName());
        //need to add email to the groups so that the group works.
        //groupCreatorEmail.setText(gro)
        groupTitle.setText(group.getGroupTitle());
        groupDesc.setText(group.getGroupDesc());
        groupDate.setText(group.getGroupMeetDate());



        groupTime.setText(group.getGroupMeetTime());
        groupLatLng = new LatLng(group.getGroupLatitude(), group.getGroupLongitude());

        Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);
        Glide.with(this).load(group.getGroupCreatorUserPhotoURL()).into(groupCreatorPhoto);

        Address addresses ;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(groupLatLng.latitude, groupLatLng.longitude, 1).get(0);
            groupLocation.setText(addresses.getAddressLine(0));
        }catch(IOException e){
            Log.e(TAG, "onClick: Error when trying to get the address, no address provided");
            groupLocation.setText(("no address set"));
        }

    }



    public Boolean getCurrentUserGroupMember() {
        return currentUserGroupMember;
    }

    private void handleRequestBTN(){
        requestToJoinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check the group isn't
                if(!getCurrentUserGroupMember()) {
                    changeRequestBTN("Requested");
                    requestToJoinGroup();

                    currentUserGroupMember = true;
                }else{
                    leaveGroup();
                    currentUserGroupMember = false;

                }
            }
        });
    }

    private void checkCurrentMemberIsGroupMember(){
        if(group.getMembersOfGroupIDS() == null){
            return;
        }
        for (String s: group.getMembersOfGroupIDS()
             ) {
            if(s.equals(user.getUid())){
                changeRequestBTN("Member");
                this.currentUserGroupMember = true;
                return;
            }
        }
        if(group.getRequestedMemberIDS() == null){
            return;
        }
        for(String s: group.getRequestedMemberIDS()){
            if(s.equals((user.getUid()))){
                changeRequestBTN("Requested");
                this.currentUserGroupMember = true;
                return;
            }
        }

    }

    private void leaveGroup(){
        if(user.getUid().equals(group.getGroupCreatorUserID())){
            Snackbar.make(requestToJoinBTN, "You cannot leave your own group", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if(group.getMembersOfGroupIDS() != null){
            group.getMembersOfGroupIDS().remove(user.getUid());
            Snackbar.make(requestToJoinBTN, "Group Left", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if(group.getRequestedMemberIDS() != null){
            group.getRequestedMemberIDS().remove(user.getUid());
            Snackbar.make(requestToJoinBTN, "Group request cancelled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
        docRef.update("membersOfGroupIDS", group.getMembersOfGroupIDS());
        changeRequestBTN("Request To Join");

    }

    private void requestToJoinGroup(){
        if(!getCurrentUserGroupMember()){
            group.appendMemberRequest(user);
            docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
        }
    }

    private void changeRequestBTN(String text){
        requestToJoinBTN.setText(text);
        requestToJoinBTN.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }
    private void setupRecyclerView(){
        //get the logical query for all users, (doesn't download them, just a logical requirement).
        Query searchQuery = userRef.orderBy("uid");
        boolean matchFound = false;
        //no need to override method and hide users if no user is found because a group will always have at-least one member.
        for (String memberID: group.getMembersOfGroupIDS()
             ) {
            //might return nothing when there are two members in a group as this logically reduces the dataset each time we call it.
               searchQuery =  userRef.whereEqualTo("uid", memberID);
                matchFound = true;
        }


        if(matchFound) {
            // Query query = userRef.whereIn("uid", group.getMembersOfGroupIDS());
            FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>().setQuery(searchQuery, User.class).build();
            userAdapter = new UserAdapter(options);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(userAdapter);
            userAdapter.startListening();
        }//otherwise don't load the recyler view at all as there has been a failure in the database.


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(groupLatLng).title(group.getGroupTitle()));

        //setting up the style of the map
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(groupLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(groupLatLng, 12));
        googleMap.setMapStyle(style);
    }

    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
        if(userAdapter != null){
            userAdapter.startListening();
        }


    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
        if(userAdapter != null) {
            userAdapter.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}