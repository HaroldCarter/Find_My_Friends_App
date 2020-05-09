package com.example.find_my_friends.ui.dialog_windows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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


public class ReAuthUserDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button confrimBTN;
    private Button denyBTN;


    private String titleText;



    private AlertDialog dialog;


    private final String TAG = "ReAuthUserDialog :";

    private  ReAuthUserDialogListener reAuthUserDialogListener;

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
                checkUserAuthenication();
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



    public void setTitle(String titleText){
        this.titleText = titleText;
        if(popupTitleTextView != null){
            popupTitleTextView.setText(titleText);
        }
    }


    private void checkUserAuthenication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //checks that the passwords match.
        if ((!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()))) {
            confirmPasswordEditText.setError("please check your password match");
            return;
        }
        if(user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), passwordEditText.getText().toString()); // Current Login Credentials \\
            // Prompt the user to re-provide their sign-in credentials
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


    public void setReAuthUserDialogListener(ReAuthUserDialogListener reAuthUserDialogListener){
        this.reAuthUserDialogListener = reAuthUserDialogListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface ReAuthUserDialogListener {
        void returnResult(String Password, Boolean Authenticated);
    }

}
