package com.example.find_my_friends;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.userUtil.User;
import com.example.find_my_friends.util.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;


import static com.example.find_my_friends.userUtil.CurrentUserUtil.notifyChangeListener;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.setLocationCurrentUser;
import static com.example.find_my_friends.util.Constants.CurrentUserLoaded;
import static com.example.find_my_friends.util.Constants.GPS_UPDATE_RATE;
import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;
import static com.example.find_my_friends.util.Constants.currentUserFirebase;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private Button logoutButton;
    private ImageView profilePhoto;
    private TextView usernameTextview;
    private TextView emailTextview;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleLoadingPopulatingNavDraw();
        handleSettingUpLocationServices();
        listenForChangesToCurrentUser();
    }


    //each time the listener is set up, it counts as a read, and updating teh document snapshot triggers another change.
    private void listenForChangesToCurrentUser(){
        currentUserDocument.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null && documentSnapshot.exists()){
                    //currentUserDocument = documentSnapshot;
                    //that one line might've been causing a cyclic dependancy.
                    currentUser = documentSnapshot.toObject(User.class);
                    if(currentUser != null) {
                        notifyChangeListener();
                        updateNavDraw();
                        CurrentUserLoaded = true;
                    }else{
                        CurrentUserLoaded = false;
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }

                    //this is a little wasteful as all upates created by the user on the interface are already modelled, however critical for functionality such as
                }else{
                    if(e != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Toast.makeText(MainActivity.this, e.toString(),Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(MainActivity.this, "User signed out",
                                Toast.LENGTH_LONG).show();
                    }
                    CurrentUserLoaded = false;
                    FirebaseAuth.getInstance().signOut();
                    finish();
                }
            }
        });
    }

    private void handleSettingUpLocationServices(){
        if(handleRequestingPermissions()){
            createLocationRequest();
            setupLocationCallback();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            setUpLocationUpdates();
        }
    }


    private Boolean handleRequestingPermissions(){
        boolean hasPermissionLocation = PermissionUtils.checkLocationPermission(this);
        //boolean hasPermissionBackground = PermissionUtils.checkLocationBackgroundPermission(this);

        if(hasPermissionLocation){
            return true;
        }else{
            PermissionUtils.requestLocationPermission(this);
            return false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        handleSettingUpLocationServices();

    }



    private void setupLocationCallback(){
         locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if(currentUser != null && currentUser.getUserLocationUpToDate()) {
                        if(location.getLatitude() != currentUser.getUserLat() || location.getLongitude() != currentUser.getUserLong()) {
                            //only set the location of the current user if the location has indeed changed otherwise this is wasteful
                            setLocationCurrentUser(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
            }
        };
    }


    private void setUpLocationUpdates(){
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(GPS_UPDATE_RATE);
        locationRequest.setFastestInterval(GPS_UPDATE_RATE/2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void handleLoadingPopulatingNavDraw(){
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        logoutButton = findViewById(R.id.logOutButton);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_current_groups, R.id.nav_map_overview, R.id.nav_my_groups,
                R.id.nav_settings, R.id.nav_group_requests)
                .setDrawerLayout(drawer)
                .build();

        handleLogoutBTN();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        profilePhoto =  navigationView.getHeaderView(0).findViewById(R.id.nav_draw_profile_photo);
        usernameTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_name);
        emailTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_email);

        if(currentUserFirebase != null) {
            String temp = currentUserFirebase.getEmail();
            emailTextview.setText(temp);
            usernameTextview.setText(currentUserFirebase.getDisplayName());
            if(currentUserFirebase.getPhotoUrl()!= null) {
                Glide.with(this).load(currentUserFirebase.getPhotoUrl()).into(profilePhoto);
            }
        }
    }


    private void updateNavDraw(){
        if(currentUserFirebase != null) {
            String temp = currentUserFirebase.getEmail();
            emailTextview.setText(temp);
            usernameTextview.setText(currentUserFirebase.getDisplayName());
            if(currentUserFirebase.getPhotoUrl()!= null) {
                Glide.with(this).load(currentUserFirebase.getPhotoUrl()).into(profilePhoto);
            }
        }
    }



    private void handleLogoutBTN(){
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }
}
