package com.example.find_my_friends.ui.map_overview;

import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;

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
    private View root;
    private MapView mapView;
    private LatLng currentLocation = new LatLng(0, 0);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapOverviewViewModel =
                ViewModelProviders.of(this).get(MapOverviewViewModel.class);
        root = inflater.inflate(R.layout.fragment_map_overview, container, false);
        floatingMenuBackground = root.findViewById(R.id.floating_action_menu_map_overview);
        actionMenuFAB1 = root.findViewById(R.id.action_menu_FAB1);
        actionMenuFAB2 = root.findViewById(R.id.action_menu_FAB2);
        mapView = root.findViewById(R.id.map_view_overview);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
        //set to hide for the default and then override this later once data has been checked
        hideMenu();

        //check the state of vis reloaded from the bundle data (so state can be restored)
        //check the state of the mode of transport so the UI can be updated to match the instanced data (REAL TIME DATABASE REQUIRED HERE)
        //each page that uses the REAL time database will required a DAO/Viewmodel, so that the activity is not clouding this document.


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

        FloatingActionButton navigationDrawFAB = (FloatingActionButton) root.findViewById(R.id.nav_draw_fab_map_overview);
        navigationDrawFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });


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

        modeTransportFAB = (FloatingActionButton) root.findViewById(R.id.mode_of_transport_fab_map_overview);
        handleModeTransportSelection();
        checkStateOfTransport();

        return root;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("marker"));
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
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
        floatingMenuBackground.setVisibility(View.INVISIBLE);
        modeTransportMenuVisibility = false;
    }

    private void showMenu() {
        actionMenuFAB1.show();
        actionMenuFAB2.show();
        floatingMenuBackground.setVisibility(View.VISIBLE);
        modeTransportMenuVisibility = true;
    }
}