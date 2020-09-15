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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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

import static com.example.find_my_friends.groupUtil.GroupUtil.appendCompletedMember;
import static com.example.find_my_friends.groupUtil.GroupUtil.appendMemberRequest;
import static com.example.find_my_friends.groupUtil.GroupUtil.canUserComplete;
import static com.example.find_my_friends.groupUtil.GroupUtil.isUserAMember;
import static com.example.find_my_friends.groupUtil.GroupUtil.isUserAlreadyCompleted;
import static com.example.find_my_friends.groupUtil.GroupUtil.removeCompletedMember;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.removeMembershipCurrentUser;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.removeRequestedMembershipCurrentUser;
import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;

/**
 * The Activity responsible for displaying the details of a group which UID is passed through intent upon initialisation of this class.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private LatLng groupLatLng = new LatLng(0, 0);

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
    private Button requestCompletion;
    private Group group;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean currentUserGroupMember = false;

    private DocumentReference docRef;


    /**
     * overrides the on-create function to set the internal variables to be equal to that as on screen variables and then sets their handlers and onclick callback listeners.
     *
     * @param savedInstanceState bundle to restore data from the saved instance state
     */
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
        requestCompletion = findViewById(R.id.ConfirmArrivalBTN);


        if (supportBar != null) {
            supportBar.setTitle("");
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

        handleRequestBTN();
        handleLoadingData();
        handleConfirmBTN();
    }

    /**
     * upon the user clicking the completion button (only displayed if the user is already a member) the listener within this function is called, this then checks if the member meets criteria to complete / arrive at the group meet point, if they do not meet criteria a reason why is displayed to the user using a snackbar notification
     */
    public void handleConfirmBTN() {
        requestCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canUserComplete(group, currentUser)) {
                    //if true then allow the user to complete.
                    appendCompletedMember(group, user);
                    docRef.update("completedMemberIDS", FieldValue.arrayUnion(user.getUid()));
                    requestCompletion.setText("completed");

                } else {
                    if (isUserAMember(group, currentUser)) {
                        Snackbar.make(requestCompletion, "you are not close enough to register arrival", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        Snackbar.make(requestCompletion, "you are not a member of this group", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });
    }


    /**
     * handle loading the data of the group in by getting the document reference from the bundled data passed to this activity (passed by the intent) then loads this data from the database using a document read request (loads to a local variable that represents the group) .
     */
    public void handleLoadingData() {
        String documentID = getIntent().getStringExtra("documentID");
        if (documentID != null) {
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

    /**
     * update all the onscreen data about the group upon completion of the the group load request being made, this then updates the UI to represent the group that was requested to be inspected. this function also makes another read request to get the most uptodate information regarding the creators information (so that further updates to the users profile are displayed correctly).
     */
    public void updateUI() {
        groupTitle.setText(group.getGroupTitle());
        groupDesc.setText(group.getGroupDesc());
        groupDate.setText(group.getGroupMeetDate());

        db.collection("Users").document(group.getGroupCreatorUserID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User tempUser = task.getResult().toObject(User.class);
                    if (tempUser != null) {
                        Glide.with(GroupDetailsActivity.this).load(tempUser.getUserPhotoURL()).into(groupCreatorPhoto);
                        groupCreatorEmail.setText(tempUser.getUserEmailAddress());
                        groupCreatorTitle.setText(tempUser.getUsername());

                    } else {
                        groupCreatorEmail.setText(("no email registered"));
                        groupCreatorTitle.setText(("no creator registered"));
                    }

                }
            }
        });

        Glide.with(this).load(group.getGroupCreatorUserPhotoURL()).into(groupCreatorPhoto);
        if (group.getGroupCreatorEmail() == null) {
            groupCreatorEmail.setText(("no email registered"));
        } else {
            groupCreatorEmail.setText(group.getGroupCreatorEmail());
        }


        if (isUserAMember(group, currentUser)) {
            requestCompletion.setVisibility(View.VISIBLE);
            if (isUserAlreadyCompleted(group, currentUser)) {
                requestCompletion.setText("Completed");
            }
        }


        groupTime.setText(group.getGroupMeetTime());
        groupLatLng = new LatLng(group.getGroupLatitude(), group.getGroupLongitude());

        Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);


        Address addresses;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(groupLatLng.latitude, groupLatLng.longitude, 1).get(0);
            groupLocation.setText(addresses.getAddressLine(0));
        } catch (IOException e) {
            Log.e(TAG, "onClick: Error when trying to get the address, no address provided");
            groupLocation.setText(("no address set"));
        }

    }


    /**
     *
     */
    private void handleRequestBTN() {
        requestToJoinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check the group isn't
                if (!currentUserGroupMember) {
                    changeRequestBTN("Requested");
                    requestToJoinGroup();
                    currentUserGroupMember = true;
                } else {
                    currentUserGroupMember = false;
                    leaveGroup();

                }
            }
        });
    }

    /**
     *
     */
    private void checkCurrentMemberIsGroupMember() {
        if (group.getMembersOfGroupIDS() == null) {
            return;
        }
        for (String s : group.getMembersOfGroupIDS()
        ) {
            if (s.equals(user.getUid())) {
                changeRequestBTN("Member");
                this.currentUserGroupMember = true;
                return;
            }
        }
        if (group.getRequestedMemberIDS() == null) {
            return;
        }
        for (String s : group.getRequestedMemberIDS()) {
            if (s.equals((user.getUid()))) {
                changeRequestBTN("Requested");
                this.currentUserGroupMember = true;
                return;
            }
        }

    }

    /**
     * this function is called when the user requests to leave a group, this then takes the current user saved reference from the constants and removes the current user from the group that is currently being inspected by this activity, this is achieved by purging all reference to the user from the group (apart from the group creator), this function will block the group creator from leaving their own group.
     */
    private void leaveGroup() {
        if (user.getUid().equals(group.getGroupCreatorUserID())) {
            Snackbar.make(requestToJoinBTN, "You cannot leave your own group", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            currentUserGroupMember = true;
            return;
        }
        if (group.getMembersOfGroupIDS() != null) {
            group.getMembersOfGroupIDS().remove(user.getUid());
            removeMembershipCurrentUser(group.getGroupID());
            Snackbar.make(requestToJoinBTN, "Group Left", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (group.getRequestedMemberIDS() != null) {
            group.getRequestedMemberIDS().remove(user.getUid());
            removeRequestedMembershipCurrentUser(group.getGroupID());
            Snackbar.make(requestToJoinBTN, "Group request cancelled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (isUserAlreadyCompleted(group, currentUser)) {
            removeCompletedMember(group, user);
        }
        docRef.update("requestedMemberIDS", FieldValue.arrayRemove(user.getUid()));
        docRef.update("membersOfGroupIDS", FieldValue.arrayRemove(user.getUid()));
        docRef.update("completedMemberIDS", FieldValue.arrayRemove(user.getUid()));
        changeRequestBTN("Request To Join");

    }

    /**
     * function called if the user is requesting to join a group, this is achieved by appending the user to the groups requested member id's array list of the server.
     */
    private void requestToJoinGroup() {
        if (!currentUserGroupMember) {
            appendMemberRequest(group, user);
            docRef.update("requestedMemberIDS", FieldValue.arrayUnion(user.getUid()));
        }
    }

    /**
     * A function that sets the input text field to the set text for the request button (as this button is used for requesting to join, leave depending on the users state)
     *
     * @param text String to set the button text to
     */
    private void changeRequestBTN(String text) {
        requestToJoinBTN.setText(text);
        requestToJoinBTN.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    /**
     * initialize the recycler view to display the query of the group's members, this uses the UserAdapter to display the users in the recycler view.
     */
    private void setupRecyclerView() {
        Query searchQuery = userRef.whereIn("uid", group.getMembersOfGroupIDS());
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>().setQuery(searchQuery, User.class).build();
        userAdapter = new UserAdapter(options, group);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
        userAdapter.startListening();

    }


    /**
     * overrides the callback provided by the googleplay service / google maps library to alert the program when the map is ready to be utilized/be mutated to display the app's information, a custom json style is applied to the map which switches the map into a custom dark mode. all ui settings are removed, as this clutters the view within the app.
     *
     * @param googleMap the Googlemap prepared by the google maps library from which markers can be set and polygons can be drawn (route planning).
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(groupLatLng).title(group.getGroupTitle()));
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(groupLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(groupLatLng, 12));
        googleMap.setMapStyle(style);
    }

    /**
     * overrides on start to initialize the map and to alert the user adapter to start listening for changes (firebaseUI recycler adapter, automatically updates upon change).
     */
    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
        if (userAdapter != null) {
            userAdapter.startListening();
        }


    }

    /**
     * overrides on resume to resume the map.
     */
    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    /**
     * overrides on resume to pause the map(maps are resource heavy therefore best to pause whenever possible)
     */
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    /**
     * overrides on stop to stop the map and to alert the user adapter to stop listening for changes (firebaseUI recycler adapter, automatically updates upon change, causes null pointer errors if this line is deleted).
     */
    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
        if (userAdapter != null) {
            userAdapter.stopListening();
        }
    }

    /**
     * overrides on destroy to destroy the map
     */
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    /**
     * saves the instance state of the map to the bundle through a custom deserializer for the map, however this is not required due to this overcomplicating the zoom functionality of the map and therefore may be better to let the map reload upon save instance.
     *
     * @param outState the bundle to be saved by the activity
     */
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

    /**
     * overrides on low memory to set the map into low memory utilization mode (doesn't update as frequently)
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}