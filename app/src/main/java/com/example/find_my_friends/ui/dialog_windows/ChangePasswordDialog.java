package com.example.find_my_friends.ui.dialog_windows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.find_my_friends.R;

/**
 * a custom Dialog window class which allows for the password for a user to be changed
 *
 * @author Harold Carter
 * @version 2.0
 */
public class ChangePasswordDialog extends AppCompatDialogFragment {
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button confrimBTN;
    private Button denyBTN;
    private ChangePasswordDialogListener changePasswordDialogListener;

    /**
     * Called Override to build a custom dialog container which displays two password editTexts and checks that the their contents matches.
     *  used for changing the password of the current user.
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
        AlertDialog dialog = builder.create();
        confrimBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPasswordIsValid()) {
                    changePasswordDialogListener.returnResult(passwordEditText.getText().toString());
                    dialog.dismiss();
                }
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
     * this function compares the two password edit text boxes and check if the passwords are matching; it will return the result of this comparison, it also checks that the passwords are over 5 characters long.
     * @return boolean true-if the passwords match and over 5 characters, false-if they don't match criteria
     */
    private boolean checkPasswordIsValid() {
        boolean validData = true;
        if ((!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()))) {
            confirmPasswordEditText.setError("please check your password match");
            validData = false;
        }
        if (passwordEditText.getText().toString().length() < 5) {
            passwordEditText.setError("please make sure your password is over 5 characters");
            validData = false;
        }
        return validData;
    }


    /**
     * set the internal variable for the instance of the ChangePasswordDialogListener interface to the instance passed by reference as a parameter of the function, this is to be triggered once the dialog window has successfully completed.
     * @param changePasswordDialogListener instance of Listener to set the internal listener equal to.
     */
    public void setChangePasswordDialogListener(ChangePasswordDialogListener changePasswordDialogListener) {
        this.changePasswordDialogListener = changePasswordDialogListener;
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
    public interface ChangePasswordDialogListener {
        void returnResult(String Password);
    }

}
