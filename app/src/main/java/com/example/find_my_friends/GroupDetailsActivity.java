package com.example.find_my_friends;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private LatLng groupLatLng = new LatLng(0,0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = findViewById(R.id.toolbarGD);
        setSupportActionBar(toolbar);
        ActionBar supportBar = getSupportActionBar();

        if (supportBar != null) {
            getSupportActionBar().setTitle("");
            //default theme sets the title incorrectly
            getSupportActionBar().setIcon(R.drawable.svg_back_arrow_primary);
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.group_details_map_view);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(groupLatLng).title("marker"));

        //setting up the style of the map
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json);
        googleMap.setMapStyle(style);
    }

    @Override
    protected void onStart() {
        mapView.onStart();
        super.onStart();
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
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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