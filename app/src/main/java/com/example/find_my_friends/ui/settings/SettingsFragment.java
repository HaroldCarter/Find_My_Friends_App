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

/**
 * A class containing all the functionality of the settings fragment, allowing the users' details to be changed and allowing for the map settings to be changed.
 *
 * @author Harold Carter
 * @version 6.0
 */
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


    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return the View for the fragment's UI, or null.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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
        groupColors = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Group_colors)));
        updateRates = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Update_Duration)));
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
        if (getActivity() != null) {
            getActivity().setActionBar(toolbar);
        } else {
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && uploadStatus)
                    ((MainActivity) getActivity()).openDrawer();
                else {
                    Toast.makeText(getActivity(), "Please wait for your profile photo to be uploaded",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }


    /**
     * given a parameter of a color from the group colors arraylist the color is index is returned (if the color is a valid index)
     *
     * @param colorToMatch String for the enumerated value of the color to match
     * @return Int index of the color in the arraylist
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
     * given a parameter of a duration from the spinner's arraylist the durations is index is returned (if the durations is a valid index)
     *
     * @param durationToMatch String for the value of the duration to match
     * @return Int index of the duration in the arraylist
     */
    private Integer getDurationIndex(String durationToMatch) {
        int i = updateRates.indexOf(durationToMatch);
        if (i != -1) {
            return i;
        } else {
            return null;
        }
    }

    /**
     *upon the view being created this fucntion is calld to load the current users existing settings for the map settings, positions of the spinners are set and the icon coloured accordingly
     *
     */
    private void loadMapSettings() {
        mapSettingsMarkerColorSpinner.setPrompt("Select One");
        mapSettingsMapUpdateDuration.setPrompt("Select One");
        if (getContext() != null) {
            ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.Group_colors, android.R.layout.simple_spinner_item);
            mapSettingsMarkerColorSpinner.setAdapter(colorAdapter);

            ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.Update_Duration, android.R.layout.simple_spinner_item);
            mapSettingsMapUpdateDuration.setAdapter(durationAdapter);

        }
        if (currentUser.getUserColor() != null) {
            Integer index = getColorIndex(currentUser.getUserColor());
            if (index != null) {
                mapSettingsMarkerColorSpinner.setSelection(index);
                markerPreviewImageview.setColorFilter(Color.parseColor(groupColors.get(index)));
            } else {
                mapSettingsMarkerColorSpinner.setSelection(0);
            }
        } else {
            mapSettingsMarkerColorSpinner.setSelection(0);
        }
        if (currentUser.getUserUpdateRate() != null) {
            Integer index = getDurationIndex(currentUser.getUserUpdateRate().toString() + "S");
            if (index != null) {
                mapSettingsMapUpdateDuration.setSelection(index);
            } else {
                mapSettingsMapUpdateDuration.setSelection(0);
            }
        } else {
            mapSettingsMapUpdateDuration.setSelection(0);
        }
        mapSettingsMarkerColorSpinner.setOnItemSelectedListener(this);
        mapSettingsMapUpdateDuration.setOnItemSelectedListener(this);
    }


    /**
     * loads the current users profile photo into the imageview displaying the user profilephoto.
     *
     * @param Uri String URI for the download URL resource from the server
     */
    private void loadUsersPhoto(String Uri) {
        if (getContext() != null) {
            if (Uri == null)
                Glide.with(getContext()).load(currentUser.getUserPhotoURL()).into(this.profilePhotoImageView);
            else Glide.with(getContext()).load(Uri).into(this.profilePhotoImageView);
        }
    }

    /**
     * when the change password button is click the onclick listener in this class is called, which inturn opens a password dialog popup for the user to create a new password.
     */
    private void handleChangePasswordBTN() {
        changePasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPasswordDialog();
            }
        });
    }

    /**
     *  when the change username button is click the onclick listener in this class is called, which inturn opens a username dialog popup for the user to assign a new username to their account.
     */
    private void handleChangeUsernameBTN() {
        changeUsernameBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUsernameDialog();
            }
        });
    }

    /**
     *  when the change email button is click the onclick listener in this class is called, which inturn opens a email dialog popup for the user to assign a new email to their account.
     */
    private void handleChangeEmailBTN() {
        changeEmailBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmailDialog();
            }
        });
    }

    /**
     * upon the save button being click the internal onclick callback is triggered, form this the current settings for the map settings are saved to the local version of the user, before being updated on the server; an appropriate message regarding what factors of the map settings where changed will be displayed to the user to confirm the change has taken place.
     */
    private void handleMapSettingsSaveChangesBTN() {
        mapSettingsSaveChangesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedColor == null && selectedUpdateRate == null) {
                    Toast.makeText(getActivity(), "No changes made",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                boolean updateColor = false;
                boolean updateRate = false;
                if (selectedColor != null) {
                    if (currentUser.getUserColor() != null && !(currentUser.getUserColor().equals(selectedColor))) {
                        updateUserColor(selectedColor);
                        updateColor = true;
                    } else if (currentUser.getUserColor() == null) {
                        updateUserColor(selectedColor);
                        updateColor = true;
                    }
                }
                if (selectedUpdateRate != null) {
                    Integer updateRateInt = updateRateToInt(selectedUpdateRate);
                    if (currentUser.getUserUpdateRate() != null && !(currentUser.getUserUpdateRate().equals(updateRateInt))) {
                        updateUserUpdateRate(updateRateToInt(selectedUpdateRate));
                        updateRate = true;
                    } else if (currentUser.getUserUpdateRate() == null) {
                        updateUserUpdateRate(updateRateToInt(selectedUpdateRate));
                        updateRate = true;
                    }
                }
                if (updateColor && updateRate) {
                    Toast.makeText(getActivity(), "Color and Update rate changes saved",
                            Toast.LENGTH_LONG).show();
                } else if (updateColor) {
                    Toast.makeText(getActivity(), "Color change saved",
                            Toast.LENGTH_LONG).show();
                } else if (updateRate) {
                    Toast.makeText(getActivity(), "Update rate change saved",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "settings already configured, no save required",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    /**
     * converts the string of the update rate from the spinner to its integer equivalent; EG. "15S" = 15
     *
     * @param updateRate String representing the selected update rate from the string array of the spinner
     * @return the integer value the string is representing
     */
    private int updateRateToInt(String updateRate) {
        return Integer.valueOf(updateRate.substring(0, updateRate.length() - 1));
    }

    /**
     * handles the callback triggered when the user clicks the add photo FAB, this calls the loadPhoto function to create an implicit intent for photo selection.
     */
    private void handleAddProfilePhotoFAB() {
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


    /**
     * opens a new username dialog and sets a listener to the dialog to be triggered upon completion
     */
    private void openUsernameDialog() {
        if (this.getActivity() != null) {
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

    /**
     * updates the current users username in the firestore database document for the current user and within the authentication layer.
     *
     * @param username String for the new username (display name) for the user
     */
    private void updateUsersUsername(String username) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
            mAuth.getCurrentUser().updateProfile(updateRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "username Updated",
                            Toast.LENGTH_LONG).show();
                    updateDateBaseUsersUsername(username);
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

    /**
     * updates the firestore document representation of the current user's usersname
     *
     * @param username String for the new username (display name) for the user.
     */
    private void updateDateBaseUsersUsername(String username) {
        currentUserDocument.getReference().update("username", username);
    }

    /**
     * opens a new email dialog and sets a listener to the dialog to be triggered upon completion
     */
    private void openEmailDialog() {
        if (this.getActivity() != null) {
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

    /**
     * because changing the authentication layers copy of the email registered to an account is a fundamental change to the account the users credentials need to be sent to author such a change, therefore to get the user's credentials then the user's creditals must be re-authenticated
     * this function generates a dialog window that will request the users credentials and then re-authenticate the user through means of setting a result listener
     * @param Email String representing the new email for the current user's account.
     */
    private void reAuthPopupEmail(final String Email) {
        if (this.getActivity() != null) {
            ReAuthUserDialog reAuthUserDialog = new ReAuthUserDialog();
            reAuthUserDialog.setTitle("Please enter your current password to confirm this change");
            reAuthUserDialog.setReAuthUserDialogListener(new ReAuthUserDialog.ReAuthUserDialogListener() {
                @Override
                public void returnResult(String Password, Boolean Authenticated) {
                    if (Authenticated) {
                        updateUsersEmailAddress(Email, Password);
                    }
                }
            });
            reAuthUserDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    /**
     * upon re-authentication success this function is called to use the resulting password to authenticate the user's action of updating their email address, this function does this update on the authentication layer.
     *
     * @param emailAddress String new email address to be updated to
     * @param Password String the password for the current user's account.
     */
    private void updateUsersEmailAddress(String emailAddress, String Password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
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
                                                updateDateBaseUsersEmailAddress(emailAddress);
                                                Toast.makeText(getContext(), "Email updated",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "Failure: email failed to update",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    /**
     * updates the email address for the user on the firestore document for the current user
     * @param emailAddress new email address to update to.
     */
    private void updateDateBaseUsersEmailAddress(String emailAddress) {
        currentUserDocument.getReference().update("userEmailAddress", emailAddress);
    }

    /**
     * opens a new password dialog and sets a listener to the dialog to be triggered upon completion
     */
    private void openPasswordDialog() {
        if (this.getActivity() != null) {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.setChangePasswordDialogListener(new ChangePasswordDialog.ChangePasswordDialogListener() {
                @Override
                public void returnResult(String Password) {
                    reAuthPopupPassword(Password);
                }
            });
            changePasswordDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    /**
     *  because changing the authentication layers password that is registered to a users account is a fundamental change to the account the users credentials need to be sent to author such a change, therefore to get the user's credentials then the user's creditals must be re-authenticated
     *  this function generates a dialog window that will request the users credentials and then re-authenticate the user through means of setting a result listener
     *
     * @param PasswordInput String representing the new password for the user.
     */
    private void reAuthPopupPassword(final String PasswordInput) {
        if (this.getActivity() != null) {
            ReAuthUserDialog reAuthUserDialog = new ReAuthUserDialog();
            reAuthUserDialog.setTitle("Please enter your current password to confirm this change");
            reAuthUserDialog.setReAuthUserDialogListener(new ReAuthUserDialog.ReAuthUserDialogListener() {
                @Override
                public void returnResult(String Password, Boolean Authenticated) {
                    if (Authenticated) {
                        updateUsersPassword(PasswordInput, Password);
                    }
                }
            });
            reAuthUserDialog.show(this.getActivity().getSupportFragmentManager(), this.TAG);
        }
    }

    /**
     * upon re-authentication success this function is called to use the resulting password to authenticate the user's action of updating their password to their newly selected password in the process, this function does this update on the authentication layer.
     *
     * @param newPassword String for the new password for the current user's account.
     * @param existingPassword string for the users existing password that has just been authenticated.
     */
    private void updateUsersPassword(String newPassword, String existingPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), existingPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated.");
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Password updated",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "Failure: Password failed to update",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        }
    }


    /**
     * creates an implicit intent for acquiring a profile photo for the user
     */
    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }


    /**
     * onRequestPermissionsResult override callback for when the os has queried the user for permission to access certain resources (gallery access in the case of this activity)
     * once permission is granted loading the photo can occur
     *
     * @param requestCode Code handed to the intent by the author of the intent used to clarify if it was our intent that has called this callback listener.
     * @param permissions the string arraylist of stacked requested permissions (this app only requests one permission at a time)
     * @param grantResults Int Array which gives the status of whether the corresponding permission was granted or denied by the user.
     */
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

    /**
     * upon creating an implicit intent to get an image resource from the users device, the intent will call an activity result which we must then check the result code for to check it was our activity that authored this request and that the request was valid and a success

     *
     * @param requestCode Code handed to the intent by the author of the intent used to clarify if it was our intent that has called this callback listener.
     * @param resultCode  an Int representing if the intent was successful.
     * @param data        the data returned by the activity (Intent object).
     */
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
                        Toast.makeText(getActivity(), "server error, new users are no possible to be created at this time",
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

    /**
     * a function which extracts the URI from an intents data (only applicable to file selection intents), this then uploads this as a users profile photo to the firebase server, by calling upload photo function.
     *
     * @param data Intent containing a URI to a file or resource location
     */
    private void handleGettingURIFromData(Intent data) {
        if (data.getData() != null && getActivity() != null) { //&& mAuth.getCurrentUser() != null) {
            ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), data.getData());
            Uri imageURI = data.getData();
            String uploadPath = "images/users/userphoto" + UUID.randomUUID().toString();
            uploadPhoto(imageURI, uploadPath);
        }
    }

    /**
     * uploadPhoto will take the image uri path and upload this to the database, this function also contains listeners for the success or failure of this transaction. upon success the function will call the get the download URI function, so that the photo can then be referenced from the server.
     *
     * @param imageURI   the URI of the Image on the local device
     * @param uploadPath the string of where we want to put the file on the server
     */
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
                    getDownloadURI(storageReference);
                }
            });
        }
    }

    /**
     * as the name suggest this function will get the download the download uri link from the server so that the resource can be access by referencing the user.
     * upon success this function will then load the download URI into the profile photo imageview through calling the loadGroupPhoto function. upon failure an error message is displayed to the user.
     *
     * @param storageReference reference to the file which we want to get the download URI for.
     */
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

    /**
     * this function updates the profile uri for the user in the authentication layer of firestore
     *
     * @param uri URi representing the Download URi for the users new photo
     */
    private void updateProfileURI(Uri uri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
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

    /**
     * this function updates the profile uri for the user in the database document representing the current user
     *
     * @param uri URi representing the Download URi for the users new photo
     */
    private void updateDatabaseProfileURI(Uri uri) {
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

    /**
     * updates the users color to the document on the firestore database document for the current user.
     *
     * @param colorToUpdateTO String representing the enum value for the color to be udpated to.
     */
    private void updateUserColor(String colorToUpdateTO) {
        currentUserDocument.getReference().update("userColor", colorToUpdateTO);
    }

    /**
     * updates the user's udate rate to the document in the firestore database document for the current user.
     *
     * @param newUpdateRate Int for the new update rate (15,30,60)
     */
    private void updateUserUpdateRate(int newUpdateRate) {
        currentUserDocument.getReference().update("userUpdateRate", newUpdateRate);
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
        if (parent.equals(this.mapSettingsMarkerColorSpinner)) {

            if (!groupColors.get(position).equals("random")) {
                selectedColor = groupColors.get(position);
                markerPreviewImageview.setColorFilter(Color.parseColor(selectedColor));
            } else {
                String tempRandomColor = GroupColors.randomColor().getStringValue();
                markerPreviewImageview.setColorFilter(Color.parseColor(tempRandomColor));
                selectedColor = tempRandomColor;
            }
        } else if (parent.equals(this.mapSettingsMapUpdateDuration)) {
            selectedUpdateRate = updateRates.get(position);
        }
    }

    /**
     * default override called to satisfy implemented methods, this activity does not take action upon nothing selected. (this function is empty)
     * @param parent the parent view for the spinner that called this callback.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}