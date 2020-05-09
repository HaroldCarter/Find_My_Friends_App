package com.example.find_my_friends.ui.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.GroupColors;
import com.example.find_my_friends.ui.dialog_windows.ChangeEmailDialog;
import com.example.find_my_friends.ui.dialog_windows.ChangePasswordDialog;
import com.example.find_my_friends.ui.dialog_windows.ChangeUsernameDialog;
import com.example.find_my_friends.ui.dialog_windows.ReAuthUserDialog;
import com.example.find_my_friends.util.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;
import static com.example.find_my_friends.util.Constants.RESULT_LOADED_IMAGE;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Button changePasswordBTN;
    private Button changeUsernameBTN;
    private Button changeEmailBTN;

    private Button mapSettingsSaveChangesBTN;
    private Spinner mapSettingsMarkerColorSpinner;
    private Spinner mapSettingsMapUpdateDuration;

    private FloatingActionButton addProfilePhotoFAB;
    private ImageView profilePhotoImageView;
    private ProgressBar progressBar;
    private ImageView markerPreviewImageview;

    private final String TAG = "Settings_Fragment :";

    private boolean uploadStatus = true;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<String> groupColors;
    private ArrayList<String> updateRates;
    private String selectedColor;
    private String selectedUpdateRate;



    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Toolbar toolbar = root.findViewById(R.id.settings_menubar);
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Settings");


        changePasswordBTN = root.findViewById(R.id.settings_page_change_passwordBTN);//
        changeUsernameBTN = root.findViewById(R.id.settings_page_change_usernameBTN);//
        changeEmailBTN = root.findViewById(R.id.settings_page_changeEmailBTN);//

        mapSettingsSaveChangesBTN = root.findViewById(R.id.setting_page_map_setting_saveBTN);//
        mapSettingsMarkerColorSpinner = root.findViewById(R.id.map_icon_color_spinner);//
        mapSettingsMapUpdateDuration = root.findViewById(R.id.map_update_duration_spinner);
        markerPreviewImageview = root.findViewById(R.id.settings_page_map_settings_icon_preview_imageview);


        groupColors= new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Group_colors)));

        updateRates= new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Update_Duration)));

        addProfilePhotoFAB = root.findViewById(R.id.setting_page_change_photoBTN);//

        profilePhotoImageView = root.findViewById(R.id.setting_page_profile_photo_imageView);
        progressBar = root.findViewById(R.id.settings_page_progressBar_profilePhoto);


        loadMapSettings();


        loadUsersPhoto(null);
        handleChangePasswordBTN();
        handleChangeEmailBTN();
        handleChangeUsernameBTN();
        handleAddProfilePhotoFAB();
        handleMapSettingsSaveChangesBTN();

        if(getActivity() != null) {
            getActivity().setActionBar(toolbar);
        }else{
            //display an error saying the program has lost reference to itself.
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null && uploadStatus)((MainActivity) getActivity()).openDrawer();else{
                    Toast.makeText(getActivity(), "Please wait for your profile photo to be uploaded",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }



    private Integer getColorIndex(String colorToMatch){
        int i =  groupColors.indexOf(colorToMatch);
        if(i != -1){
            return i;
        }else{
            return null;
        }
    }


    private Integer getDurationIndex(String durationToMatch){
        int i =  updateRates.indexOf(durationToMatch);
        if(i != -1){
            return i;
        }else{
            return null;
        }
    }


    private void loadMapSettings(){
        mapSettingsMarkerColorSpinner.setPrompt("Select One");
        mapSettingsMapUpdateDuration.setPrompt("Select One");


       if(getContext() != null) {
           ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(getContext(),
                   R.array.Group_colors, android.R.layout.simple_spinner_item);
           mapSettingsMarkerColorSpinner.setAdapter(colorAdapter);

           ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(getContext(),
                   R.array.Update_Duration, android.R.layout.simple_spinner_item);
           mapSettingsMapUpdateDuration.setAdapter(durationAdapter);

       }


        if(currentUser.getUserColor() != null) {
            Integer index = getColorIndex(currentUser.getUserColor());
            if(index!= null) {
                mapSettingsMarkerColorSpinner.setSelection(index);
                markerPreviewImageview.setColorFilter(Color.parseColor(groupColors.get(index)));
            }else{
                mapSettingsMarkerColorSpinner.setSelection(0);
            }
        }else{
            mapSettingsMarkerColorSpinner.setSelection(0);
        }

        if(currentUser.getUserUpdateRate() != null) {
            Integer index = getDurationIndex(currentUser.getUserUpdateRate().toString() + "S");
            if (index != null) {
                mapSettingsMapUpdateDuration.setSelection(index);
            } else {
                mapSettingsMapUpdateDuration.setSelection(0);
            }
        }else{
            mapSettingsMapUpdateDuration.setSelection(0);
        }

        mapSettingsMarkerColorSpinner.setOnItemSelectedListener(this);
        mapSettingsMapUpdateDuration.setOnItemSelectedListener(this);

    }




    private void loadUsersPhoto(String Uri){
        if(getContext() != null) {
            if(Uri == null)
            Glide.with(getContext()).load(currentUser.getUserPhotoURL()).into(this.profilePhotoImageView);
            else Glide.with(getContext()).load(Uri).into(this.profilePhotoImageView);
        }
    }


    private void handleChangePasswordBTN(){
        changePasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPasswordDialog();
            }
        });
    }


    private void handleChangeUsernameBTN(){
        changeUsernameBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUsernameDialog();
            }
        });
    }

    private void handleChangeEmailBTN(){
        changeEmailBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmailDialog();
            }
        });
    }

    private void handleMapSettingsSaveChangesBTN(){
        mapSettingsSaveChangesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedColor == null && selectedUpdateRate == null){
                    Toast.makeText(getActivity(), "No changes made",
                            Toast.LENGTH_LONG).show();

                    return;
                }
                boolean updateColor = false;
                boolean updateRate =false;
                if(selectedColor != null){
                    //update the color to the new color
                    if(currentUser.getUserColor() != null && !(currentUser.getUserColor().equals(selectedColor))){
                        //update to the new color, else
                        updateUserColor(selectedColor);
                        updateColor = true;
                    }else if (currentUser.getUserColor() == null){
                        //first time the user has updated their color.
                        updateUserColor(selectedColor);
                        updateColor = true;
                    }
                }
                if(selectedUpdateRate != null){
                    Integer updateRateInt = updateRateToInt(selectedUpdateRate);
                    if(currentUser.getUserUpdateRate() != null && !(currentUser.getUserUpdateRate().equals(updateRateInt))){
                        updateUserUpdateRate(updateRateToInt(selectedUpdateRate));
                        updateRate = true;
                    }else if(currentUser.getUserUpdateRate() == null){
                        updateUserUpdateRate(updateRateToInt(selectedUpdateRate));
                        updateRate = true;
                    }
                }



                //display what was uppdated to the user.

                if(updateColor && updateRate){
                    Toast.makeText(getActivity(), "Color and Update rate changes saved",
                            Toast.LENGTH_LONG).show();
                }else if(updateColor){
                    Toast.makeText(getActivity(), "Color change saved",
                            Toast.LENGTH_LONG).show();
                }else if(updateRate){
                    Toast.makeText(getActivity(), "Update rate change saved",
                            Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "settings already configured, no save required",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private int updateRateToInt(String updateRate){
        return Integer.valueOf(updateRate.substring(0, updateRate.length() - 1));
    }

    private void handleAddProfilePhotoFAB(){
        addProfilePhotoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadStatus = false;
                progressBar.setVisibility(View.VISIBLE);
                PermissionUtils.requestReadExternalPermission(getActivity());
                if (PermissionUtils.checkReadExternalPermission(getActivity())) {
                    loadPhoto();
                }
            }
        });
    }



    private void openUsernameDialog(){
        if(this.getActivity()!= null) {
            ChangeUsernameDialog changeUsernameDialog = new ChangeUsernameDialog();
            changeUsernameDialog.setChangeUsernameDialogListener(new ChangeUsernameDialog.ChangeUsernameDialogListener() {
                @Override
                public void returnResult(String Username) {
                    updateUsersUsername(Username);
                }
            });
            changeUsernameDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    private void updateUsersUsername(String username){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            //update the authlink
            UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
            mAuth.getCurrentUser().updateProfile(updateRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "username Updated",
                            Toast.LENGTH_LONG).show();
                    updateDatebaseUsersUsername(username);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "username failed to updated, Error : + " + e.toString());
                    Toast.makeText(getActivity(), "new profile Photo Failed to link to user",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateDatebaseUsersUsername(String username){
        currentUserDocument.getReference().update("username", username);
    }


    private void openEmailDialog(){
        if(this.getActivity()!= null) {
            ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
            changeEmailDialog.setChangeEmailDialogListener(new ChangeEmailDialog.ChangeEmailDialogListener() {
                @Override
                public void returnResult(String Email) {
                    reAuthPopupEmail(Email);
                }
            });
            changeEmailDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    private void reAuthPopupEmail(final String Email){
        if(this.getActivity()!= null) {
            ReAuthUserDialog reAuthUserDialog = new ReAuthUserDialog();
            reAuthUserDialog.setTitle("Please enter your current password to confirm this change");
            reAuthUserDialog.setReAuthUserDialogListener(new ReAuthUserDialog.ReAuthUserDialogListener() {
                                                             @Override
                                                             public void returnResult(String Password, Boolean Authenticated) {
                                                                 if(Authenticated){
                                                                     updateUsersEmailAddress(Email, Password);
                                                                 }
                                                             }
                                                         });
            reAuthUserDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    private void updateUsersEmailAddress(String emailAddress, String Password){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), Password);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated.");
                            user.updateEmail(emailAddress)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                updateDatebaseUsersEmailAddress(emailAddress);
                                                Toast.makeText(getContext(), "Email updated",
                                                        Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getContext(), "Failure: email failed to update",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    private void updateDatebaseUsersEmailAddress(String emailAddress){
        currentUserDocument.getReference().update("userEmailAddress", emailAddress);
    }


    private void openPasswordDialog(){
        if(this.getActivity()!= null) {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.setChangePasswordDialogListener(new ChangePasswordDialog.ChangePasswordDialogListener() {
                @Override
                public void returnResult(String Password) {
                    reAuthPopupPassword(Password);
                    //reauth the user.
                }
            });
            changePasswordDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }


    private void reAuthPopupPassword(final String PasswordInput){
        if(this.getActivity()!= null) {
            ReAuthUserDialog reAuthUserDialog = new ReAuthUserDialog();
            reAuthUserDialog.setTitle("Please enter your current password to confirm this change");
            reAuthUserDialog.setReAuthUserDialogListener(new ReAuthUserDialog.ReAuthUserDialogListener() {
                @Override
                public void returnResult(String Password, Boolean Authenticated) {
                    if(Authenticated){
                        updateUsersPassword(PasswordInput);
                    }
                }
            });
            reAuthUserDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    private void updateUsersPassword(String Password){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), Password);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated.");
                            user.updatePassword(Password)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Password updated",
                                                        Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getContext(), "Failure: Password failed to update",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }



    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_GALLERY_ACCESS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission to storage granted",
                            Toast.LENGTH_SHORT).show();
                    loadPhoto();
                } else {
                    Toast.makeText(getContext(), "Permission was denied",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOADED_IMAGE) {
            if (mAuth.getCurrentUser() == null) {
                final Intent dataFinal = data;
                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        handleGettingURIFromData(dataFinal);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),  "server error, new users are no possible to be created at this time",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                handleGettingURIFromData(data);
            }


        } else {
            Toast.makeText(getActivity(), "path to image is corrupt, or no path no longer exists",
                    Toast.LENGTH_LONG).show();
        }
    }


    private void handleGettingURIFromData(Intent data) {
        if (data.getData() != null && getActivity() != null) { //&& mAuth.getCurrentUser() != null) {
            ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), data.getData());
            Uri imageURI = data.getData();
            String uploadPath = "images/users/userphoto" + UUID.randomUUID().toString();
            uploadPhoto(imageURI, uploadPath);
        }
    }


    private void uploadPhoto(Uri imageURI, String uploadPath) {
        if (imageURI != null && imageURI.getPath() != null) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(uploadPath);
            UploadTask uploadTask = storageReference.putFile(imageURI);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e.getCause());
                    Toast.makeText(getActivity(), e.toString(),
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
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


    private void getDownloadURI(StorageReference storageReference) {
        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(getActivity(), "Photo uploaded",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, uri.toString());


                        updateProfileURI(uri);
                        uploadStatus = true;
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Linking the selected photo failed, photo did not upload correctly (connection interrupted)",
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                });
    }


    private void updateProfileURI(Uri uri){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            //update the authlink
            UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                mAuth.getCurrentUser().updateProfile(updateRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "profile Photo linked to user",
                                Toast.LENGTH_LONG).show();
                        updateDatabaseProfileURI(uri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "new profile Photo Failed to link to user + " + e.toString());
                        Toast.makeText(getActivity(), "new profile Photo Failed to link to user",
                                Toast.LENGTH_LONG).show();
                    }
                });
        }
    }


    private void updateDatabaseProfileURI(Uri uri){
        db.collection("Users").document(currentUser.getUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                documentSnapshot.getReference().update("userPhotoURL", uri.toString());
                loadUsersPhoto(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed to update the profile photo link in the database, photo will not change",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateUserColor(String colorToUpdateTO){
        currentUserDocument.getReference().update("userColor", colorToUpdateTO);
    }

    private void updateUserUpdateRate(int newUpdateRate){
        currentUserDocument.getReference().update("userUpdateRate", newUpdateRate);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.equals(this.mapSettingsMarkerColorSpinner)) {

            if(!groupColors.get(position).equals("random")) {
                selectedColor = groupColors.get(position);
                markerPreviewImageview.setColorFilter(Color.parseColor(selectedColor));
            }else{
                String tempRandomColor = GroupColors.randomColor().getStringValue();
                markerPreviewImageview.setColorFilter(Color.parseColor(tempRandomColor));
                selectedColor =tempRandomColor;
                //maybe update the groups color to a random color.
            }
        }else if (parent.equals(this.mapSettingsMapUpdateDuration)){
            selectedUpdateRate = updateRates.get(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}