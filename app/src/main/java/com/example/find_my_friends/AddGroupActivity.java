package com.example.find_my_friends;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.groupUtil.GroupColors;
import com.example.find_my_friends.groupUtil.GroupUtil;
import com.example.find_my_friends.util.DatePickerFragment;
import com.example.find_my_friends.util.PermissionUtils;
import com.example.find_my_friends.util.TimePickerFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import uk.co.mgbramwell.geofire.android.GeoFire;

import static com.example.find_my_friends.groupUtil.GroupUtil.appendMember;
import static com.example.find_my_friends.groupUtil.GroupUtil.generateKeywords;
import static com.example.find_my_friends.groupUtil.GroupUtil.removeMember;
import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;
import static com.example.find_my_friends.util.Constants.RESULT_LOADED_IMAGE;
import static com.example.find_my_friends.util.Constants.RESULT_LOCATION_REQUEST;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;

/**
 * The activity responsible for adding groups onto the Firestore database, this activity allows the user to append new groups setting their, photo, title, description, location, date, and time of the aforementioned group.
 *
 * if the group is being edited then pass the UID of the group as an extra upon the intent calling this activity, then the information will be loaded appropriately thus allowing the user to edit the information.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class AddGroupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {
    private static String TAG = "AddGroupActivity :";
    private Group groupToAdd;
    private TextView titleTextViewAG;
    private TextView desTextViewAG;
    private TextView dateSpinnerAG;
    private TextView timeSpinnerAG;
    private Button addLocationButton;
    private Button addGroupButton;
    private FloatingActionButton addBackFAB;
    private FloatingActionButton addGroupPhotoFAB;
    private ImageView groupPhoto;
    private Spinner groupColorSpinner;
    private ImageView markerIcon;

    private ArrayList<String> groupColors;
    private String selectedColor;
    private boolean uploadStatus = false;
    private ProgressBar progressBarGroupPhoto;
    private ProgressBar progressBarAddGroup;

    private DocumentReference docRef;
    private Group group;
    private LatLng groupLatLng = null;


    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private GroupUtil groupUtil = new GroupUtil();
    private boolean locationSet = false;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        groupColors = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Group_colors)));
        groupToAdd = new Group();
        if (mUser != null) {
            groupToAdd.setGroupCreatorUserID(mUser.getUid());
            groupToAdd.setGroupCreatorDisplayName(mUser.getDisplayName());
            if (mUser.getPhotoUrl() != null) {
                groupToAdd.setGroupCreatorUserPhotoURL(mUser.getPhotoUrl().toString());
            }
        }
        db = FirebaseFirestore.getInstance();
        groupPhoto = findViewById(R.id.GroupPhotoAG);
        dateSpinnerAG = findViewById(R.id.dateSpinnerAG);
        timeSpinnerAG = findViewById(R.id.timeSpinnerAG);
        addLocationButton = findViewById(R.id.addLocationAG);
        addGroupButton = findViewById(R.id.addNewGroupButton);
        addBackFAB = findViewById(R.id.AddGroupBackFBAG);
        addGroupPhotoFAB = findViewById(R.id.AddGroupPhotoFABAG);
        titleTextViewAG = findViewById(R.id.TitleTextViewAG);
        desTextViewAG = findViewById(R.id.DescriptionOfGroupAG);
        groupColorSpinner = findViewById(R.id.add_group_color_spinner);
        markerIcon = findViewById(R.id.add_group_marker_icon);
        progressBarGroupPhoto = findViewById(R.id.progressBarGroupPhoto);
        progressBarAddGroup = findViewById(R.id.progressBarAddGroup);
        groupColorSpinner.setOnItemSelectedListener(this);
        handleAddGroupPhotoFAB();
        handleBackBTN();
        handleDateSpinnerAG();
        handleTimeSpinnerAG();
        handleAddLocationBTN();
        handleAddNewGroupBTN();
        handleLoadingData();
    }


    /**
     * as the name indicates,this function loads if group already has a color preset (which they will as each time a group is assigned a random color) this color is then loaded and displayed on the spinner with the icon being shaded to be that of the same color as the selection made.
     */
    private void loadColorSettings() {
        groupColorSpinner.setPrompt("Select One");
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(AddGroupActivity.this,
                R.array.Group_colors, android.R.layout.simple_spinner_item);
        groupColorSpinner.setAdapter(colorAdapter);
        if (currentUser.getUserColor() != null) {
            Integer index = getColorIndex(group.getGroupColor());
            if (index != null) {
                groupColorSpinner.setSelection(index);
                markerIcon.setColorFilter(Color.parseColor(groupColors.get(index)));
                selectedColor = groupColors.get(index);
            } else {
                groupColorSpinner.setSelection(0);
            }
        } else {
            groupColorSpinner.setSelection(0);
        }
    }

    /**
     * finds the index in the local arraylist of colors with the inputted (selected) color by the user utilizing the spinner
     *
     * @param colorToMatch String containing the name of the color that the user selected
     * @return the index of the color in the array of colors. (so long as it is found)
     */
    private Integer getColorIndex(String colorToMatch) {
        int i = groupColors.indexOf(colorToMatch);
        if (i != -1) {
            return i;
        } else {
            return null;
        }
    }


    /**
     * add group the activity can take on two purposes, adding a group to the database, or editing an existing group, therefore the intent needs to be checked for extra information regarding a documentID, if the activity has been handed a document id then the relevant material regarding the group needs to be requested from the database and subsequently loaded into the class's internal variables and onscreen variables
     * this should allow the user to make changes to the group if there was one exist or to create a new group if no "documentID" is passed via the intent bundle.
     */
    public void handleLoadingData() {
        String documentID = getIntent().getStringExtra("documentID");
        if (documentID != null) {
            this.docRef = db.collection("Groups").document(documentID);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    group = documentSnapshot.toObject(Group.class);
                    loadColorSettings();
                    updateUI();
                    uploadStatus = true;
                    progressBarGroupPhoto.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            groupColorSpinner.setPrompt("Select One");
            ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(AddGroupActivity.this,
                    R.array.Group_colors, android.R.layout.simple_spinner_item);
            groupColorSpinner.setAdapter(colorAdapter);
        }
    }


    /**
     * upon the user interacting with the spinner this default call back is triggered (only when the user makes a selection, hence the name) upon this callback being triggered, the marker icon is shaded to match the users selection, or if the selection was for the color to return a random color then a random color is generated and the marker icon shaded accordingly
     * note that this also changes the internal variable to that of the users selection.
     *
     * @param parent   the adapter View utilized / clicked (using default)
     * @param view     the view itself for the spinner
     * @param position the position of the selection in the spinners string array (INT)
     * @param id       not used by this function however the id of the view(spinner). as all spinners return to this callback, however im using object comparison to compare if the a spinner is of a specific instance not id.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(this.groupColorSpinner)) {

            if (!groupColors.get(position).equals("random")) {
                selectedColor = groupColors.get(position);
                markerIcon.setColorFilter(Color.parseColor(selectedColor));
                groupColorSpinner.setSelection(position);
            } else {
                String tempRandomColor = GroupColors.randomColor().getStringValue();
                markerIcon.setColorFilter(Color.parseColor(tempRandomColor));
                selectedColor = tempRandomColor;
                groupColorSpinner.setSelection(position);
            }
        }
    }

    /**
     * if a spinner is interacted with but nothing is selected then this funciton is called, this is a nesscary override, however i don't want to take action upon a user interacting with the spinner but not selecting a color
     *
     * @param parent the adapter view for the spinner that called this callback.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * update the information displayed in the activity to the information contained in the group internal variable (only used if the activity is in edit mode)
     */
    public void updateUI() {
        addGroupButton.setText(("Update group"));
        titleTextViewAG.setText(group.getGroupTitle());
        desTextViewAG.setText(group.getGroupDesc());
        dateSpinnerAG.setText(group.getGroupMeetDate());
        timeSpinnerAG.setText(group.getGroupMeetTime());
        groupLatLng = new LatLng(group.getGroupLatitude(), group.getGroupLongitude());
        Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);
        Address addresses;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(groupLatLng.latitude, groupLatLng.longitude, 1).get(0);
            addLocationButton.setText(addresses.getAddressLine(0));
        } catch (IOException e) {
            Log.e(TAG, "onClick: Error when trying to get the address, no address provided");
            addLocationButton.setText(("no address set"));
        }

    }

    /**
     * opens an implicit intent to select a photo from the clients device
     */
    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }

    /**
     * upload the local image to the database from the declared local image path to the declare path on the server and listen for the status of this request, if successful then the download uri is requested, else if it is a failure, then a message displaying why to the user is generated.
     *
     * @param imageURI   path to the resource being uploaded on the clients devices
     * @param uploadPath path on the server the resource upload destination is
     */
    private void uploadPhoto(Uri imageURI, String uploadPath) {
        if (imageURI != null && imageURI.getPath() != null) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(uploadPath);
            UploadTask uploadTask = storageReference.putFile(imageURI);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e.getCause());
                    Toast.makeText(AddGroupActivity.this, e.toString(),
                            Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    getDownloadURI(storageReference);
                }
            });
        }
    }

    /**
     * once uploaded to maintain reference to the groups photo the download reference needs to be ether updated (when editing) or created when adding a new group. this is so that the group can have the photo for the group index by other users, this download uri path is then uploaded to the database as the uri for the photo of the group.
     *
     * @param storageReference reference to the image resource that was just uploaded.
     */
    private void getDownloadURI(StorageReference storageReference) {
        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(AddGroupActivity.this, "Photo uploaded",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, uri.toString());
                        if (group == null) {
                            groupToAdd.setGroupPhotoURI(uri.toString());
                        } else {
                            group.setGroupPhotoURI(uri.toString());
                        }
                        loadGroupPhoto();
                        uploadStatus = true;
                        progressBarGroupPhoto.setVisibility(View.INVISIBLE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddGroupActivity.this, "Linking the selected photo failed, photo did not upload correctly (connection interrupted)",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * using the download URI for the group the photo is loaded into group imageview using a glide.
     */
    private void loadGroupPhoto() {
        if (group == null) {
            if (groupToAdd.getGroupPhotoURI() != null) {
                Glide.with(this).load(groupToAdd.getGroupPhotoURI()).into(groupPhoto);
            }
        } else {
            Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);
        }

    }

    /**
     * on activity result is returned once the implicit intent for media is fulfilled or cancelled by the user, from this the request can be handled and the image URI can be determined from the Data attached to the intent.
     *
     * @param requestCode the code authoered by this activity, checking that the result is for the implicit intent made by this activity / app (INT)
     * @param resultCode  result code, the state of the transaction, if the transction was a failure or success (INT)
     * @param data        and Intent which contains an internal bundle that contains the data for the imageURI
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOADED_IMAGE) {
            if (data.getData() != null) {
                Uri imageURI = data.getData();
                String uploadPath;
                if (group == null) {
                    uploadPath = "images/groups/groupPhoto" + groupToAdd.getGroupID();
                } else {
                    uploadPath = "images/groups/groupPhoto" + group.getGroupID();
                }
                uploadPhoto(imageURI, uploadPath);
            } else {
                Toast.makeText(this, "path to image is corrupt, or no path no longer exists",
                        Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK && requestCode == RESULT_LOCATION_REQUEST) {
            if (data != null) {
                locationSet = true;
                if (group == null) {
                    groupToAdd.setGroupLatitude(data.getDoubleExtra("Lat", 0.0));
                    groupToAdd.setGroupLongitude(data.getDoubleExtra("Lng", 0.0));
                } else {
                    group.setGroupLatitude(data.getDoubleExtra("Lat", 0.0));
                    group.setGroupLongitude(data.getDoubleExtra("Lng", 0.0));

                }
                Bundle args = data.getBundleExtra("BUNDLE");
                ArrayList<Address> addresses = null;
                if (args != null) {
                    //this cannot be checked for before attempting, however this cannot fail as i am manually setting this part of the bundle.
                    addresses = (ArrayList<Address>) args.getSerializable("arrayList");
                }
                if (addresses != null) {
                    addLocationButton.setText(addresses.get(0).getAddressLine(0));
                }
            }
        }
    }

    /**
     * upon requesting permission for the gallery the response from the user (to confirm or deny permission) is sent here, this is then handled so that if permission is granted an implicit intent can be started to select a photo, else if it is denied then a message is shown to the user through the medium of a snackbar notification explaining acknowledgement that they denied the request
     *
     * @param requestCode  (INT) the code authored by this activity for the permission request, used to check it is our request triggering this callback
     * @param permissions  (String array) contain the array of permissions requested
     * @param grantResults (int array) containing a parallel number of members containing the status of the request (confirmed or denied)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_GALLERY_ACCESS) {
            return;
        }
        if (PermissionUtils.checkReadExternalPermission(this)) {
            loadPhoto();
        } else {
            Snackbar.make(addGroupPhotoFAB, "Permission to access gallery denied", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * upon the back button being pressed this callback is triggered, in which this activity is exited
     */
    private void handleBackBTN() {
        addBackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * handles the user interacting with the textbox representing the date spinner ( spinners can't be interacted with in this manner, however a button looks out of place); this will pass the current date to the dialog provided by the android os of the users device, so that the selection is made from the current date.
     */
    private void handleDateSpinnerAG() {
        dateSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), DATEPICKER_TAG_KEY);
            }
        });

    }

    /**
     * handles the user interacting with the textbox representing the time spinner (spinners can't be interacted with in this manner, however a button looks out of place); this will pass the current time to the time dialog provided by the android os of the users device, so that the selection is made from the current time and time zone format.
     */
    private void handleTimeSpinnerAG() {
        timeSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKER_TAG_KEY);
            }
        });
    }

    /**
     * handles the user interacting with the set location button, upon clicking this button the activity SetLocation is launched with the intent bundle containing the groups last known location if the current activity is in edit mode, or not if not.
     * this is started for a result.
     */
    private void handleAddLocationBTN() {
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SetLocationActivity.class);
                if (group != null) {
                    intent.putExtra("Lat", groupLatLng.latitude);
                    intent.putExtra("Lng", groupLatLng.longitude);
                    intent.putExtra("State", "true");
                } else {
                    intent.putExtra("State", "false");
                }
                startActivityForResult(intent, RESULT_LOCATION_REQUEST);
            }
        });
    }

    /**
     * upon the add new group button being clicked this callback is triggered, the onclick lsitener is called, in which the group mode is checked (edit mode or append new group) from this appropriate checks are made to the data and then the changes are uploaded to the database, note if the group is being updated the checks are not as extensive as data cannot be uploaded to be null
     * therefore this doesn't need to be checked.
     * <p>
     * the group is then appended or updated on the firestore database.
     */
    private void handleAddNewGroupBTN() {
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarAddGroup.setVisibility(View.VISIBLE);
                if (group == null) {
                    groupToAdd.setGroupID(UUID.randomUUID().toString());
                    groupToAdd.setGroupTitle(titleTextViewAG.getText().toString());
                    groupToAdd.setGroupDesc(desTextViewAG.getText().toString());
                    groupToAdd.setGroupColor(selectedColor);
                    generateKeywords(groupToAdd, groupToAdd.getGroupTitle());
                    if (!uploadStatus) {
                        Snackbar.make(addGroupButton, "Please wait for the photo to be uploaded", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (groupToAdd.getGroupPhotoURI() == null) {
                        Snackbar.make(addGroupButton, "Please upload a group Photo", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (!locationSet) {
                        Snackbar.make(addGroupPhotoFAB, "Please set a location of the group", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (groupToAdd.getGroupTitle().equals("")) {
                        Snackbar.make(addGroupPhotoFAB, "Please Give the group a title", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (groupToAdd.getGroupDesc().equals("")) {
                        Snackbar.make(addGroupPhotoFAB, "Please Give the group a Description", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (groupToAdd.getGroupMeetTime() == null) {
                        Snackbar.make(addGroupPhotoFAB, "please set a time", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else if (groupToAdd.getGroupMeetDate() == null) {
                        Snackbar.make(addGroupPhotoFAB, "please set a date", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        appendMember(groupToAdd, mUser);
                        if (groupUtil.uploadGroup(groupToAdd, db)) {
                            progressBarAddGroup.setVisibility(View.INVISIBLE);
                            finish();
                            return;
                        } else {
                            removeMember(groupToAdd, mUser);
                            Snackbar.make(addGroupPhotoFAB, "Creation of group failed, check data and try again.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                    progressBarAddGroup.setVisibility(View.INVISIBLE);
                } else {
                    if (!uploadStatus) {
                        Snackbar.make(addGroupButton, "Please wait for the photo to be uploaded", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        group.setGroupDesc(desTextViewAG.getText().toString());
                        group.setGroupTitle(titleTextViewAG.getText().toString());
                        docRef.update("groupPhotoURI", group.getGroupPhotoURI());
                        docRef.update("groupTitle", group.getGroupTitle());
                        docRef.update("groupDesc", group.getGroupDesc());
                        docRef.update("groupLatitude", group.getGroupLatitude());
                        docRef.update("groupLongitude", group.getGroupLongitude());
                        docRef.update("groupMeetDate", group.getGroupMeetDate());
                        docRef.update("groupMeetTime", group.getGroupMeetTime());
                        docRef.update("groupColor", selectedColor);
                        GeoFire geoFire = new GeoFire(db.collection("Groups"));
                        geoFire.setLocation(group.getGroupID(), group.getGroupLatitude(), group.getGroupLongitude());
                        progressBarAddGroup.setVisibility(View.INVISIBLE);
                        finish();
                    }
                }
            }
        });
    }

    /**
     * sets an onclick listener for the add photo FAB which is only called when the user interacts with the FAB, from this a check for gallery permissions is made, and subsequently loadPhoto() is called to create an implicit intent requesting the user to select a photo
     */
    private void handleAddGroupPhotoFAB() {
        addGroupPhotoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadStatus = false;
                progressBarGroupPhoto.setVisibility(View.VISIBLE);
                if (!PermissionUtils.checkReadExternalPermission(AddGroupActivity.this)) {
                    PermissionUtils.requestReadExternalPermission(AddGroupActivity.this);
                } else {
                    loadPhoto();
                }

            }
        });
    }

    /**
     * handles the callback provided by the UI interface for setting a date, only called if the user actually confirms a date, within this function it handles saving the date to the local variables of this class to be use when uploading the changes or the when creating a group
     *
     * @param view       the view which called this callback upon user submission
     * @param year       the year of the date they selected
     * @param month      the month that year
     * @param dayOfMonth the day of that month.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String setDate = DateFormat.getDateInstance().format(calendar.getTime());
        if (group == null) {
            groupToAdd.setGroupMeetDate(setDate);
        } else {
            group.setGroupMeetDate(setDate);
        }
        dateSpinnerAG.setText(setDate);
    }

    /**
     * handles the callback provided by the ui interface for setting the time, only called if the user actually confirms a time. withiin this function it handles saving the time to the local variables of this class to be used in updating a group or creating a group depending on the mode this class is used in.
     *
     * @param view      the view which called this callback (origin)
     * @param hourOfDay the hour of the time selected
     * @param minute    the minute of that hour.
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String setTime;
        if (minute < 9) {
            setTime = hourOfDay + ":0" + minute;
        } else {
            setTime = hourOfDay + ":" + minute;
        }
        if (group == null) {
            groupToAdd.setGroupMeetTime(setTime);
        } else {
            group.setGroupMeetTime(setTime);
        }
        timeSpinnerAG.setText(setTime);
    }
}
