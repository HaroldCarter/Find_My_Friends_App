package com.example.find_my_friends;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.find_my_friends.ui.dialog_windows.ChangeEmailDialog;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.find_my_friends.util.Constants.CurrentUserLoaded;
import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;
import static com.example.find_my_friends.util.Constants.currentUserFirebase;


/**
 * fairly well done, just need some cleanup
 * needs to use bundles so that data is not lost upon screen rotation
 *
 * need to implement password recovery (possibly another screen, but should be very easy to implement, or just change the visability of the assets on this screen).
 */
public class LoginActivity extends AppCompatActivity {
    EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private String password;
    private boolean validInput = true;
    private ProgressBar progressBar;

    private final String TAG = "Login Activity : ";


    private Button forgotPasswordBTN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarLoginPage);
        forgotPasswordBTN = findViewById(R.id.forgotPasswordBTN);



        if(mAuth.getCurrentUser() != null){
            progressBar.setVisibility(View.VISIBLE);
            loadCurrentUser();
        }


        mEmail =  findViewById(R.id.loginUsernameTextField);
        mPassword =  findViewById(R.id.loginPasswordTextField);

        configureRegisterButton();
        configureLoginButton();
        handleForgotPasswordBTN();

    }


    private void handleForgotPasswordBTN(){
        forgotPasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordDialog();
            }
        });
    }



    private void openForgotPasswordDialog(){
        ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
        changeEmailDialog.setTitleText("Forgot your Password? Please enter your email to reset it");
        changeEmailDialog.setChangeEmailDialogListener(new ChangeEmailDialog.ChangeEmailDialogListener() {
            @Override
            public void returnResult(String Email) {
                mAuth.sendPasswordResetEmail(Email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "a reset link has been sent to your inbox",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(LoginActivity.this, "this email is not registered",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        changeEmailDialog.show(LoginActivity.this.getSupportFragmentManager(), this.TAG);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", mEmail.getText().toString());
        outState.putString("password", mPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String username = (String)savedInstanceState.get("username");
        mEmail.setText(username);
        String password = (String)savedInstanceState.get("password");
        mPassword.setText(password);
    }

    public void configureRegisterButton() {
        Button regButton = findViewById(R.id.registerButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void configureLoginButton(){
        Button loginBTN = findViewById(R.id.logInButton);
        if(mAuth != null) {
            loginBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = mEmail.getText().toString();
                    password = mPassword.getText().toString();
                    validInput = true;
                    checkValidInput();
                }
            });
        }
        else{
            Toast.makeText(LoginActivity.this, "User Already Logged in.",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            loadCurrentUser();
        }
    }

    private void checkValidInput(){
        if(TextUtils.isEmpty(email) ||  !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //prompt to enter a valid email
            mEmail.setError("please enter a valid email");
            validInput = false;
        }
        if(password.length() < 5) {
            //prompt to enter a valid password
            mPassword.setError("please enter a valid password");
            validInput = false;
        }
        if(validInput){
            signUserIn();
        }
    }

    private void signUserIn(){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication Succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.VISIBLE);
                            loadCurrentUser();

                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
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
                    progressBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
    }
}
