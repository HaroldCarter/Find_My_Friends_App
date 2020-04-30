package com.example.find_my_friends.ui.map_overview;
import android.content.Intent;


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
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.SearchGroupsActivity;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;
import static com.example.find_my_friends.util.Constants.currentUserFirebase;

public class MapOverviewFragment extends Fragment implements OnMapReadyCallback {

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
   //private LatLng currentLocation = new LatLng(0, 0);
   // private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton gpsToggleFAB;
    private FloatingActionButton addGroupPhotoFAB;
    private FloatingActionButton navigationDrawFAB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        mapOverviewViewModel =
                ViewModelProviders.of(this).get(MapOverviewViewModel.class);
        root = inflater.inflate(R.layout.fragment_map_overview, container, false);

        locateResources();



        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);



        //set to hide for the default and then override this later once data has been checked
        hideMenu();

        handleNavDrawFAB();
        handleGPSToggleFAB();
        handleAddGroupFAB();
        handleSearchFAB();
        handleModeTransportSelection();
        checkStateOfTransport();
        loadCurrentUser();


        return root;
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

    //each time data about the user is request/refreshed the cached variable should also be updated, should be placed into a custom util's class, and implement an oncomplete method for this.
    private void loadCurrentUser(){
        db.collection("Users").document(currentUserFirebase.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUserDocument = task.getResult();
                if(currentUserDocument != null) {
                    currentUser = currentUserDocument.toObject(User.class);
                    mapView.getMapAsync(MapOverviewFragment.this);
                    loadGpsState();
                    loadModeOfTransportSelection();
                }
            }
        });
    }

    private void loadGpsState(){
        if(currentUser != null && currentUser.getUserLocationUpToDate() != null) {
            boolean currentState = currentUser.getUserLocationUpToDate();
            if (currentState) {
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
                ((MainActivity) getActivity()).openDrawer();
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
                    gpsToggleFAB.setImageAlpha(50);
                    currentUser.setCurrentUserLocationUpToDate(false);
                    gpsToggle = false;
                } else {
                    gpsToggleFAB.setImageAlpha(255);
                    currentUser.setCurrentUserLocationUpToDate(true);
                    gpsToggle = true;
                }

            }
        });

    }





    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if(currentUser != null && currentUser.getUserLocation() != null) {
            LatLng userLocation = currentUser.getUserLocation();
            googleMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location"));
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
                    currentUser.setModeOfTransportCurrentUser("Car");
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    break;
                case 2:
                    //bike selected
                    currentUser.setModeOfTransportCurrentUser("Bike");
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    break;
                default:
                    //walking selected
                    currentUser.setModeOfTransportCurrentUser("Person");
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