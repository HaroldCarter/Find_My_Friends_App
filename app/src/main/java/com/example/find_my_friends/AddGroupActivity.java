package com.example.find_my_friends;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.groupUtil.Group;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import static com.example.find_my_friends.util.Constants.DATEPICKER_TAG_KEY;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;
import static com.example.find_my_friends.util.Constants.RESULT_LOADED_IMAGE;
import static com.example.find_my_friends.util.Constants.RESULT_LOCATION_REQUEST;
import static com.example.find_my_friends.util.Constants.TIMEPICKER_TAG_KEY;

//the class badly needs to have the oncreate and one destroy correctly implemented.
public class AddGroupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
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
    private boolean uploadStatus = false;
    //private  mCompressor;

    private DocumentReference docRef;
    private Group group;
    private LatLng groupLatLng = null;


    private FirebaseStorage storageRef;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private boolean locationSet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);


        storageRef = FirebaseStorage.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //mCompressor = new FileCompressor(this);
        groupToAdd = new Group();
        if(mUser != null){
            groupToAdd.setGroupCreatorUserID(mUser.getUid());
            groupToAdd.setGroupCreatorDisplayName(mUser.getDisplayName());
            groupToAdd.setGroupCreatorUserPhotoURL(mUser.getPhotoUrl().toString());
        }


        db = FirebaseFirestore.getInstance();

        groupPhoto = findViewById(R.id.GroupPhotoAG);
        dateSpinnerAG = findViewById(R.id.dateSpinnerAG);
        timeSpinnerAG =  findViewById(R.id.timeSpinnerAG);
        addLocationButton =  findViewById(R.id.addLocationAG);
        addGroupButton = findViewById(R.id.addNewGroupButton);
        addBackFAB =  findViewById(R.id.AddGroupBackFBAG);
        addGroupPhotoFAB =  findViewById(R.id.AddGroupPhotoFABAG);
        titleTextViewAG = findViewById(R.id.TitleTextViewAG);
        desTextViewAG = findViewById(R.id.DescriptionOfGroupAG);


        handleAddGroupPhotoFAB();
        handleBackBTN();
        handleDateSpinnerAG();
        handleTimeSpinnerAG();
        handleAddLocationBTN();
        handleAddNewGroupBTN();
        handleLoadingData();
    }


    public void handleLoadingData(){
        String documentID =getIntent().getStringExtra("documentID");
        if (documentID!= null){
            this.docRef = db.collection("Groups").document(documentID);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    group = documentSnapshot.toObject(Group.class);
                    updateUI();
                    uploadStatus = true;
                    //stops errors regarding the button not allowing to update because the app things its uploading a photo.
                }
            });
        }else{
        }
    }


    public void updateUI(){
        //groupCreatorTitle.setText(group.getGroupCreatorDisplayName());
        //groupCreatorEmail.setText(gro)
        addGroupButton.setText("Update group");
        titleTextViewAG.setText(group.getGroupTitle());
        desTextViewAG.setText(group.getGroupDesc());
        dateSpinnerAG.setText(group.getGroupMeetDate());
        timeSpinnerAG.setText(group.getGroupMeetTime());
        groupLatLng = new LatLng(group.getGroupLatitude(), group.getGroupLongitude());

        Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);

        Address addresses ;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(groupLatLng.latitude, groupLatLng.longitude, 1).get(0);
            addLocationButton.setText(addresses.getAddressLine(0));
        }catch(IOException e){
            Log.e(TAG, "onClick: Error when trying to get the address, no address provided");
            addresses = null;
            addLocationButton.setText("no address set");
        }

    }

    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }


    private void uploadPhoto(Uri imageURI, String uploadPath){
        if(imageURI != null && imageURI.getPath() != null) {
            final StorageReference storageReference =  FirebaseStorage.getInstance().getReference().child(uploadPath);
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
                    //get the download Uri and set it to the groups
                    getDownloadURI(storageReference);
                }
            });
        }
    }

    private void getDownloadURI(StorageReference storageReference){
        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(AddGroupActivity.this, "Photo uploaded",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, uri.toString());
                        if(group == null){
                            groupToAdd.setGroupPhotoURI(uri.toString());
                        }else {
                            group.setGroupPhotoURI(uri.toString());
                        }
                        loadGroupPhoto();
                        uploadStatus = true;
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

    private void loadGroupPhoto(){
        if(groupToAdd.getGroupPhotoURI() != null) {
            if(group == null) {
                Glide.with(this).load(groupToAdd.getGroupPhotoURI()).into(groupPhoto);
            }else{
                Glide.with(this).load(group.getGroupPhotoURI()).into(groupPhoto);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOADED_IMAGE) {
            if (data.getData() != null) {
                Uri imageURI = data.getData();
                String uploadPath = "images/groups/groupPhoto" + groupToAdd.getGroupID();
                uploadPhoto(imageURI, uploadPath);
            } else {
                Toast.makeText(this, "path to image is corrupt, or no path no longer exists",
                        Toast.LENGTH_LONG).show();
            }
        }
        if(resultCode == RESULT_OK && requestCode == RESULT_LOCATION_REQUEST){
            if(data != null){
                //double lat = data.getDoubleExtra("Lat", 0.0);
                //double lng = data.getDoubleExtra("Lng", 0.0);
                locationSet = true;
                if(group == null) {
                    groupToAdd.setGroupLatitude(data.getDoubleExtra("Lat", 0.0));
                    groupToAdd.setGroupLongitude(data.getDoubleExtra("Lng", 0.0));
                }else{
                    group.setGroupLatitude(data.getDoubleExtra("Lat", 0.0));
                    group.setGroupLongitude(data.getDoubleExtra("Lng", 0.0));

                }
               // groupToAdd.setGroupLocation(new GeoPoint(lat, lng));
                //maybe update the button text to show the nearest address?
                Bundle args = data.getBundleExtra("BUNDLE");
                ArrayList<Address> addresses = null;
                if(args != null) {
                    //unknown how to check this type of cast (isInstance doesn't work), however i am specifically controlling the type contained in this bundle therefore this complaint is not rel
                    addresses = (ArrayList<Address>) args.getSerializable("arrayList");
                }
                if(addresses != null){
                    addLocationButton.setText(addresses.get(0).getAddressLine(0));
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_GALLERY_ACCESS) {
            return;
        }
        if (PermissionUtils.checkReadExternalPermission(this)) {
            //handle adding the photo to the Group.
            loadPhoto();
        }else{
            Snackbar.make(addGroupPhotoFAB, "Permission to access gallery denied", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    private void handleBackBTN(){
        addBackFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void handleDateSpinnerAG(){
        dateSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),DATEPICKER_TAG_KEY);
            }
        });

    }

    private void handleTimeSpinnerAG(){
        timeSpinnerAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKER_TAG_KEY);
            }
        });
    }

    private void handleAddLocationBTN(){
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SetLocationActivity.class);
                if(group != null) {
                    intent.putExtra("Lat", groupLatLng.latitude);
                    intent.putExtra("Lng", groupLatLng.longitude);
                    intent.putExtra("State", "true");
                }else {
                    intent.putExtra("State", "false");
                }

                startActivityForResult(intent, RESULT_LOCATION_REQUEST);
            }
        });
    }

    private void handleAddNewGroupBTN(){
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the onscreen display to the data on
                if (group == null) {
                    groupToAdd.setGroupID(UUID.randomUUID().toString());
                    groupToAdd.setGroupTitle(titleTextViewAG.getText().toString());
                    groupToAdd.setGroupDesc(desTextViewAG.getText().toString());
                    //put the creator as a member and as the creator this saves over complicates later functions and makes literal sense.
                    groupToAdd.appendMember(mUser);


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
                    } else {
                        //add the group to the database.
                        if (groupToAdd.uploadGroup(db)) {
                            finish();
                        } else {
                            Snackbar.make(addGroupPhotoFAB, "Creation of group failed, check data and try again.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }


                }else{
                    if (!uploadStatus) {
                        Snackbar.make(addGroupButton, "Please wait for the photo to be uploaded", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }else {
                        group.setGroupDesc(desTextViewAG.getText().toString());
                        group.setGroupTitle(titleTextViewAG.getText().toString());
                        docRef.update("groupPhotoURI", group.getGroupPhotoURI());
                        docRef.update("groupTitle", group.getGroupTitle());
                        docRef.update("groupDesc", group.getGroupDesc());
                        docRef.update("groupLatitude", group.getGroupLatitude());
                        docRef.update("groupLongitude", group.getGroupLongitude());
                        docRef.update("groupMeetDate", group.getGroupMeetDate());
                        docRef.update("groupMeetTime", group.getGroupMeetTime());
                        finish();
                    }
                }
            }
        });
    }

    private void handleAddGroupPhotoFAB(){
        uploadStatus = false;
        addGroupPhotoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make a request to get permission
                if(!PermissionUtils.checkReadExternalPermission(AddGroupActivity.this)){
                    PermissionUtils.requestReadExternalPermission(AddGroupActivity.this);
                }else{
                    loadPhoto();
                }

            }
        });
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String setDate = DateFormat.getDateInstance().format(calendar.getTime());
        if(group == null) {
            groupToAdd.setGroupMeetDate(setDate);
        }else{
            group.setGroupMeetDate(setDate);
        }
        dateSpinnerAG.setText(setDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String setTime;
        if(minute < 9){
            setTime  = hourOfDay + ":0" + minute;
        }else{
            setTime = hourOfDay + ":" + minute;
        }

        if(group == null) {
            groupToAdd.setGroupMeetTime(setTime);
        }else{
            group.setGroupMeetTime(setTime);
        }
        timeSpinnerAG.setText(setTime);
    }
}
