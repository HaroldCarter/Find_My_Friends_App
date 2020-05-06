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
 * an example class to register users to a firebase database, this should be separated out into different classes, so that the code can be reused for the settings page later in the development
 * consider revising the massive if else tree at the bottom of the class, replace with a switch case to improve readabillity.
 * <p>
 * if a photo uploaded is to large then it will crash the application, this is an inherent issue with the bitmap datatype itself; this cannot be solved without manually checking each time the image is set.
 * requires fixing.
 * <p>
 * <p>
 * bundling the data is requried, so that a screen rotation change doesn't delete all the on screen data.
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

    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaSelectionIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        mediaSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }

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


    private void handleGettingURIFromData(Intent data) {
        if (data.getData() != null) { //&& mAuth.getCurrentUser() != null) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
            Uri imageURI = data.getData();
            String uploadPath = "images/users/userphoto" + UUID.randomUUID().toString();
            uploadPhoto(imageURI, uploadPath);
        }
    }

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
                        Toast.makeText(RegisterActivity.this,  "server error, new users are no possible to be created at this time",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                //the user is already logged in, this could be annon or still logged in somehow, we cannot assume ether so must allow them to attempt to continue making an accoutn
                //later lines catch this error and tell the user they're trying to sign up with an account already in use.
                handleGettingURIFromData(data);
            }


        } else {
            Toast.makeText(RegisterActivity.this, "path to image is corrupt, or no path no longer exists",
                    Toast.LENGTH_LONG).show();
            profilePhotoBitmap = null;
        }
    }


    private void configureBackButton() {
        mAuth.signOut();
        //incase they are anon signed in, otherwise when they load the page it will automatically sign them in to a non-existant account (allows for rogue groups to be made).
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

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

    private void loadGroupPhoto() {
        if (photoURI != null) {
            Glide.with(this).load(photoURI).into(rProfilePhoto);

        }
    }


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

    //function needs revising, left in current state till functionaltiy is completed, then refactor.
    private void configureRegButton() {
        //found that if you clicked quick enough  this function could somehow be toggled before mAuth was set up, strange error that isn't logical from the perspective of my code, assuming its an async task.
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

    private void launchMainActivity() {
        rProgressBar.setVisibility(View.INVISIBLE);
        loadCurrentUser();
        finish();
    }

    private void updateUserInfo(final String username) {
        //set the username in the authentication panel.
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        //apply the update.
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
            //registering failed the user should be deleted, however this is not possible and should only be added once the delete function has been written.
        }
    }


    private void storeUserInfoOnFireStore(FirebaseUser firebaseUser) {
        //asimilated this data into a user object.
        User user = new User();
        user.setUID(firebaseUser.getUid());
        user.setUserEmailAddress(firebaseUser.getEmail());
        //this should never occur but just to catch the null anyway.
        if(firebaseUser.getPhotoUrl() != null) {
            user.setUserPhotoURL(firebaseUser.getPhotoUrl().toString());
        }
        user.setUsername(firebaseUser.getDisplayName());

        //upload this as a document to the firestore database.
        //experiencing issues with this function not actually uploading the user to the documents. however this function has not been changed.
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




    private void loadCurrentUser(){
        currentUserFirebase = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Users").document(currentUserFirebase.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentUserDocument = task.getResult();
                if(currentUserDocument != null) {
                    currentUser = currentUserDocument.toObject(User.class);
                    CurrentUserLoaded = true;
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
    }
}


