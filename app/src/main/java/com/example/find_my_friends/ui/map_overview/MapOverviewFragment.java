package com.example.find_my_friends.ui.map_overview;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
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
import com.example.find_my_friends.util.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;

import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;

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
    private LatLng currentLocation = new LatLng(0, 0);
    private Location mCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapOverviewViewModel =
                ViewModelProviders.of(this).get(MapOverviewViewModel.class);
        root = inflater.inflate(R.layout.fragment_map_overview, container, false);
        floatingMenuBackground = root.findViewById(R.id.floating_action_menu_map_overview);
        actionMenuFAB1 = root.findViewById(R.id.action_menu_FAB1);
        actionMenuFAB2 = root.findViewById(R.id.action_menu_FAB2);
        searchFAB = root.findViewById(R.id.search_group_fab_map_overview);
        mapView = root.findViewById(R.id.map_view_overview);



        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);



        //set to hide for the default and then override this later once data has been checked
        hideMenu();
        //handle checking if we have approved location services.
        if(PermissionUtils.checkLocationPermission(getActivity())){
            fetchLastLocation();
        }else{
            PermissionUtils.requestLocationPermission(getActivity());
        }


        //createLocationRequest();
        handleNavDrawFAB();
        handleGPSToggleFAB();
        handleAddGroupFAB();
        handleSearchFAB();

        modeTransportFAB = (FloatingActionButton) root.findViewById(R.id.mode_of_transport_fab_map_overview);
        handleModeTransportSelection();
        checkStateOfTransport();

        return root;
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
        FloatingActionButton navigationDrawFAB = (FloatingActionButton) root.findViewById(R.id.nav_draw_fab_map_overview);
        navigationDrawFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
    }


    private void handleAddGroupFAB() {
        FloatingActionButton addGroupPhotoFAB = (FloatingActionButton) root.findViewById(R.id.add_group_fab_map_overview);
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
        final FloatingActionButton gpsToggleFAB = (FloatingActionButton) root.findViewById(R.id.location_toggle_map_overview);
        gpsToggleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the GPS to grey & stop updating their location periodically (this will require realtime user database to implement this)
                if (gpsToggle) {
                    gpsToggleFAB.setImageAlpha(50);
                    gpsToggle = false;
                } else {
                    gpsToggleFAB.setImageAlpha(255);
                    gpsToggle = true;
                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (!PermissionUtils.checkLocationPermission(getActivity())) {
            //this permission is critical to the application, not having it will crash search functions and cause alot of issues, therefore forcing the user to accept it is the only option.
            PermissionUtils.requestLocationPermission(getActivity());
        } else {
            fetchLastLocation();
        }
    }


    private void fetchLastLocation(){
        if(!PermissionUtils.checkLocationPermission(getActivity())){
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    mapView.getMapAsync(MapOverviewFragment.this);
                }
            }
        });
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
     */


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5));
        //setting up the style of the map
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext(), R.raw.map_style_json);
        googleMap.setMapStyle(style);






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
    public void onSaveInstanceState(Bundle outState) {
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
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    break;
                case 2:
                    //bike selected
                    modeTransportFAB.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_bike_white));
                    actionMenuFAB1.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_person_white));
                    actionMenuFAB2.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.svg_car_white));
                    break;
                default:
                    //walking selected
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
        actionMenuFAB1.hide();
        actionMenuFAB2.hide();
        floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dsg_textview_rounded_fully_trans));

        modeTransportMenuVisibility = false;
    }

    private void showMenu() {
        actionMenuFAB1.show();
        actionMenuFAB2.show();
        floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.dsg_textview_rounded_trans));
        modeTransportMenuVisibility = true;
    }
}