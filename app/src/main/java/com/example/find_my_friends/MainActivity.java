package com.example.find_my_friends;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
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

/**
 * The main activity of the application, which contains the functionality for the navigation draw and responsible listening for changes to the current users' account.
 *
 * @author Harold Carter
 * @version 5.0
 */
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

    /**
     * an override for the default oncreate callback function, this handles setting the content of the of the view/layer and populating the nav draw and getting current location
     *
     * @param savedInstanceState bundle (not used) as no onscreen data is influx within this activity (its fixed state)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleLoadingPopulatingNavDraw();
        handleSettingUpLocationServices();
        listenForChangesToCurrentUser();
    }

    /**
     * this functions adds a snapshot listener to the current users document, and when any change is detected the current user mode locally stored is updated, this also notifies any attached change listener, while this is not properly implemented in this version of the application it will be in future versions as this is the least wasteful way of keeping user information consistent with that found on the server
     */
    private void listenForChangesToCurrentUser() {
        currentUserDocument.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        notifyChangeListener();
                        updateNavDraw();
                        CurrentUserLoaded = true;
                    } else {
                        CurrentUserLoaded = false;
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }
                } else {
                    if (e != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    } else {
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

    /**
     * as the name indicates this function requests location services and if the resources is already permitted then it will setup a location callback at a variable interval, this achieved by using a fuselocationclient
     */
    private void handleSettingUpLocationServices() {
        if (handleRequestingPermissions()) {
            createLocationRequest();
            setupLocationCallback();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            setUpLocationUpdates();
        }
    }

    /**
     * checks if the location permission are permitted for this activity/app, is so returns true else it will request the permissions and return false
     *
     * @return Boolean based off the current status of the location permissions.
     */
    private Boolean handleRequestingPermissions() {
        boolean hasPermissionLocation = PermissionUtils.checkLocationPermission(this);
        //boolean hasPermissionBackground = PermissionUtils.checkLocationBackgroundPermission(this);

        if (hasPermissionLocation) {
            return true;
        } else {
            PermissionUtils.requestLocationPermission(this);
            return false;
        }
    }

    /**
     * on the result of the location permission request this function is called, this function checks if the permission request was authored from this activity, if not the result is ignored; if it is then handlesettinguplocation is called in a cyclic loop because this permission is required for the functionality of this app
     *
     * @param requestCode  (int) representing the author's tag
     * @param permissions  (String array) contain the array of permissions requested
     * @param grantResults (int array) containing a parallel number of members containing the status of the request (confirmed or denied)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        handleSettingUpLocationServices();

    }


    /**
     * as the name suggests this function provides the callback method triggered by the fuselocation;
     */
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (currentUser != null && currentUser.getUserLocationUpToDate()) {
                        if (location.getLatitude() != currentUser.getUserLat() || location.getLongitude() != currentUser.getUserLong()) {
                            //only set the location of the current user if the location has indeed changed otherwise this is wasteful
                            setLocationCurrentUser(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
            }
        };
    }

    /**
     * this function configures the fusedlocationclient's settings and provides the main looper, this is realistically a handler which calls a callback, hence why it needs a callback and reference to the looper for the ui thread
     */
    private void setUpLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    /**
     * this function initializes the location request settings, by altering the internal variable of the class, it sets the update interval and the priority of the location request
     */
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(GPS_UPDATE_RATE);
        locationRequest.setFastestInterval(GPS_UPDATE_RATE / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * called in the oncreate method, this function sets the internal variables of the mainclass for the nav draw, this function is also responsible for loading the users profile photo into the navdraw)
     */
    private void handleLoadingPopulatingNavDraw() {
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

        profilePhoto = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_profile_photo);
        usernameTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_name);
        emailTextview = navigationView.getHeaderView(0).findViewById(R.id.nav_draw_user_email);

        if (currentUserFirebase != null) {
            String temp = currentUserFirebase.getEmail();
            emailTextview.setText(temp);
            usernameTextview.setText(currentUserFirebase.getDisplayName());
            if (currentUserFirebase.getPhotoUrl() != null) {
                Glide.with(this).load(currentUserFirebase.getPhotoUrl()).into(profilePhoto);
            }
        }
    }

    /**
     * once the current users data is changed the most likely change is the information displayed in the nav draw (as this displays almost all of it), therefore this function updates the onscreen variables to match the variables of the current user from the database.
     */
    private void updateNavDraw() {
        if (currentUserFirebase != null) {
            String temp = currentUserFirebase.getEmail();
            emailTextview.setText(temp);
            usernameTextview.setText(currentUserFirebase.getDisplayName());
            if (currentUserFirebase.getPhotoUrl() != null && getApplicationContext() != null) {
                Glide.with(getApplicationContext()).load(currentUserFirebase.getPhotoUrl()).into(profilePhoto);
            }
        }
    }


    /**
     * upon the user clicking the logout button the onclick callback listener contained in this function is triggered, within which the authentication of the is removed (firebase authentication is revoked) and the main activity is closed.
     */
    private void handleLogoutBTN() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }


    /**
     * boiler plate generated by the default nav draw preset in android studio, handle the upwards navigation through the UI (upon pressing back on the taskbar)
     *
     * @return boolean true if Up navigation completed successfully and this Activity was finished, false otherwise
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * a function used for opening the nav draw
     */
    public void openDrawer() {
        drawer.openDrawer(GravityCompat.START);
    }
}
