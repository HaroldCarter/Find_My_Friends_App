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


public class ChangePasswordDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button confrimBTN;
    private Button denyBTN;

    private ChangePasswordDialogListener changePasswordDialogListener;

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
                if(checkPasswordIsValid()){
                    //if they password is valid then return the password.
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



    private boolean checkPasswordIsValid() {
        boolean validData = true;

        //checks that the passwords match.
        if ((!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()))) {
            confirmPasswordEditText.setError("please check your password match");
            validData = false;
        }

        //checks the password is of an appropriate length
        if (passwordEditText.getText().toString().length() < 5) {
            passwordEditText.setError("please make sure your password is over 5 characters");
            validData = false;
        }

        return validData;
    }


    public void setChangePasswordDialogListener(ChangePasswordDialogListener changePasswordDialogListener){
        this.changePasswordDialogListener = changePasswordDialogListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface ChangePasswordDialogListener {
        void returnResult(String Password);
    }

}
