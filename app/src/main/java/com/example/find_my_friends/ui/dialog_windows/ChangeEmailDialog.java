package com.example.find_my_friends.ui.dialog_windows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.find_my_friends.R;

/**
 * a custom Dialog window class which allows for the email for a user to be changed
 *
 * @author Harold Carter
 * @version 2.0
 */
public class ChangeEmailDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private Button confirmBTN;
    private Button denyBTN;

    private String titleText;

    private ChangeEmailDialogListener changeEmailDialogListener;

    /**
     * Called Override to build a custom dialog container which displays two text boxs and checks that the their contents matches.
     *  used for changing the email of the current user.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.confirm_form_popup, null);
        builder.setView(view);
        popupTitleTextView = view.findViewById(R.id.popup_window_title);
        emailEditText = view.findViewById(R.id.popup_window_editText);
        confirmEmailEditText = view.findViewById(R.id.popup_window_confirm_EditText);
        confirmBTN = view.findViewById(R.id.popup_confirmBTN);
        denyBTN = view.findViewById(R.id.popup_denyBTN);
        if (titleText != null) {
            popupTitleTextView.setText(titleText);
        } else {
            popupTitleTextView.setText(("Type In you new email address"));
        }
        emailEditText.setHint(("Email"));
        confirmEmailEditText.setHint(("Confirm Email"));
        AlertDialog dialog = builder.create();
        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmailIsValid()) {
                    changeEmailDialogListener.returnResult(emailEditText.getText().toString());
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
     *
     * @param titleText
     */
    public void setTitleText(String titleText) {
        this.titleText = titleText;
        if (this.popupTitleTextView != null) {
            this.popupTitleTextView.setText(titleText);
        }
    }

    /**
     * checks the email matches the pattern matcher for an email, and also check that the two text boxes for the emails contain matching email addresses; if both cases are true then the function returns true, else false
     * @return boolean representing the validity of the
     */
    private boolean checkEmailIsValid() {
        boolean validData = true;
        if ((TextUtils.isEmpty(emailEditText.getText().toString())) || (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())) {
            emailEditText.setError("please enter a valid email address");
            validData = false;
        }
        if (!emailEditText.getText().toString().equals(confirmEmailEditText.getText().toString())) {
            confirmEmailEditText.setError("please check your emails match");
            validData = false;
        }
        return validData;
    }

    /**
     * set the internal variable for the instance of the changeEmailDialogListener interface to the instance passed by reference as a parameter of the function, this is to be triggered once the dialog window has successfully completed.
     * @param changePasswordDialogListener instance of Listener to set the internal listener equal to.
     */
    public void setChangeEmailDialogListener(ChangeEmailDialogListener changePasswordDialogListener) {
        this.changeEmailDialogListener = changePasswordDialogListener;
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
    public interface ChangeEmailDialogListener {
        void returnResult(String Email);
    }

}