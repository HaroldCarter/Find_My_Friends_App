package com.example.find_my_friends;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.userUtil.User;
import com.example.find_my_friends.util.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.find_my_friends.util.Constants.CurrentUserLoaded;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;
import static com.example.find_my_friends.util.Constants.RESULT_LOADED_IMAGE;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;
import static com.example.find_my_friends.util.Constants.currentUserFirebase;

import java.util.UUID;

/**
 * The class responsible for the register activity, in which a user can sign up to the application, set a profile photo, user name and email.
 *
 * @author Harold Carter
 * @version 4.0
 */
public class RegisterActivity extends AppCompatActivity {
    EditText rUsername, rEmail, rPassword, rConfirmPassword, rConfirmEmail;
    FirebaseAuth mAuth;
    StorageReference mStorageRef;
    FirebaseFirestore db;
    Button regBTN;

    ImageView rProfilePhoto;
    FloatingActionButton addPhotoBTN;
    FloatingActionButton backBTN;

    ProgressBar rProgressBar;
    ProgressBar progressBarProfilePhoto;

    static final String TAG = "Register Activity : ";
    Bitmap profilePhotoBitmap = null;
    Activity contextOfApp;
    private Uri photoURI;
    private boolean uploadStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        regBTN = findViewById(R.id.registerUserButton);
        rProfilePhoto = findViewById(R.id.UserProfilePhotoReg);
        rUsername = findViewById(R.id.UsernameTextFieldReg);
        rEmail = findViewById(R.id.emailRelativeTextFieldReg);
        rPassword = findViewById(R.id.passwordTextFieldReg);
        rConfirmPassword = findViewById(R.id.confirmPasswordTextFieldReg);
        rConfirmEmail = findViewById(R.id.confirmEmailRelativeTextFieldReg);
        rProgressBar = findViewById(R.id.progressBarReg);
        progressBarProfilePhoto = findViewById(R.id.progressBarUserProfilePhoto);
        addPhotoBTN = findViewById(R.id.addUsersPhoto);
        mAuth = FirebaseAuth.getInstance();
        backBTN = findViewById(R.id.backButton);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        contextOfApp = this;
        configureBackButton();
        configureRegButton();
        configureAddPhotoButton();

    }

    /**
     * creates an implicit intent for acquiring a profile photo for the user, called upon the user clicking the FAB to add a new photo.
     */
    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }

    /**
     * handles the callback triggered when the user clicks the add photo FAB, this calls the loadPhoto function to create an implicit intent for photo selection.
     */
    private void configureAddPhotoButton() {
        addPhotoBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadStatus = false;
                progressBarProfilePhoto.setVisibility(View.VISIBLE);
                PermissionUtils.requestReadExternalPermission(RegisterActivity.this);
                if (PermissionUtils.checkReadExternalPermission(RegisterActivity.this)) {
                    loadPhoto();
                }

            }
        });
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
                    Toast.makeText(RegisterActivity.this, "Permission to storage granted",
                            Toast.LENGTH_SHORT).show();
                    loadPhoto();
                } else {
                    Toast.makeText(RegisterActivity.this, "Permission was denied",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * a function which extracts the URI from an intents data (only applicable to file selection intents), this then uploads this as a users profile photo to the firebase server, by calling upload photo function.
     *
     * @param data Intent containing a URI to a file or resource location
     */
    private void handleGettingURIFromData(Intent data) {
        if (data.getData() != null) { //&& mAuth.getCurrentUser() != null) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
            Uri imageURI = data.getData();
            String uploadPath = "images/users/userphoto" + UUID.randomUUID().toString();
            uploadPhoto(imageURI, uploadPath);
        }
    }

    /**
     * upon creating an implicit intent to get an image resource from the users device, the intent will call an activity result which we must then check the result code for to check it was our activity that authored this request and that the request was valid and a success
     * after this check has been completed, the photo's URI is then extracted using the handlegettingURIfromdata function, if the task is not successful then then this function will display a toast message explaining the error to the user.
     *
     * @param requestCode Code handed to the intent by the author of the intent used to clarify if it was our intent that has called this callback listener.
     * @param resultCode  an Int representing if the intent was successful.
     * @param data        the data returned by the activity (Intent object).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        Toast.makeText(RegisterActivity.this, "server error, new users are no possible to be created at this time",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                handleGettingURIFromData(data);
            }


        } else {
            Toast.makeText(RegisterActivity.this, "path to image is corrupt, or no path no longer exists",
                    Toast.LENGTH_LONG).show();
            profilePhotoBitmap = null;
        }
    }

    /**
     * hanldes the user clicking on the back button by closing this activity.
     */
    private void configureBackButton() {
        mAuth.signOut();
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    Toast.makeText(RegisterActivity.this, e.toString(),
                            Toast.LENGTH_LONG).show();
                    progressBarProfilePhoto.setVisibility(View.INVISIBLE);
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
                        Toast.makeText(RegisterActivity.this, "Photo uploaded",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, uri.toString());
                        photoURI = uri;
                        loadGroupPhoto();
                        uploadStatus = true;
                        progressBarProfilePhoto.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Linking the selected photo failed, photo did not upload correctly (connection interrupted)",
                                Toast.LENGTH_LONG).show();
                        progressBarProfilePhoto.setVisibility(View.INVISIBLE);
                    }

                });
    }

    /**
     * uses glide to load the download uri into the imageview, if the local variable for the photoURI is not null
     */
    private void loadGroupPhoto() {
        if (photoURI != null) {
            Glide.with(this).load(photoURI).into(rProfilePhoto);

        }
    }


    /**
     * upon the user clicking the register button this function is called to check if the entered data is valid input for the creation of a new user, all appropriate warning and info settings are then returned to the ui based of the validity of their entry within this function, the function will also return if the users input was valid as a boolean.
     *
     * @return a boolean representing if the users input was valid or not.
     */
    private boolean checkUserData() {
        rProgressBar.setVisibility(View.VISIBLE);
        boolean validData = true;
        //check that the photo is present.
        if (!uploadStatus) {
            Snackbar.make(regBTN, "please upload a profile photo or wait for the selected to upload", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            validData = false;
        }

        //checks the username isn't empty
        if (TextUtils.isEmpty(rUsername.getText())) {
            rUsername.setError("please enter a username");
            validData = false;
        }

        //checks that the passwords match.
        if ((!rPassword.getText().toString().equals(rConfirmPassword.getText().toString()))) {
            rConfirmPassword.setError("please check your password match");
            validData = false;
        }

        //checks the password is of an appropriate length
        if (rPassword.getText().toString().length() < 5) {
            rPassword.setError("please make sure your password is over 5 characters");
            validData = false;
        }

        //checks that the email is a populated and that it matches the email pattern matcher
        if ((TextUtils.isEmpty(rEmail.getText().toString())) || (!Patterns.EMAIL_ADDRESS.matcher(rEmail.getText().toString()).matches())) {
            rEmail.setError("please enter a valid email address");
            validData = false;
        }

        //checks that the emails match
        if (!rEmail.getText().toString().equals(rConfirmEmail.getText().toString())) {
            rConfirmEmail.setError("please check your emails match");
            validData = false;
        }


        return validData;
    }


    /**
     * within this function, so long as the authentication layer of the application is not null (so long as firebase has initialized) then a listener callback is applied to the register button, upon the user clicking this button, the validity of input is checked and then the user is signed up to authentication layer of the firebase database server.
     */
    private void configureRegButton() {
        if (mAuth != null) {
            regBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rProgressBar.setVisibility(View.VISIBLE);
                    if (checkUserData()) {
                        signUpUser(rEmail.getText().toString(), rPassword.getText().toString(), rUsername.getText().toString());
                    } else {
                        rProgressBar.setVisibility(View.INVISIBLE);
                    }


                }
            });
        }
    }

    /**
     * within this function the authentication layer of the database is requested to append a new member to the list of users through registering their basic information, upon failure (user already registered) a message will be displayed to the user, else on success the function will then update the user info through calling the function, updateUserInfo():
     *
     * @param email    email the user wishes to signup with (String)
     * @param password password the user wishes to sighup with (String)
     * @param username Username the user wishes to sighup with (String)
     */
    private void signUpUser(final String email, final String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                            Toast.makeText(RegisterActivity.this, "Authentication Succeeded.",
                                    Toast.LENGTH_SHORT).show();

                            updateUserInfo(username);

                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * hides the progress bar and will load the current users into the constants (so reference doesn't need to be constantly passed) and then finish this current activity.
     */
    private void launchMainActivity() {
        rProgressBar.setVisibility(View.INVISIBLE);
        loadCurrentUser();
        finish();
    }

    /**
     * updates the user information to append the users uri to their profile photo
     *
     * @param username takes the users Username (their email) as a parameter from which it will request to update their profile on the auth layer.
     */
    private void updateUserInfo(final String username) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) { //success on updating user profile
                                Toast.makeText(RegisterActivity.this, "Registration Successful",
                                        Toast.LENGTH_SHORT).show();
                                //upload the profile photo
                                setUserProfileUri(photoURI);
                            } else { //failed on updating user profile
                                Toast.makeText(RegisterActivity.this, "Setting Username Failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            rProgressBar.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * upon the user info being acquired from the database a userprofilerequest is made in which this function sets the URI of the users account to that uploaded to the database (the download URI) the data is then stored onto the firestore database through the storeUserInforOnFireStore function if the request is a sucess, if the request is a failure an appropriate message is displayed to the user.
     *
     * @param profilePhotoUri
     */
    private void setUserProfileUri(Uri profilePhotoUri) {
        //update the profile to contain the profile photo
        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setPhotoUri(profilePhotoUri).build();
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().updateProfile(updateRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(RegisterActivity.this, "profile Photo linked to user",
                            Toast.LENGTH_LONG).show();
                    storeUserInfoOnFireStore(mAuth.getCurrentUser());
                    launchMainActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "new profile Photo Failed to link to user + " + e.toString());
                    Toast.makeText(RegisterActivity.this, "new profile Photo Failed to link to user",
                            Toast.LENGTH_LONG).show();
                }
            });

        }
    }


    /**
     * upon successful authentication of the user, the users data is then appended to the firestore database, the authentication layer cannot be user to store information about the user and is simply just for authentication (storing FireabaseUsers is mal-practice as it leave apps open to malicious attacks)
     * this is then
     *
     * @param firebaseUser
     */
    private void storeUserInfoOnFireStore(FirebaseUser firebaseUser) {
        User user = new User();
        user.setUID(firebaseUser.getUid());
        user.setUserEmailAddress(firebaseUser.getEmail());
        if (firebaseUser.getPhotoUrl() != null) {
            user.setUserPhotoURL(firebaseUser.getPhotoUrl().toString());
        }
        user.setUsername(firebaseUser.getDisplayName());
        db.collection("Users").document(firebaseUser.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "profile uploaded to fireStore successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing profile to the fireStore", e);
                    }
                });


    }


    /**
     * will request the current users database collection and then load this into the constant current user object then launch the main activity (authentication success)
     */
    private void loadCurrentUser() {
        currentUserFirebase = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Users").document(currentUserFirebase.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUserDocument = task.getResult();
                if (currentUserDocument != null) {
                    currentUser = currentUserDocument.toObject(User.class);
                    CurrentUserLoaded = true;
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
    }
}


