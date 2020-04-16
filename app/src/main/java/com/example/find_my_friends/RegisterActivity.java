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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;
import static com.example.find_my_friends.util.Constants.RESULT_LOADED_IMAGE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    static final String TAG = "Register Activity : ";
    Bitmap profilePhotoBitmap = null;
    Activity contextOfApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        regBTN = findViewById(R.id.registerUserButton);
        rProfilePhoto =  findViewById(R.id.UserProfilePhotoReg);
        rUsername =  findViewById(R.id.UsernameTextFieldReg);
        rEmail =  findViewById(R.id.emailRelativeTextFieldReg);
        rPassword =  findViewById(R.id.passwordTextFieldReg);
        rConfirmPassword =  findViewById(R.id.confirmPasswordTextFieldReg);
        rConfirmEmail =  findViewById(R.id.confirmEmailRelativeTextFieldReg);
        rProgressBar =  findViewById(R.id.progressBarReg);
        addPhotoBTN =  findViewById(R.id.addUsersPhoto);
        mAuth = FirebaseAuth.getInstance();
        backBTN =  findViewById(R.id.backButton);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        contextOfApp = this;
        configureBackButton();
        configureRegButton();
        configureAddPhotoButton();

    }

    private void loadPhoto() {
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_PICK);
        mediaSelectionIntent.setType("image/*");
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }

    private void configureAddPhotoButton() {
        final Activity activity = this;

        addPhotoBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    PermissionUtils.requestReadExternalPermission(activity);
                    if(PermissionUtils.checkReadExternalPermission(activity)) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOADED_IMAGE) {
            if (data.getData() != null) {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
                try {
                    profilePhotoBitmap = ImageDecoder.decodeBitmap(source);
                    rProfilePhoto.setImageBitmap(profilePhotoBitmap);
                } catch (IOException e) {
                    Toast.makeText(RegisterActivity.this, "error when decoding image, use JPEG or PNG",
                            Toast.LENGTH_LONG).show();
                    profilePhotoBitmap = null;
                }
            } else {
                Toast.makeText(RegisterActivity.this, "path to image is corrupt, or no path no longer exists",
                        Toast.LENGTH_LONG).show();
                profilePhotoBitmap = null;
            }
        }
    }

    private void configureBackButton() {
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void uploadImage(Bitmap profileBitmap) {
        if (mAuth.getCurrentUser() != null) {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            //computationally expensive but done on request not looping.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            mStorageRef = mStorageRef.child("Images")
                    .child(mAuth.getUid() + ".jpeg");
            mStorageRef.putBytes(byteArrayOutputStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    getImageUri();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e.getCause());
                    Toast.makeText(RegisterActivity.this, e.toString(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(RegisterActivity.this, "User is not Authenticated at time of uploading profile photo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getImageUri() {

        mStorageRef.getDownloadUrl()
        .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(RegisterActivity.this, uri.toString(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, uri.toString());
                setUserProfileUri(uri);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mAuth.getCurrentUser() != null) {
                    Log.e(TAG, "Failed to locate resource index for profile photo of user " + mAuth.getCurrentUser().getUid());
                } else {
                    Log.e(TAG, "Failed to locate resource index user because user reference is null");
                }
            }
        });
    }

    private void setUserProfileUri(Uri profilePhotoUri) {
        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setPhotoUri(profilePhotoUri).build();
        if(mAuth.getCurrentUser() != null) {
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


    private boolean checkUserData(){
        rProgressBar.setVisibility(View.VISIBLE);
        boolean validData = true;


        //check that the photo is present.
        if(profilePhotoBitmap == null){
            Snackbar.make(regBTN, "please upload a profile photo", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            validData =  false;
        }

        //checks the username isn't empty
        if(TextUtils.isEmpty(rUsername.getText())){
            rUsername.setError("please enter a username");
            validData =  false;
        }

        //checks that the passwords match.
        if ((!rPassword.getText().toString().equals(rConfirmPassword.getText().toString()))){
            rConfirmPassword.setError("please check your password match");
            validData =  false;
        }

        //checks the password is of an appropriate length
        if(rPassword.getText().toString().length() < 5){
            rPassword.setError("please make sure your password is over 5 characters");
            validData =  false;
        }

        //checks that the email is a populated and that it matches the email pattern matcher
        if ((TextUtils.isEmpty(rEmail.getText().toString())) ||  (!Patterns.EMAIL_ADDRESS.matcher(rEmail.getText().toString()).matches())) {
            rEmail.setError("please enter a valid email address");
            validData =  false;
        }

        //checks that the emails match
        if (!rEmail.getText().toString().equals(rConfirmEmail.getText().toString())){
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
                    if(checkUserData()){
                        signUpUser(rEmail.getText().toString() , rPassword.getText().toString(),rUsername.getText().toString());
                    }else{
                        rProgressBar.setVisibility(View.INVISIBLE);
                    }


                }
            });
        }
    }

    private void signUpUser(final String email, final String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && mAuth.getCurrentUser()!= null) {
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

    private void launchMainActivity(){
        rProgressBar.setVisibility(View.INVISIBLE);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void updateUserInfo(final String username){
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        if(mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) { //success on updating user profile
                                Toast.makeText(RegisterActivity.this, "Registration Successful",
                                        Toast.LENGTH_SHORT).show();
                                //upload the profile photo
                                uploadImage(profilePhotoBitmap);
                            } else { //failed on updating user profile
                                Toast.makeText(RegisterActivity.this, "Setting Username Failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            rProgressBar.setVisibility(View.INVISIBLE);
            //registering failed the user should be deleted, however this is not possible and should only be added once the delete function has been written.
        }
    }


    private void storeUserInfoOnFireStore(FirebaseUser firebaseUser){
            Map<String, Object> user = new HashMap<>();
            user.put("UID", firebaseUser.getUid());
            user.put("UserEmailAddress", firebaseUser.getEmail());
            user.put("UserPhotoURL", firebaseUser.getPhotoUrl().toString());
            user.put("Username", firebaseUser.getDisplayName());

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
}


