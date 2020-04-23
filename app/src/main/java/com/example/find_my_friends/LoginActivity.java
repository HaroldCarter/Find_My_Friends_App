package com.example.find_my_friends;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;


/**
 * fairly well done, just need some cleanup
 * needs to use bundles so that data is not lost upon screen rotation
 *
 * need to implement password recovery (possibly another screen, but should be very easy to implement, or just change the visability of the assets on this screen).
 */
public class LoginActivity extends AppCompatActivity {
    EditText mEmail, mPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        configureRegisterButton();
        configureLoginButton();


    }

    public void configureRegisterButton() {
        Button regButton = (Button) findViewById(R.id.registerButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void configureLoginButton(){
        Button loginBTN = (Button) findViewById(R.id.logInButton);
        mEmail = (EditText) findViewById(R.id.loginUsernameTextField);
        mPassword = (EditText) findViewById(R.id.loginPasswordTextField);




        if(mAuth != null) {


            loginBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();

                    boolean validInput = true;



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

                    if(!validInput){
                        return;
                    }
                   

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Authentication Succeeded.",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }
        else{
            Toast.makeText(LoginActivity.this, "User Already Logged in.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        //if not then we are already logged in.
    }
}
