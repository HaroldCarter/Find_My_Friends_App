package com.example.find_my_friends.ui.map_overview;
import android.content.Context;
import android.content.Intent;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.GroupDetailsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.SearchGroupsActivity;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.groupUtil.GroupInfoWindowAdapter;
import com.example.find_my_friends.groupUtil.GroupInfoWindowData;
import com.example.find_my_friends.groupUtil.GroupMarker;
import com.example.find_my_friends.userUtil.CurrentUserUtil;
import com.example.find_my_friends.userUtil.User;

import com.example.find_my_friends.userUtil.UserMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.find_my_friends.userUtil.CurrentUserUtil.setCurrentUserListener;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.setLocationUpToDateCurrentUser;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.setModeOfTransportCurrentUser;
import static com.example.find_my_friends.userUtil.UserUtil.getUserLocation;
import static com.example.find_my_friends.util.Constants.CurrentUserLoaded;
import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;

public class MapOverviewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private MapOverviewViewModel mapOverviewViewModel;
    private boolean gpsToggle = true;
    private boolean modeTransportMenuVisibility = false;
    private int modeTransportState = 0; //0 walking, 1 driving, 2 biking.
    private ConstraintLayout floatingMenuBackground;
    private FloatingActionButton modeTransportFAB;
    private FloatingActionButton actionMenuFAB1;
    private FloatingActionButton actionMenuFAB2;
    private FloatingActionButton searchFAB;
    private View root;
    private MapView mapView;
    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private FloatingActionButton gpsToggleFAB;
    private FloatingActionButton addGroupPhotoFAB;
    private FloatingActionButton navigationDrawFAB;

    private MarkerOptions currentLocation;
    private Marker currentLocationMarker;
    private Marker selectedMarker;

    private ArrayList<GroupMarker> currentGroupMarkers;
    private HashMap<String, Integer> groupMarkersHashMaps = new HashMap<>();
    private HashMap<String, Integer> userMarkerHashMaps = new HashMap<>();
    private boolean groupInspected = false;
    private boolean groupHighlighted = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        mapOverviewViewModel =
                ViewModelProviders.of(this).get(MapOverviewViewModel.class);
        root = inflater.inflate(R.layout.fragment_map_overview, container, false);

        locateResources();


        //mapview bundle key (restore the map state upon reopening the fragment).
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);



        //set to hide for the default and then override this later once data has been checked
        hideMenu();

        //handle on screen interactions (onclick listeners)
        handleNavDrawFAB();
        handleGPSToggleFAB();
        handleAddGroupFAB();
        handleSearchFAB();
        handleModeTransportSelection();
        //check from the server the current mode of transport for the current user.
        checkStateOfTransport();


        //listen out for changes to the user.
        currentUserListener();
        //if the current user is loaded, then start the map services.
        if(CurrentUserLoaded){
            loadGpsState();
            loadModeOfTransportSelection();
            mapView.getMapAsync(MapOverviewFragment.this);
        }

        return root;
    }


    private void currentUserListener(){
        setCurrentUserListener(new CurrentUserUtil.ChangeListener() {
            @Override
            public void onChange() {
                //update the ui
                loadGpsState();
                loadModeOfTransportSelection();
                updateCurrentLocation();
                loadIcon(currentLocationMarker ,currentUser.getModeOfTransport());
            }
        });
    }

    private void updateCurrentLocation(){
        if(currentLocationMarker != null){
            currentLocationMarker.setPosition(new LatLng(currentUser.getUserLat(), currentUser.getUserLong()));
        }
    }

    private void loadGroups(GoogleMap googleMap){
        if(currentUser.getUsersMemberships() != null) {
            currentGroupMarkers = new ArrayList<>();
            groupMarkersHashMaps.clear();
            for (String s : currentUser.getUsersMemberships()
            ) {
                db.collection("Groups").document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            Group tempGroup = documentSnapshot.toObject(Group.class);
                            if (tempGroup != null) {
                                Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(tempGroup.getGroupLatitude(), tempGroup.getGroupLongitude())).title(tempGroup.getGroupTitle()));
                                GroupInfoWindowData groupInfoWindowData = new GroupInfoWindowData(tempGroup.getGroupID(), null, null, tempGroup.getGroupTitle(), tempGroup.getGroupCreatorUserPhotoURL(), tempGroup.getGroupCreatorDisplayName());
                                marker.setIcon(bitmapDescriptorFromVector(MapOverviewFragment.this.getContext(),(R.drawable.svg_location_white)));
                                marker.setTag(groupInfoWindowData);
                                GroupMarker groupMarker = new GroupMarker(marker, tempGroup);
                                currentGroupMarkers.add(groupMarker);
                                groupMarkersHashMaps.put(marker.getId(), currentGroupMarkers.indexOf(groupMarker));
                            }

                        }
                    }
                });


            }
        }
    }

    //hide all the user markers on the screen, and display all the group markers; because only one group can be selected at a time it may be an idea just to deselect just that items users.
    public void resumeGroupOverview(){
        for (GroupMarker groupMarker: currentGroupMarkers
             ) {
            groupMarker.getGroupMarker().setVisible(true);
            for (UserMarker user: groupMarker.getUsers()
                 ) {
                user.getUserMarker().setVisible(false);
            }
        }
    }


    //whenever a marker is interacted with the callback is given to this function therefore allowing us to actively work out which marker was interacted with.
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!checkIfMakerGroupMarker(marker)){
            checkIfMarkerUserMarker(marker);
        }
        return false;
    }



    private boolean checkIfMakerGroupMarker(Marker marker) {
        if (marker != null && groupMarkersHashMaps != null && currentGroupMarkers != null) {
            Integer index = groupMarkersHashMaps.get(marker.getId());
            if (index != null) {
                currentLocationMarker.setVisible(false);
                hideAllGroupMarkersButCurrent(index);
                groupInspected = true;
                this.selectedMarker = marker;
                navigationDrawFAB.setImageResource(R.drawable.svg_back_arrow_white);
                generateUserMarkers(index);
                return true;
            }
        }
        return false;
    }


    private void hideAllGroupMarkersButCurrent(Integer index){
        GroupMarker selectedMarker = currentGroupMarkers.get(index);
        //hide all the other group markers.
        for (GroupMarker cGM : currentGroupMarkers
        ) {
            if (!cGM.getGroupMarker().getId().equals(selectedMarker.getGroupMarker().getId())) {
                cGM.getGroupMarker().setVisible(false);
            }
        }
    }


    private void generateUserMarkers(Integer index){
        userMarkerHashMaps.clear();
        //load the group's users and display them in on the map.
        for (String s : currentGroupMarkers.get(index).getGroupMarkerRepresents().getMembersOfGroupIDS()
        ) {
            db.collection("Users").document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        User tempUser = documentSnapshot.toObject(User.class);
                        if(tempUser != null) {
                            Marker markerUser = mMap.addMarker(new MarkerOptions().position(getUserLocation(tempUser)).title(tempUser.getUsername()));
                            UserMarker userMarker = new UserMarker(markerUser, tempUser);
                            GroupInfoWindowData groupInfoWindowData = new GroupInfoWindowData(tempUser.getUID(), getUserLocation(tempUser), tempUser.getModeOfTransport(), tempUser.getUsername(), tempUser.getUserPhotoURL(), tempUser.getUserEmailAddress());
                            markerUser.setTag(groupInfoWindowData);
                            currentGroupMarkers.get(index).appendUser(userMarker);
                            userMarkerHashMaps.put(markerUser.getId(), currentGroupMarkers.get(index).getUserIndex(userMarker));
                            loadIcon(userMarker.getUserMarker(), userMarker.getUserMarkerRepresents().getModeOfTransport());
                        }
                    }
                }
            });

        }
    }





    private boolean checkIfMarkerUserMarker(Marker marker){
        if(marker != null && userMarkerHashMaps !=null){
            Integer index = userMarkerHashMaps.get(marker.getId());
            if(index != null) {
                groupInspected = true;
                this.selectedMarker = marker;
                navigationDrawFAB.setImageResource(R.drawable.svg_back_arrow_white);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Integer index =  groupMarkersHashMaps.get(marker.getId());
        if(index != null){
            Intent intent = new Intent(this.getActivity(), GroupDetailsActivity.class);
            intent.putExtra("documentID",currentGroupMarkers.get(index).getGroupMarkerRepresents().getGroupID());
            startActivity(intent);
        }
    }

    private void locateResources(){
        floatingMenuBackground = root.findViewById(R.id.floating_action_menu_map_overview);
        actionMenuFAB1 = root.findViewById(R.id.action_menu_FAB1);
        actionMenuFAB2 = root.findViewById(R.id.action_menu_FAB2);
        searchFAB = root.findViewById(R.id.search_group_fab_map_overview);
        mapView = root.findViewById(R.id.map_view_overview);

        gpsToggleFAB = root.findViewById(R.id.location_toggle_map_overview);
        addGroupPhotoFAB =  root.findViewById(R.id.add_group_fab_map_overview);
        navigationDrawFAB =  root.findViewById(R.id.nav_draw_fab_map_overview);
        modeTransportFAB =  root.findViewById(R.id.mode_of_transport_fab_map_overview);
    }



    private void loadGpsState(){
        if(currentUser != null && currentUser.getUserLocationUpToDate() != null) {
            if (currentUser.getUserLocationUpToDate()) {
                gpsToggleFAB.setImageAlpha(255);
                gpsToggle = true;
            } else {
                gpsToggleFAB.setImageAlpha(50);
                gpsToggle = false;
            }
        }
    }

    private void loadModeOfTransportSelection(){
        if(currentUser != null && currentUser.getModeOfTransport() != null) {
           switch(currentUser.getModeOfTransport()){
               case "Car":
                   modeTransportState = 1;
                   break;
               case "Bike":
                   modeTransportState = 2;
                   break;
               default:
                   modeTransportState = 0;
                   break;
           }
           updateMenu();
        }
    }

    private void handleSearchFAB(){
        searchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchGroupsActivity.class));
            }
        });
    }

    private void handleNavDrawFAB() {
        navigationDrawFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(groupInspected){
                    navigationDrawFAB.setImageResource(R.drawable.svg_cancel_white);
                    if(selectedMarker != null) {
                        selectedMarker.hideInfoWindow();
                        selectedMarker = null;
                    }
                    groupInspected = false;
                    groupHighlighted = true;

                    //make all the group veiwable again.
                }
                else if(groupHighlighted){
                    navigationDrawFAB.setImageResource(R.drawable.svg_menu_white);
                    if(selectedMarker != null) {
                        selectedMarker.hideInfoWindow();
                        selectedMarker = null;
                    }
                    groupHighlighted = false;
                    resumeGroupOverview();
                    currentLocationMarker.setVisible(true);
                }
                else {
                    ((MainActivity) getActivity()).openDrawer();
                }
            }
        });
    }


    private void handleAddGroupFAB() {
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
    }

    private void handleGPSToggleFAB() {

        gpsToggleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the GPS to grey & stop updating their location periodically (this will require realtime user database to implement this)
                if (gpsToggle) {
                    //if true then turn it off on this interaction
                    gpsToggleFAB.setImageAlpha(50);
                    setLocationUpToDateCurrentUser(false);
                    gpsToggle = false;
                } else {
                    //else false then turn on/
                    gpsToggleFAB.setImageAlpha(255);
                    setLocationUpToDateCurrentUser(true);
                    gpsToggle = true;
                }

            }
        });

    }





    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if(currentUser != null && getUserLocation(currentUser) != null) {
            GroupInfoWindowAdapter infoWindowAdapter = new GroupInfoWindowAdapter(getContext());
            googleMap.setInfoWindowAdapter(infoWindowAdapter);
            LatLng userLocation = getUserLocation(currentUser);
            currentLocation = new MarkerOptions().position(userLocation).title("Current Location");
            currentLocationMarker = googleMap.addMarker(currentLocation);
            loadIcon(currentLocationMarker ,currentUser.getModeOfTransport());
            loadGroups(googleMap);
            googleMap.setOnInfoWindowClickListener(this);
            googleMap.setOnMarkerClickListener(this);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5));

        }
        //setting up the style of the map
        if(getActivity()!= null && getActivity().getApplicationContext() != null) {
            //don't apply the style if the app is crashing, this shouldn't happen, but applying this can cause the application not just to crash but to complete halt (app not responding).
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext(), R.raw.map_style_json);
            googleMap.setMapStyle(style);
        }
    }

    private void loadIcon(Marker marker, String modeOfTransport){
        if(marker != null) {
            switch (modeOfTransport) {
                case "Car":

                    marker.setIcon(bitmapDescriptorFromVector(MapOverviewFragment.this.getContext(),(R.drawable.svg_car_white)));
                    break;
                case "Bike":
                    marker.setIcon(bitmapDescriptorFromVector(MapOverviewFragment.this.getContext(),(R.drawable.svg_bike_white)));
                    break;
                default:
                    marker.setIcon(bitmapDescriptorFromVector(MapOverviewFragment.this.getContext(),(R.drawable.svg_person_white)));
                    break;

            }
        }
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if(vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }else{
            return null;
        }
    }


    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    private void handleModeTransportSelection() {
        modeTransportFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the GPS to grey & stop updating their location periodically (this will require realtime user database to implement this)
                if (modeTransportMenuVisibility) {
                    hideMenu();
                } else {
                    showMenu();
                }

            }
        });

    }

    private void checkStateOfTransport() {
        actionMenuFAB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (modeTransportState) {
                    case 1:
                        //car selected previously
                        modeTransportState = 0;
                        break;
                    case 2:
                        //bike selected previously
                        modeTransportState = 0;
                        break;
                    default:
                        //walking selected previously
                        modeTransportState = 2;
                        break;

                }
                updateMenu();
            }
        });
        actionMenuFAB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (modeTransportState) {
                    case 1:
                        //car selected previously
                        modeTransportState = 2;
                        break;
                    case 2:
                        //bike selected previously
                        modeTransportState = 1;
                        break;
                    default:
                        //walking selected previously
                        modeTransportState = 1;
                        break;

                }
                updateMenu();
            }
        });
    }

    private void updateMenu() {
        if (getActivity() != null) {
            switch (modeTransportState) {
                case 1:
                    //car selected
                    setModeOfTransportCurrentUser("Car");
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    break;
                case 2:
                    //bike selected
                    setModeOfTransportCurrentUser("Bike");
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    break;
                default:
                    //walking selected
                    setModeOfTransportCurrentUser("Person");
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    break;

            }
            hideMenu();
        } else {
            Snackbar.make(root, "Main activity has terminated, app will crash.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void hideMenu() {
        if(getContext() != null) {
            actionMenuFAB1.hide();
            actionMenuFAB2.hide();
            floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dsg_textview_rounded_fully_trans));
            modeTransportMenuVisibility = false;
        }
    }

    private void showMenu() {
        if(getContext() != null) {
            actionMenuFAB1.show();
            actionMenuFAB2.show();
            floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dsg_textview_rounded_trans));
            modeTransportMenuVisibility = true;
        }
    }
}