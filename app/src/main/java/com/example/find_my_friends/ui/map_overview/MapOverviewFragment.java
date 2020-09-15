package com.example.find_my_friends.ui.map_overview;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.GroupDetailsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.SearchGroupsActivity;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.groupUtil.InfoWindowAdapter;
import com.example.find_my_friends.groupUtil.InfoWindowData;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import static com.example.find_my_friends.userUtil.UserUtil.composeEmail;
import static com.example.find_my_friends.userUtil.UserUtil.getUserLocation;
import static com.example.find_my_friends.util.Constants.CurrentUserLoaded;
import static com.example.find_my_friends.util.Constants.MAPVIEW_BUNDLE_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.LocationUtils.distanceBetweenTwoPointMiles;

/**
 * This class handles the functionally for the map overview fragment, handling the drawing of on map displays, marker interaction and drawing the shortest path to a group all occur within this class.
 *
 * @author Harold Carter
 * @version 7.0
 */
public class MapOverviewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, RoutingListener {
    private final int SECOND_IN_MILLI = 1000; /* milliseconds */
    private boolean gpsToggle = true;
    private boolean modeTransportMenuVisibility = false;
    private int modeTransportState = 0; //0 walking, 1 driving, 2 biking.
    private ConstraintLayout floatingMenuBackground;
    private FloatingActionButton modeTransportFAB;
    private FloatingActionButton actionMenuFAB1;
    private FloatingActionButton actionMenuFAB2;
    private FloatingActionButton searchFAB;
    private ProgressBar loadingBar;
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
    private GroupMarker currentGroupHighlighted = null;
    private UserMarker currentUserHighlighted = null;

    private Handler mHandler = new Handler();
    private boolean FragmentFocused = true;
    private ArrayList<Polyline> routePolyLines = new ArrayList<>();

    /**
     * a runnable for updating the current group markers on a map
     * info - while creating a handler and running a task on the ui thread is typically bad practice because it can lead to leaks regarding activity lifecycle event, the action of purging all messages(not in process, and checking if references in the runnable are dead before recalling a runnable from within)
     * should mean that the leak is minimized, and the handler won't survive past the life cycle of the activity/fragment activity.
     */
    private Runnable updateGroupMarkers = new Runnable() {
        @Override
        public void run() {
            if (mMap != null && FragmentFocused) {
                if ((!groupInspected && !groupHighlighted)) {
                    loadingBar.setVisibility(View.VISIBLE);
                    Toast toast = Toast.makeText(getContext(), "Map Updated", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
                    toast.show();
                    deleteAllMarkers();
                    loadGroups(mMap);
                    loadingBar.setVisibility(View.INVISIBLE);
                    if (currentUser != null && currentUser.getUserUpdateRate() != null) {
                        mHandler.postDelayed(this, (currentUser.getUserUpdateRate() * SECOND_IN_MILLI));
                    } else {
                        mHandler.postDelayed(this, (15 * SECOND_IN_MILLI));
                    }
                } else {
                    mHandler.postDelayed(this, (15 * SECOND_IN_MILLI));
                }
            }
        }
    };


    /**
     * Called to have the fragment instantiate its user interface view. initializes all the internal variables which represent the onscreen variables within the view and handles their functionality
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return the View for the fragment's UI, or null.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map_overview, container, false);
        locateResources();
        loadingBar.setVisibility(View.VISIBLE);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        hideMenu();
        handleNavDrawFAB();
        handleGPSToggleFAB();
        handleAddGroupFAB();
        handleSearchFAB();
        handleModeTransportSelection();
        checkStateOfTransport();
        currentUserListener();
        if (CurrentUserLoaded) {
            loadGpsState();
            loadModeOfTransportSelection();
            mapView.getMapAsync(MapOverviewFragment.this);
        }
        mHandler.postDelayed(updateGroupMarkers, 0);
        return root;
    }

    /**
     * sets the listener for the current user, so upon change a snapshot listener within the main activity will notify this listener and therefore can update the map accordingly
     */
    private void currentUserListener() {
        setCurrentUserListener(new CurrentUserUtil.ChangeListener() {
            @Override
            public void onChange() {
                loadGpsState();
                loadModeOfTransportSelection();
                updateCurrentLocation();
                loadIcon(currentLocationMarker, currentUser.getModeOfTransport(), currentUser.getUserColor());
            }
        });
    }

    /**
     * updates the position for the current location's marker on the map, if the marker exists. if the marker doesn't exist this function does nothing.
     */
    private void updateCurrentLocation() {
        if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(new LatLng(currentUser.getUserLat(), currentUser.getUserLong()));
        }
    }

