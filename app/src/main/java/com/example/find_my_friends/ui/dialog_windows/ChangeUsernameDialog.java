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

public class ChangeUsernameDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText usernameEditText;
    private EditText confirmUsernameEditText;
    private Button confrimBTN;
    private Button denyBTN;

    private ChangeUsernameDialogListener changeUsernameDialogListener;

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
                if(checkEmailIsValid()){
                    //if they password is valid then return the password.
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



    private boolean checkEmailIsValid() {
        boolean validData = true;

        //checks the username isn't empty
        if (TextUtils.isEmpty(usernameEditText.getText())) {
            usernameEditText.setError("please enter a username");
            validData = false;
        }

        //checks that the username's match
        if (!usernameEditText.getText().toString().equals(confirmUsernameEditText.getText().toString())) {
            confirmUsernameEditText.setError("please check your emails match");
            validData = false;
        }


        return validData;
    }


    public void setChangeUsernameDialogListener(ChangeUsernameDialogListener changeEmailDialogListener){
        this.changeUsernameDialogListener = changeEmailDialogListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface ChangeUsernameDialogListener {
        void returnResult(String Email);
    }

}