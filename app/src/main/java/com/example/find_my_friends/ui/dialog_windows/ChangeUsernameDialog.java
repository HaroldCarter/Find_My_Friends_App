package com.example.find_my_friends.ui.dialog_windows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
 * a custom Dialog window class which allows for the Username for a user to be changed
 *
 * @author Harold Carter
 * @version 2.0
 */
public class ChangeUsernameDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText usernameEditText;
    private EditText confirmUsernameEditText;
    private Button confrimBTN;
    private Button denyBTN;

    private ChangeUsernameDialogListener changeUsernameDialogListener;

    /**
     * Called Override to build a custom dialog container which displays two text boxes and checks that the their contents matches.
     * used for changing the username of the current user.
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
        usernameEditText = view.findViewById(R.id.popup_window_editText);
        confirmUsernameEditText = view.findViewById(R.id.popup_window_confirm_EditText);
        confrimBTN = view.findViewById(R.id.popup_confirmBTN);
        denyBTN = view.findViewById(R.id.popup_denyBTN);
        popupTitleTextView.setText(("Type In you new Username"));
        usernameEditText.setHint(("Username"));
        confirmUsernameEditText.setHint(("Confirm Username"));
        AlertDialog dialog = builder.create();
        confrimBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmailIsValid()) {
                    changeUsernameDialogListener.returnResult(usernameEditText.getText().toString());
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
     * checks the validity of the inputted textviews, checks that they are both populated and checks if they contents are matching.
     * @return boolean, true if they text boxes match else false.
     */
    private boolean checkEmailIsValid() {
        boolean validData = true;
        if (TextUtils.isEmpty(usernameEditText.getText())) {
            usernameEditText.setError("please enter a username");
            validData = false;
        }
        if (!usernameEditText.getText().toString().equals(confirmUsernameEditText.getText().toString())) {
            confirmUsernameEditText.setError("please check your emails match");
            validData = false;
        }
        return validData;
    }

    /**
     * set the internal variable for the instance of the changeUsernameDialogListener interface to the instance passed by reference as a parameter of the function, this is to be triggered once the dialog window has successfully completed.
     * @param changeEmailDialogListener instance of Listener to set the internal listener equal to.
     */
    public void setChangeUsernameDialogListener(ChangeUsernameDialogListener changeEmailDialogListener) {
        this.changeUsernameDialogListener = changeEmailDialogListener;
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
    public interface ChangeUsernameDialogListener {
        void returnResult(String Email);
    }

}