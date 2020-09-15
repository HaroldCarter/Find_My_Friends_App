package com.example.find_my_friends.ui.dialog_windows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.find_my_friends.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A custom dialogue which reauthenticates the user before returning to the main application, used for changes that require the user to confirm their authentication before Firestore permits the modifications.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class ReAuthUserDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button confrimBTN;
    private Button denyBTN;
    private String titleText;
    private AlertDialog dialog;
    

    private ReAuthUserDialogListener reAuthUserDialogListener;

    /**
     * Called Override to build a custom dialog container which displays two text boxes and checks that the their contents matches.
     * used for gathering the users email and password combination and authenticating that the user has entered the correct details.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.password_popup, null);
        builder.setView(view);
        passwordEditText = view.findViewById(R.id.popup_window_password_editText);
        confirmPasswordEditText = view.findViewById(R.id.popup_window_confirm_password_EditText);
        confrimBTN = view.findViewById(R.id.popup_password_confirmBTN);
        denyBTN = view.findViewById(R.id.popup_password_denyBTN);
        popupTitleTextView = view.findViewById(R.id.popup_window_password_title);
        popupTitleTextView.setText(titleText);
        dialog = builder.create();
        confrimBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserAuthentication();
            }
        });
        denyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }


    /**
     * set the title text of the custom dialog window
     * @param titleText String to be inputted to the title text view and be displayed as the title of the custom dialog window
     */
    public void setTitle(String titleText) {
        this.titleText = titleText;
        if (popupTitleTextView != null) {
            popupTitleTextView.setText(titleText);
        }
    }


    /**
     * checks the authentication of the user and if the user is authenticated (the correct credientials, the callback listener is called and given password edit text contents (inorder to re-authenticate to request a crucial change the firebase authentication layer regarding whatever change this re-auth dialog was called to authenticate the user for (typically changing the email address requires re-authentication))
     */
    private void checkUserAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if ((!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()))) {
            confirmPasswordEditText.setError("please check your password match");
            return;
        }
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), passwordEditText.getText().toString()); // Current Login Credentials \\
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            reAuthUserDialogListener.returnResult(passwordEditText.getText().toString(), true);
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Authentication Failed",
                            Toast.LENGTH_SHORT).show();
                    passwordEditText.setError("please check your password match");
                }
            });
        }
    }


    /**
     * set the internal variable for the instance of the ReAuthUserDialogListener interface to the instance passed by reference as a parameter of the function, this is to be triggered once the dialog window has successfully completed.
     * @param reAuthUserDialogListener instance of Listener to set the internal listener equal to.
     */
    public void setReAuthUserDialogListener(ReAuthUserDialogListener reAuthUserDialogListener) {
        this.reAuthUserDialogListener = reAuthUserDialogListener;
    }

    /**
     * Called when a fragment is first attached to its context. onCreate(android.os.Bundle) will be called after this.
     * @param context context of the application creating the dialog fragment
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    /**
     * a public interface that depicts a listener to be triggered upon the completion of the dialog window; this listener needs to accept a string parameter to represent the value filled in on the dialog popup window.
     */
    public interface ReAuthUserDialogListener {
        void returnResult(String Password, Boolean Authenticated);
    }

}