    /**
     * indexes through the current user's collection of memberships to get each groups UID from the arraylist of usersMemberships and then index the corresponding documents on the FireStore database through means of asynchronous callback; upon success a maker for the group is created and an instance of the group saved through calling createGroupMaker
     * if the document fails to be fetched from the server (group is deleted) then no action is taken as this group should not be displayed or attempted to be displayed.
     * @param googleMap googleMap object, the map the markers will be appended to.
     */
    private void loadGroups(GoogleMap googleMap) {
        if (currentUser.getUsersMemberships() != null) {
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
                                if ((tempGroup.getCompletedMemberIDS() == null && tempGroup.getMembersOfGroupIDS() != null)) {
                                    createGroupMarker(tempGroup, googleMap);
                                } else if (tempGroup.getCompletedMemberIDS().size() != tempGroup.getMembersOfGroupIDS().size()) {
                                    createGroupMarker(tempGroup, googleMap);
                                }
                            }

                        }
                    }
                });


            }
        }
    }

    /**
     * CreateGroupMarker generates a marker for the group passed as a parameter and sets the infoWindowAdapters date to be that of the most uptodate from the server, re-requesting the creator's documents through means of an asynchronous callback; upon success the document is converted to an object and the photourl
     * is passed to the infoWindowData object so that the user's most upto date photo and settings are displayed rather than that of the time of creation for the group, as creator details are not maintained within the group document.
     *
     * @param groupToCreatorMarkerFor Group that the marker is representing on the map
     * @param googleMap GoogleMap Object, the map to append the marker too
     *
     */
    private void createGroupMarker(Group groupToCreatorMarkerFor, GoogleMap googleMap) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(groupToCreatorMarkerFor.getGroupLatitude(), groupToCreatorMarkerFor.getGroupLongitude())).title(groupToCreatorMarkerFor.getGroupTitle()));
        marker.setVisible(false);
        db.collection("Users").document(groupToCreatorMarkerFor.getGroupCreatorUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User temp = documentSnapshot.toObject(User.class);
                if (temp != null && getContext() != null) {
                    marker.setVisible(true);
                    InfoWindowData infoWindowData = new InfoWindowData(null, null, groupToCreatorMarkerFor.getGroupTitle(), groupToCreatorMarkerFor.getGroupPhotoURI(), temp.getUsername());
                    marker.setIcon(vectorResourceToBitMapDescriptorConverter(MapOverviewFragment.this.getContext(), (R.drawable.svg_location_white), groupToCreatorMarkerFor.getGroupColor()));
                    marker.setTag(infoWindowData);
                    GroupMarker groupMarker = new GroupMarker(marker, groupToCreatorMarkerFor);
                    currentGroupMarkers.add(groupMarker);
                    groupMarkersHashMaps.put(marker.getId(), currentGroupMarkers.indexOf(groupMarker));
                }
            }
        });


    }

    /**
     * this function is used when the user navigates away from inspecting the members of a group, therefore this function hides all users and displays all groups markers from the marker Arraylists for the respectful types of markers
     *
     */
    public void resumeGroupOverview() {
        for (GroupMarker groupMarker : currentGroupMarkers
        ) {
            groupMarker.getGroupMarker().setVisible(true);
            for (UserMarker user : groupMarker.getUsers()
            ) {
                user.getUserMarker().setVisible(false);
            }
        }
    }


    /**
     * overrides the defaul onclickmarker callback, this fuction is called whenever a marker is clicked; within this function it checks if the marker is that of a group, and if so handles it accordingly(within checkIfMarkerGroupMarker) and then goes on to check if the marker is a user marker in the respective method checkIfMarkerUserMarker.
     *
     *
     * @param marker Marker to check and act upon the click
     * @return true if the listener has consumed the event (i.e., the default behavior should not occur); false otherwise (i.e., the default behavior should occur). (we do not want to consume the action but expand upon it therefore false is always returned)
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!checkIfMakerGroupMarker(marker)) {
            checkIfMarkerUserMarker(marker);
        }
        return false;
    }

    /**
     * checks if the marker is contained in the hashmap of group markers, if so then the group is inspected users for the group are displayed as markers on the map.
     *
     * @param marker Marker to check if the marker is contained in the hashmap of group markers
     * @return status of the marker being a group marker as a boolean, true- if the marker passed as a parameter was a group or false if not.
     */
    private boolean checkIfMakerGroupMarker(Marker marker) {
        if (marker != null && groupMarkersHashMaps != null && currentGroupMarkers != null) {
            Integer index = groupMarkersHashMaps.get(marker.getId());
            if (index != null) {
                currentLocationMarker.setVisible(false);
                hideAllGroupMarkersButCurrent(index);
                groupInspected = true;
                currentGroupHighlighted = this.currentGroupMarkers.get(index);
                this.selectedMarker = marker;
                navigationDrawFAB.setImageResource(R.drawable.svg_back_arrow_white);
                generateUserMarkers(index);
                return true;
            }
        }
        return false;
    }

    /**
     * hides all the markers for the other groups other than the currently selected marker
     *
     * @param index Int, for the index in the arraylist of groups for the selected group that is not hidden.
     */
    private void hideAllGroupMarkersButCurrent(Integer index) {
        GroupMarker selectedMarker = currentGroupMarkers.get(index);
        for (GroupMarker cGM : currentGroupMarkers
        ) {
            if (!cGM.getGroupMarker().getId().equals(selectedMarker.getGroupMarker().getId())) {
                cGM.getGroupMarker().setVisible(false);
            }
        }
    }

    /**
     * by indexing through the indexed groups members (indexed in the group arraylist using the parameter index) and then drawing their icons to the map; this is achieved by getting each document of each member of the group and then getting their last known location and preferred mode of transport
     *
     * @param index Int, index of the group selected in the ArrayList of groups.
     */
    private void generateUserMarkers(Integer index) {
        userMarkerHashMaps.clear();
        for (String s : currentGroupMarkers.get(index).getGroupMarkerRepresents().getMembersOfGroupIDS()
        ) {
            db.collection("Users").document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        User tempUser = documentSnapshot.toObject(User.class);
                        if (tempUser != null && currentGroupHighlighted != null && distanceBetweenTwoPointMiles(currentGroupHighlighted.getGroupMarkerRepresents().getGroupLatitude(), currentGroupHighlighted.getGroupMarkerRepresents().getGroupLongitude(), tempUser.getUserLat(), tempUser.getUserLong()) >= 0.5) {
                            Marker markerUser = mMap.addMarker(new MarkerOptions().position(getUserLocation(tempUser)).title(tempUser.getUsername()));
                            markerUser.setVisible(false);
                            UserMarker userMarker = new UserMarker(markerUser, tempUser);
                            InfoWindowData infoWindowData = new InfoWindowData(getUserLocation(tempUser), tempUser.getModeOfTransport(), tempUser.getUsername(), tempUser.getUserPhotoURL(), tempUser.getUserEmailAddress());
                            markerUser.setTag(infoWindowData);
                            currentGroupMarkers.get(index).appendUser(userMarker);
                            userMarkerHashMaps.put(markerUser.getId(), currentGroupMarkers.get(index).getUserIndex(userMarker));
                            loadIcon(userMarker.getUserMarker(), userMarker.getUserMarkerRepresents().getModeOfTransport(), tempUser.getUserColor());
                            markerUser.setVisible(true);
                        }
                    }
                }
            });

        }
    }

    /**
     * checks if the provided marker is contained within the userhashmap of markers, and if so this function will call other functions to draw a route through and display the users info window.
     *
     * @param marker marker to check if it was a user
     * @return boolean for the status of if the marker was a user's marker or not, if it was then true; else false
     */
    private boolean checkIfMarkerUserMarker(Marker marker) {
        if (marker != null && userMarkerHashMaps != null) {
            Integer index = userMarkerHashMaps.get(marker.getId());
            if (index != null) {
                currentUserHighlighted = currentGroupHighlighted.getUser(index);
                loadingBar.setVisibility(View.VISIBLE);
                getPathToGroup(getUserLocation(currentUserHighlighted.getUserMarkerRepresents()));
                groupInspected = true;
                this.selectedMarker = marker;
                navigationDrawFAB.setImageResource(R.drawable.svg_back_arrow_white);
                return true;
            }
        }
        return false;
    }

    /**
     * default override callback for when a markers displayed information window is clicked, within this function the index of the marker is created from the hashmap, and assuming this index exists the value wil be checked, if null the index in the groups hashmap didn't exist and therefore must be a user marker
     * the index for the userhashmap is then checked.
     *
     * if the index is for a groups marker then the group details activity is launched passing the documentID as an extra inside the intent bundle
     * else if the index is for a user marker then an implicit intent for creating an email is sent, using the userUtil's function compose email
     *
     * @param marker Marker that had the info-window clicked.
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Integer index = groupMarkersHashMaps.get(marker.getId());
        if (index != null) {
            Intent intent = new Intent(this.getActivity(), GroupDetailsActivity.class);
            intent.putExtra("documentID", currentGroupMarkers.get(index).getGroupMarkerRepresents().getGroupID());
            startActivity(intent);
        }
        if (currentGroupHighlighted != null && currentUserHighlighted != null) {
            index = userMarkerHashMaps.get(marker.getId());
            if (index != null && getContext() != null) {
                composeEmail(getContext(), new String[]{currentUserHighlighted.getUserMarkerRepresents().getUserEmailAddress()}, "New Message From " + (currentUser.getUsername()));
            }
        }
    }

    /**
     * used in the oncreate to link the internal variables of the class to that of the onscreen variables in the view.
     */
    private void locateResources() {
        floatingMenuBackground = root.findViewById(R.id.floating_action_menu_map_overview);
        actionMenuFAB1 = root.findViewById(R.id.action_menu_FAB1);
        actionMenuFAB2 = root.findViewById(R.id.action_menu_FAB2);
        searchFAB = root.findViewById(R.id.search_group_fab_map_overview);
        mapView = root.findViewById(R.id.map_view_overview);
        loadingBar = root.findViewById(R.id.map_overview_loading_bar);

        gpsToggleFAB = root.findViewById(R.id.location_toggle_map_overview);
        addGroupPhotoFAB = root.findViewById(R.id.add_group_fab_map_overview);
        navigationDrawFAB = root.findViewById(R.id.nav_draw_fab_map_overview);
        modeTransportFAB = root.findViewById(R.id.mode_of_transport_fab_map_overview);
    }

    /**
     * loads from the current user if they have the gps tracking capability toggled on or off and shades the corresponding FAB accordingly
     * low alpha - not toggled
     * high alpha - toggled
     */
    private void loadGpsState() {
        if (currentUser != null && currentUser.getUserLocationUpToDate() != null) {
            if (currentUser.getUserLocationUpToDate()) {
                gpsToggleFAB.setImageAlpha(255);
                gpsToggle = true;
            } else {
                gpsToggleFAB.setImageAlpha(50);
                gpsToggle = false;
            }
        }
    }

    /**
     *loads from the current user the mode of transport they have selected, the default is "Person" however this could be "Bike" or "Car" this is then loaded into the corresponding FAB menu through subfunctions
     */
    private void loadModeOfTransportSelection() {
        if (currentUser != null && currentUser.getModeOfTransport() != null) {
            switch (currentUser.getModeOfTransport()) {
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

    /**
     * deletes all current markers from the map and clears the hashmaps, used in updating of the map.
     */
    private void deleteAllMarkers() {
        if(currentGroupMarkers != null) {
            for (GroupMarker g : currentGroupMarkers
            ) {
                g.getGroupMarker().remove();
                groupMarkersHashMaps.remove(g.getGroupMarker().getId());
                if(g.getUsers() != null) {
                    for (UserMarker u : g.getUsers()) {
                        userMarkerHashMaps.remove(u.getUserMarker().getId());
                        u.getUserMarker().remove();
                    }
                }
                g.getUsers().clear();
            }
            currentGroupMarkers.clear();
        }
    }

    /**
     * handles the onclick functionality for the search FAB, when click this button starts an intent for the SearchActivity
     */
    private void handleSearchFAB() {
        searchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchGroupsActivity.class));
            }
        });
    }

    /**
     * handles the functionaltiy for the nav draw FAB which due to the recycled nature of the FAB can have multiple states based of the state of the activity.
     *
     * if the user is inspecting a group then this FAB acts as a back button, and will remove them to a group overview, this is known as group highlighted state
     * if the user has a group highlighted (viewing the group and its subsequent members) then this FAB will act as a back button and remove the user from highlighted mode, restoring the map overview mode (default)
     * once in default this button will act a nav draw button (its original intended purpose) and make a request to the main activity to expand the navigation draw.
     *
     * the reason this FAB is recycled like so and not multiple FAB with each dedicated purpose is simple, adding more FAB obscures the information in the map overview making it difficult to navigate and not within keeping with the original design
     */
    private void handleNavDrawFAB() {
        navigationDrawFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (groupInspected) {
                    navigationDrawFAB.setImageResource(R.drawable.svg_cancel_white);
                    if (selectedMarker != null) {
                        selectedMarker.hideInfoWindow();
                        selectedMarker = null;
                    }
                    groupInspected = false;
                    groupHighlighted = true;
                    deletePolyLines();
                } else if (groupHighlighted) {
                    navigationDrawFAB.setImageResource(R.drawable.svg_menu_white);
                    if (selectedMarker != null) {
                        selectedMarker.hideInfoWindow();
                        selectedMarker = null;
                    }
                    groupHighlighted = false;
                    currentGroupHighlighted = null;
                    resumeGroupOverview();
                    currentLocationMarker.setVisible(true);
                } else {
                    if (getActivity() != null) {
                        ((MainActivity) getActivity()).openDrawer();
                    }
                }
            }
        });
    }

    /**
     * when the add group button is click the set onclick listener found within this function is trigger, and with this an intent for the AddGroupActivity is made, therefore giving the app the ability to add a group.
     */
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

    /**
     *
     */
    private void handleGPSToggleFAB() {
        gpsToggleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpsToggle) {
                    gpsToggleFAB.setImageAlpha(50);
                    setLocationUpToDateCurrentUser(false);
                    gpsToggle = false;
                } else {
                    gpsToggleFAB.setImageAlpha(255);
                    setLocationUpToDateCurrentUser(true);
                    gpsToggle = true;
                }

            }
        });

    }

    /**
     * overrides the Callback interface for when the map is ready to be used, and sets the style of the map to the custom json file for the map design.
     * this function also calls to load all of the groups markers onto the map now that the map is ready to have markers appended.
     * all ui settings are also disabled in this method.
     * @param googleMap GoogleMap, the instance of the map that called this callback and therefore ready to be serviced.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        if (currentUser != null) {
            InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(getContext());
            googleMap.setInfoWindowAdapter(infoWindowAdapter);
            LatLng userLocation = getUserLocation(currentUser);
            currentLocation = new MarkerOptions().position(userLocation).title("Current Location");
            currentLocationMarker = googleMap.addMarker(currentLocation);
            loadIcon(currentLocationMarker, currentUser.getModeOfTransport(), currentUser.getUserColor());
            loadGroups(googleMap);
            googleMap.setOnInfoWindowClickListener(this);
            googleMap.setOnMarkerClickListener(this);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5));

        }
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext(), R.raw.map_style_json);
            googleMap.setMapStyle(style);
            loadingBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * loads the correct icon resources for the mode of transport for a given usermarker
     *
     * @param marker the Usermarker that is having the icon loaded for
     * @param modeOfTransport the mode of transport the user that marker represents
     * @param color the color the user has selected their marker to be represented as a string
     */
    private void loadIcon(Marker marker, String modeOfTransport, String color) {
        if (marker != null) {
            switch (modeOfTransport) {
                case "Car":

                    marker.setIcon(vectorResourceToBitMapDescriptorConverter(MapOverviewFragment.this.getContext(), (R.drawable.svg_car_white), color));
                    break;
                case "Bike":
                    marker.setIcon(vectorResourceToBitMapDescriptorConverter(MapOverviewFragment.this.getContext(), (R.drawable.svg_bike_white), color));
                    break;
                default:
                    marker.setIcon(vectorResourceToBitMapDescriptorConverter(MapOverviewFragment.this.getContext(), (R.drawable.svg_person_white), color));
                    break;

            }
        }
    }


    /**
     * loads and converts from a SVG resource file to a BitmapDescriptor required for loading the icon of a  marker, color is achieved through casting the drawable into an imageview and then applying the color filter to the imageview before extracting the drawable
     * this is done through this means as it is actually the most efficient means of applying a color filter to a bitmap.
     *
     * @param context context of the application performing the conversion
     * @param VectorResource the int value for the resource id
     * @param shadingColor String for the color from usercolors enum for what color the user's marker should be shaded
     * @return BitmapDescriptor the required type of object for setting the icon of a marker, containing the appropriately shaded vector resource with its intrinsic bounds.
     */
    private BitmapDescriptor vectorResourceToBitMapDescriptorConverter(Context context, int VectorResource, String shadingColor) {
        Drawable vectorResourceDrawable = ContextCompat.getDrawable(context, VectorResource);
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(vectorResourceDrawable);
        imageView.setColorFilter(Color.parseColor(shadingColor));
        vectorResourceDrawable = imageView.getDrawable();
        if (vectorResourceDrawable != null) {
            vectorResourceDrawable.setBounds(0, 0, vectorResourceDrawable.getIntrinsicWidth(), vectorResourceDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorResourceDrawable.getIntrinsicWidth(), vectorResourceDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorResourceDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } else {
            return null;
        }
    }

    /**
     * Overrides the onstart function to restart the group update handler and set the mapview's life cycle accordingly to prevent memoryleaks
     */
    @Override
    public void onStart() {
        FragmentFocused = true;
        if (currentUser != null && currentUser.getUserUpdateRate() != null) {
            mHandler.postDelayed(updateGroupMarkers, (currentUser.getUserUpdateRate() * SECOND_IN_MILLI));
        } else {
            mHandler.postDelayed(updateGroupMarkers, (15 * SECOND_IN_MILLI));
        }
        mapView.onStart();
        super.onStart();
    }

    /**
     * Overrides the onResume function to restart the group update handler and set the mapview's life cycle accordingly to prevent memoryleaks
     */
    @Override
    public void onResume() {
        FragmentFocused = true;
        if (currentUser != null && currentUser.getUserUpdateRate() != null) {
            mHandler.postDelayed(updateGroupMarkers, (currentUser.getUserUpdateRate() * SECOND_IN_MILLI));
        } else {
            mHandler.postDelayed(updateGroupMarkers, (15 * SECOND_IN_MILLI));
        }
        mapView.onResume();
        super.onResume();
    }

    /**
     *  Overrides the onPause function to stop the group update handler and set the mapview's life cycle accordingly to prevent memoryleaks
     */
    @Override
    public void onPause() {
        mHandler.removeCallbacks(updateGroupMarkers);
        FragmentFocused = false;
        mapView.onPause();
        super.onPause();
    }

    /**
     *  Overrides the onStop function to stop the group update handler and set the mapview's life cycle accordingly to prevent memoryleaks
     */
    @Override
    public void onStop() {
        mHandler.removeCallbacks(updateGroupMarkers);
        FragmentFocused = false;
        mapView.onStop();
        super.onStop();
    }

    /**
     *  Overrides the onDestroy function to stop the group update handler and set the mapview's life cycle accordingly to prevent memoryleaks
     */
    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(updateGroupMarkers);
        FragmentFocused = false;
        mapView.onDestroy();
        super.onDestroy();
    }

    /**
     * Overrides the onsave instance state to save the instance of the map, so that upon device rotation or destruction of the fragment the settings for the map are maintained
     * @param outState Bundle containg the mapviews onsaved state.
     */
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

    /**
     * Overrides the onLowMemory function to set the mapview's life cycle accordingly to reduce memory usage.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     *handles the modeoftransportFAB, this is used to reveal the full menu or hide the menu based on if the menu is already shown or not.
     */
    private void handleModeTransportSelection() {
        modeTransportFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modeTransportMenuVisibility) {
                    hideMenu();
                } else {
                    showMenu();
                }

            }
        });

    }

    /**
     * once the action menu is revealed the onclick listeners can have their reponses triggered, and depending on the state of the previously selected mode of transport predicts the FABs Functionality, as the two remaining options on the action menu change whenever the user selects an option.
     *
     */
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

    /**
     * this function switches on the selected mode of transport and updates the FAB for the mode of transports icon and the icons in the FAB menu according to the state selected (only three possible combinations).
     */
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

    /**
     * hides the onscreen Floating Action menu for selecting a new mode of transport
     */
    private void hideMenu() {
        if (getContext() != null) {
            actionMenuFAB1.hide();
            actionMenuFAB2.hide();
            floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dsg_textview_rounded_fully_trans));
            modeTransportMenuVisibility = false;
        }
    }

    /**
     * displays the onscreen Floating Action menu for selecting a new mode of transport
     */
    private void showMenu() {
        if (getContext() != null) {
            actionMenuFAB1.show();
            actionMenuFAB2.show();
            floatingMenuBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dsg_textview_rounded_trans));
            modeTransportMenuVisibility = true;
        }
    }

    /**
     * using the jd-alexander Library for route planning which simple handles requesting the route to the directions api server (no point re-inventing the wheel) and returning a list of poly lines; this function gets the route between the users location (the user the marker represents) and the location of the group currently being inpsected.
     * this route is then built on an asynchronous thread and callbacks are given to respond to the progress of the task.
     *
     * @param userLocation LatLng for the users location (the user the marker that was clicked represents)
     */
    void getPathToGroup(LatLng userLocation) {
        if (currentGroupHighlighted != null) {
            Routing.Builder routing = new Routing.Builder()
                    .key(getResources().getString(R.string.google_api_key))
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(userLocation, new LatLng(currentGroupHighlighted.getGroupMarkerRepresents().getGroupLatitude(), currentGroupHighlighted.getGroupMarkerRepresents().getGroupLongitude()));
            switch (currentUserHighlighted.getUserMarkerRepresents().getModeOfTransport()) {
                case "Car":
                    routing.travelMode(AbstractRouting.TravelMode.DRIVING);
                    break;
                case "Bike":
                    routing.travelMode(AbstractRouting.TravelMode.BIKING);
                    break;
                default:
                    routing.travelMode(AbstractRouting.TravelMode.WALKING);

            }
            routing.build().execute();
        }
    }

    /**
     * deletes all the polylines of a route, done on the UI thread as each polyline needs to be removed from the map, this causes a small amount of slowdown however this action cannot be done of a separate thread without use of a weak reference; and even then slow down to the user is comparable as each action
     * for p.remove interacts with the googlemaps thread thus slowing the maps thread down ultimately leading to the same experience in slow down.
     */
    private void deletePolyLines() {
        for (Polyline p : routePolyLines
        ) {
            p.remove();
        }
        routePolyLines.clear();
    }

    /**
     * override callback for the route planning, this is called upon the route service failing to calculate a route between the user and the target group (can occur if group and user are on different continents).
     *
     * @param e RouteException e the error that is cause the routing to fail.
     */
    @Override
    public void onRoutingFailure(RouteException e) {
        loadingBar.setVisibility(View.INVISIBLE);
        if (e != null) {
            Toast.makeText(getContext(), "Error when routing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Service not reachable", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * blank override to satisfy the implemented methods.
     */
    @Override
    public void onRoutingStart() {
    }

    /**
     * upon routing succcess this callback is triggered, within this callback the route is displayed to the map, because no alternative routes are being displayed in this iteration of the app, this has been experimented with previously therefore this function indexes through the array of routes and displays all of them ot the map in the users color.
     * this also removes currently existing routes so that the routes cannot be drawn twice at the same time as this could lead to user confusion upon refreshing the page
     *
     * @param route the arraylist of routes that the user can take to get to the destination, by default this is one however numerous routes can be selected.
     * @param shortestRouteIndex this is the index in the arraylist for the shortest path to the destination (the primary preferred route).
     */
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        loadingBar.setVisibility(View.INVISIBLE);
        if (routePolyLines.size() > 0) {
            for (Polyline poly : routePolyLines) {
                poly.remove();
            }
        }
        routePolyLines = new ArrayList<>();
        for (int i = 0; i < route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.parseColor(currentUserHighlighted.getUserMarkerRepresents().getUserColor()));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            routePolyLines.add(polyline);
            User currentUser = currentUserHighlighted.getUserMarkerRepresents();
            if (currentUser != null) {
                InfoWindowData infoWindowData = new InfoWindowData(getUserLocation(currentUser), currentUser.getModeOfTransport(), currentUser.getUsername(), currentUser.getUserPhotoURL(), currentUser.getUserEmailAddress());
                infoWindowData.setTravelDuration(route.get(i).getDurationValue());
                currentUserHighlighted.getUserMarker().setTag(infoWindowData);
                currentUserHighlighted.getUserMarker().showInfoWindow();
            }
        }
    }

    /**
     * upon the routing being cancelled (not possible client side on this app) the loading bar is made invisible.
     */
    @Override
    public void onRoutingCancelled() {
        loadingBar.setVisibility(View.INVISIBLE);
    }
}