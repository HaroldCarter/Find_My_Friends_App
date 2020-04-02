package com.example.find_my_friends;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * an example class to register users to a firebase database, this should be separated out into different classes, so that the code can be reused for the settings page later in the development
 * consider revising the massive if else tree at the bottom of the class, replace with a switch case to improve readabillity.
 *
 * if a photo uploaded is to large then it will crash the application, this is an inherent issue with the bitmap datatype itself; this cannot be solved without manually checking each time the image is set.
 * requires fixing.
 *
 *
 * bundling the data is requried, so that a screen rotation change doesn't delete all the on screen data.
 *
 */
public class RegisterActivity extends AppCompatActivity {
    EditText rUsername, rEmail, rPassword, rConfirmPassword, rConfirmEmail;
    FirebaseAuth mAuth;
    StorageReference mStorageRef;


    ImageView rProfilePhoto;

    ProgressBar rProgressBar;
    static final int RESULT_LOADED_IMAGE = 1;
    static final int REQUEST_GALLERY_ACCESS = 1000;
    static final String TAG = "Register Activity : ";
    Bitmap profilePhotoBitmap = null;
    Activity contextOfApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rProfilePhoto = (ImageView) findViewById(R.id.UserProfilePhotoReg);
        rUsername = (EditText) findViewById(R.id.UsernameTextFieldReg);
        rEmail = (EditText) findViewById(R.id.emailRelativeTextFieldReg);
        rPassword = (EditText) findViewById(R.id.passwordTextFieldReg);
        rConfirmPassword = (EditText) findViewById(R.id.confirmPasswordTextFieldReg);
        rConfirmEmail = (EditText) findViewById(R.id.confirmEmailRelativeTextFieldReg);
        rProgressBar = (ProgressBar) findViewById(R.id.progressBarReg);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        contextOfApp = this;
        configureBackButton();
        configureRegButton();
        configureAddPhotoButton();

    }

    private void loadPhoto(){
        Intent mediaSelectionIntent = new Intent(Intent.ACTION_PICK);
        mediaSelectionIntent.setType("image/*");
        startActivityForResult(mediaSelectionIntent, RESULT_LOADED_IMAGE);
    }

    private void configureAddPhotoButton(){
        FloatingActionButton addPhotoBTN = (FloatingActionButton) findViewById(R.id.addUsersPhoto);
        addPhotoBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //if running newer than marsh then permissions need to be requested, currently set to minimum API 28 M is 23 but liable to change.
                    if(ContextCompat.checkSelfPermission(contextOfApp, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                        if (ActivityCompat.shouldShowRequestPermissionRationale(contextOfApp,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Toast.makeText(RegisterActivity.this, "Permission is required to load profile photos",
                                    Toast.LENGTH_SHORT).show();
                        }

                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_ACCESS);
                    } else {
                        //permission was already granted, therefore just launch the intent.
                        loadPhoto();
                    }
                } else {
                    //permission is not required because the application is over Marshmellow (when premission granting was introduced).
                    loadPhoto();
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //switch case statement may be needed to be expanded in future, bad practice to do this with if.
        switch (requestCode){
            case REQUEST_GALLERY_ACCESS: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(RegisterActivity.this, "Permission to storage granted",
                            Toast.LENGTH_SHORT).show();
                    loadPhoto();
                } else {
                    //permission denied, cancel the functionality of loading the image.
                    Toast.makeText(RegisterActivity.this, "Permission was denied",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOADED_IMAGE){
            if(data.getData() != null) {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
                try {
                    profilePhotoBitmap = ImageDecoder.decodeBitmap(source);
                    rProfilePhoto.setImageBitmap(profilePhotoBitmap);
                } catch (IOException e) {
                    Toast.makeText(RegisterActivity.this, "error when decoding image, use JPEG or PNG",
                            Toast.LENGTH_LONG).show();
                    profilePhotoBitmap = null;

                }
            }else{
                Toast.makeText(RegisterActivity.this, "path to image is corrupt, or no path no longer exists",
                        Toast.LENGTH_LONG).show();
                profilePhotoBitmap = null;
            }
        }
    }

    private void configureBackButton(){
        FloatingActionButton backBTN = (FloatingActionButton) findViewById(R.id.backButton);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void uploadImage(Bitmap profileBitmap){
        if (mAuth.getCurrentUser() != null) {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            //computationally expensive but done on request not looping.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            mStorageRef =  mStorageRef.child("Images")
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
        }else{
            Toast.makeText(RegisterActivity.this, "User is not Authenticated at time of uploading profile photo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getImageUri(){
        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(RegisterActivity.this, uri.toString(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, uri.toString());
                setUserProfileUri(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(mAuth.getCurrentUser() != null) {
                    Log.e(TAG, "Failed to locate resource index for profile photo of user " + mAuth.getCurrentUser().getUid());
                }else{
                    Log.e(TAG, "Failed to locate resource index user because user reference is null");
                }
            }
        });
    }

    private void setUserProfileUri(Uri profilePhotoUri){
        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder().setPhotoUri(profilePhotoUri).build();
        mAuth.getCurrentUser().updateProfile(updateRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "profile Photo linked to user",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "new profile Photo Failed to link to user",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //function needs revising, left in current state till functionaltiy is completed, then refactor.
    private void configureRegButton() {
        if (mAuth != null) {
            Button regBTN = (Button) findViewById(R.id.registerUserButton);
            regBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rProgressBar.setVisibility(View.VISIBLE);
                    String email = rEmail.getText().toString();
                    String username = rUsername.getText().toString();
                    String password = rPassword.getText().toString();
                    String confirmPassword = rConfirmPassword.getText().toString();
                    String confirmEmail = rConfirmEmail.getText().toString();
                    if (TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        //checks the email is a valid address
                        rEmail.setError("please enter a valid email address");
                    }
                    if (profilePhotoBitmap != null) {
                        if (email.equals(confirmEmail)) {
                            if (password.equals(confirmPassword) && !TextUtils.isEmpty(password)) {
                                //if the passwords match and are not empty
                                if (password.length() > 5) {
                                    //the password and email both meet requirements, therefore register the user.


                                    mAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(RegisterActivity.this, "Authentication Succeeded.",
                                                                Toast.LENGTH_SHORT).show();
                                                        uploadImage(profilePhotoBitmap);
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                } else {
                                    //checks if the password is of correct length.
                                    Toast.makeText(RegisterActivity.this, "Password needs to be longer than 5 characters",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //display a toast popup saying the passwords do not match.
                                Toast.makeText(RegisterActivity.this, "Passwords Do not match consider revising.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Emails do not match, consider revising",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(RegisterActivity.this, "Profile photo is required",
                                Toast.LENGTH_SHORT).show();
                    }
                    rProgressBar.setVisibility(View.INVISIBLE);
                }
            });


        }
    }
}