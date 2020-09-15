package com.example.find_my_friends.util;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * a public class for the dialog picker for the time, this simply expands the existing class and allows the time picker to be initialized from the current time of the device with the current format
 * @author Harold Carter
 * @version 1.0
 */
public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get((Calendar.MINUTE));
        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), hour, min, android.text.format.DateFormat.is24HourFormat(getActivity()));
    }
}
