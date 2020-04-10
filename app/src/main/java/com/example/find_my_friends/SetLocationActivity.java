package com.example.find_my_friends;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.find_my_friends.ui.map_overview.MapOverviewFragment;
import com.example.find_my_friends.util.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;

public class SetLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation = new LatLng(0,0);
    private FloatingActionButton backBTN;
    private SupportMapFragment mapFragment;
    private ImageView droppedPinIMG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(PermissionUtils.checkLocationPermission(this)){
            fetchLastLocation();
        }else{
            PermissionUtils.requestLocationPermission(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        droppedPinIMG = findViewById(R.id.set_location_dropped_pin);
        droppedPinIMG.setVisibility(View.INVISIBLE);


        backBTN = (FloatingActionButton)findViewById(R.id.set_location_back_button);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5));
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this.getApplicationContext(), R.raw.map_style_json);
        mMap.setMapStyle(style);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                droppedPinIMG.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (!PermissionUtils.checkLocationPermission(this)) {
            //this permission is critical to the application, not having it will crash search functions and cause alot of issues, therefore forcing the user to accept it is the only option.
            PermissionUtils.requestLocationPermission(this);
        } else {
            fetchLastLocation();
        }
    }

    private void fetchLastLocation(){
        if(!PermissionUtils.checkLocationPermission(this)){
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    mapFragment.getMapAsync(SetLocationActivity.this);
                }
            }
        });
    }
}
