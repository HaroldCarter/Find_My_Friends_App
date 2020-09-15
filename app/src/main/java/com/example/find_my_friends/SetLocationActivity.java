package com.example.find_my_friends;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;

/**
 * A class which handles the set location Activity functionality, activity opens up a map fragment and displays the user's current location from which the user can set a location for a group.
 *
 * @version v2.0
 * @author Harold Carter
 */
public class SetLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    final private String TAG = "SetLocationActivity : ";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation = new LatLng(0,0);
    private FloatingActionButton backBTN;
    private Button setLocationBTN;
    private SupportMapFragment mapFragment;
    private ImageView droppedPinIMG;
    private boolean addGroupMode = false;

    /**
     * overrides the onCreate callback, sets all the handlers for onscreen buttons, and requests the location permissions of the user.
     * @param savedInstanceState default bundle for onsaved data, nothing is saved as nothing is influx at any point during lifecycle of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handleLoadingData();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if(addGroupMode) {
            if (PermissionUtils.checkLocationPermission(this)) {
                fetchLastLocation();
            } else {
                PermissionUtils.requestLocationPermission(this);
            }
        }else{
            if(mapFragment != null) {
                mapFragment.getMapAsync(SetLocationActivity.this);
            }
        }
        droppedPinIMG = findViewById(R.id.set_location_dropped_pin);
        droppedPinIMG.setVisibility(View.INVISIBLE);
        backBTN = findViewById(R.id.set_location_back_button);
        setLocationBTN = findViewById(R.id.setLocationButton);


        handleBackBTN();
        handleSetLocationBTN();




    }


    /**
     * loads the data passed to the intent if the data exists regarding the users current location, and sets the current location internal variable equal to it. used for when resetting the location of a group, so that the dropped marker is set on the groups last location, not the users current location.
     */
    public void handleLoadingData(){
        double Lat = getIntent().getDoubleExtra("Lat", 0.0d);
        double Lng = getIntent().getDoubleExtra("Lng", 0.0d);
        String state = getIntent().getStringExtra("State");
        if (state != null && state.equals("true")){
            this.currentLocation = new LatLng(Lat,Lng);
            addGroupMode =false;
        }else{
            addGroupMode = true;
        }
    }


    /**
     * upon the user clicking the set location button the callback listener inside this function is called, this function handles getting the coordinates of the central point on the map  (dropped marker) and returning this as a bundle as the result of the activity, this also passes the address of the location if it exists as an arraylist.
     */
    private void handleSetLocationBTN(){
        setLocationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("Lat",mMap.getCameraPosition().target.latitude);
                data.putExtra("Lng",mMap.getCameraPosition().target.longitude);

                Geocoder geocoder;
                List<Address> addresses ;
                geocoder = new Geocoder(SetLocationActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude, 1);
                }catch(IOException e){
                    Log.e(TAG, "onClick: Error when trying to get the address, no address provided");
                    addresses = null;
                }


                Bundle args = new Bundle();
                args.putSerializable("arrayList",(Serializable)addresses);
                data.putExtra("BUNDLE", args);

                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

    /**
     * handles user interaction with the back button by setting the result to cancelled and the data to null, then finishes this activity.
     */
    private void handleBackBTN(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED,null);
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
        if(addGroupMode) {
            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        }else{
            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Group Location"));
        }
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this.getApplicationContext(), R.raw.map_style_json);
        mMap.setMapStyle(style);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                droppedPinIMG.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * onRequestPermissionsResult override callback for when the os has queried the user for permission to access certain resources (location in the case of this activity) this function should never be called because the main activity should've already requested location permission however if the user denies all requests for location and approves it within this activity then this will handle this.
     * once permission is granted fetchlastknownlocation is called.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * requests the users current or last known location (based if the user has the gps currently enabled), then returns this to the functions internal callback, upon this call back it prepares the map and sets the current location to that provided to the callback.
     */
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
