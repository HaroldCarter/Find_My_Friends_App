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
import com.google.android.gms.tasks.OnFailureListener;
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
 * The Login activity which is responsible for the log in screen, allowing users to authenticate the Fireabase Database.
 *
 * @author Harold Carter
 * @version v5.0
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


    /**
     * overrides the default oncreate function to get reference to all onscreen variables and set their onclick listeners and therefore handlers, also loads data in from the bundle if the data exists.
     *
     * @param savedInstanceState the bundle to load the existing data from
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarLoginPage);
        forgotPasswordBTN = findViewById(R.id.forgotPasswordBTN);

        if (mAuth.getCurrentUser() != null) {
            progressBar.setVisibility(View.VISIBLE);
            loadCurrentUser();
        }

        mEmail = findViewById(R.id.loginUsernameTextField);
        mPassword = findViewById(R.id.loginPasswordTextField);

        if (savedInstanceState != null) {
            String usernameSaved = (String) savedInstanceState.get("username");
            if (usernameSaved != null) {
                mEmail.setText(usernameSaved);
            }

            String passwordSaved = (String) savedInstanceState.get("password");
            if (passwordSaved != null) {
                mPassword.setText(passwordSaved);
            }
        }
        configureRegisterButton();
        configureLoginButton();
        handleForgotPasswordBTN();

    }


    /**
     * adds an onclick listener which is called when the user interacts with the forgot password button, this function then creates a popup dialog using the open forgot password dialog function.
     */
    private void handleForgotPasswordBTN() {
        forgotPasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordDialog();
            }
        });
    }


    /**
     * creates a changeemaildialog but edits the text to be displayed, as this just confirms the users email; upon the user entering information a request to send a password reset link is sent to the email address given. (won't be sent if not registered).
     */
    private void openForgotPasswordDialog() {
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
                                } else {
                                    Toast.makeText(LoginActivity.this, "this email is not registered",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        changeEmailDialog.show(LoginActivity.this.getSupportFragmentManager(), this.TAG);

    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * saves the onscreen textview data into a bundle so that data is not lost upon screen rotation and retained.
     *
     * @param outState Bundle containing all onscreen variable's state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", mEmail.getText().toString());
        outState.putString("password", mPassword.getText().toString());
    }

    /**
     * upon restoring instance, this will set the onscreen variables to equal that in the saved bundle
     *
     * @param savedInstanceState bundle to load data from.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String username = (String) savedInstanceState.get("username");
        mEmail.setText(username);
        String password = (String) savedInstanceState.get("password");
        mPassword.setText(password);
    }

    /**
     * upon the user clicking the register button the callback contained within this function is called and this starts the register activity
     */
    public void configureRegisterButton() {
        Button regButton = findViewById(R.id.registerButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    /**
     * upon the user clicking the login button the callback contained within this function is called and this checks the validity of the entered credentials, then if the user is already valid and therefore logged in then the button becomes non interactive (no click callback is registered) and the main activity is loaded.
     */
    private void configureLoginButton() {
        Button loginBTN = findViewById(R.id.logInButton);
        if (mAuth != null) {
            loginBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = mEmail.getText().toString();
                    password = mPassword.getText().toString();
                    validInput = true;
                    checkValidInput();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, "User Already Logged in.",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            loadCurrentUser();
        }
    }

    /**
     * checks if the validity of the users credentials matches the requirements, if so then the function signUserIn is called
     */
    private void checkValidInput() {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //prompt to enter a valid email
            mEmail.setError("please enter a valid email");
            validInput = false;
        }
        if (password.length() < 5) {
            //prompt to enter a valid password
            mPassword.setError("please enter a valid password");
            validInput = false;
        }
        if (validInput) {
            signUserIn();
        }
    }

    /**
     * using firebase authentication a request to authenticate the provided credentials is made, if the sign in is a success then a message is displayed conveying this, and the user is loaded into the application; else a purposefully vague message is displayed claiming that authentication failed, this is vague to stop brute force from being further refined ( its not revealed if the password or username are incorrect)
     */
    private void signUserIn() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

    /**
     * the users document linked to the uid of the current user is loaded into the applications constants, this is so reference doesn't need to be constantly maintained; this is then used by the rest of the activity to access the data contained on the user in the database, the main activity is started upon success, (this can't fail unless the database has an error)
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
                    progressBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "User Documents Deleted, Therefore cannot login please contact system admin",
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
