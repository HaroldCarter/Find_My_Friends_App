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

public class ChangeEmailDialog extends AppCompatDialogFragment {
    private TextView popupTitleTextView;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private Button confrimBTN;
    private Button denyBTN;

    private String titleText;

    private ChangeEmailDialogListener changeEmailDialogListener;

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
        confrimBTN = view.findViewById(R.id.popup_confirmBTN);
        denyBTN = view.findViewById(R.id.popup_denyBTN);

        if(titleText != null){
            popupTitleTextView.setText(titleText);
        }else{
            popupTitleTextView.setText(("Type In you new email address"));
        }

        emailEditText.setHint(("Email"));
        confirmEmailEditText.setHint(("Confirm Email"));


        AlertDialog dialog = builder.create();

        confrimBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEmailIsValid()){
                    //if they password is valid then return the password.
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

    public void setTitleText(String titleText) {
        this.titleText = titleText;
        if(this.popupTitleTextView != null){
            this.popupTitleTextView.setText(titleText);
        }
    }

    private boolean checkEmailIsValid() {
        boolean validData = true;

        //checks that the email is a populated and that it matches the email pattern matcher
        if ((TextUtils.isEmpty(emailEditText.getText().toString())) || (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())) {
            emailEditText.setError("please enter a valid email address");
            validData = false;
        }

        //checks that the emails match
        if (!emailEditText.getText().toString().equals(confirmEmailEditText.getText().toString())) {
            confirmEmailEditText.setError("please check your emails match");
            validData = false;
        }


        return validData;
    }


    public void setChangeEmailDialogListener(ChangeEmailDialogListener changePasswordDialogListener){
        this.changeEmailDialogListener = changePasswordDialogListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public interface ChangeEmailDialogListener {
        void returnResult(String Email);
    }

}