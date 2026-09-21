package com.lu4ikson.birthday;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class AddPersonDialogFragment extends DialogFragment {

    private EditText editTextName;
    private NumberPicker numberPickerDay;
    private Spinner spinnerMonth;
    private CheckBox checkBoxYearKnown;
    private EditText editTextYear;
    private CheckBox checkBoxNotify;
    private TextView textNotifyTime;

    private int notifyHour = 20;
    private int notifyMinute = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_person, null);

        editTextName = view.findViewById(R.id.editTextName);
        numberPickerDay = view.findViewById(R.id.numberPickerDay);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        checkBoxYearKnown = view.findViewById(R.id.checkBoxYearKnown);
        editTextYear = view.findViewById(R.id.editTextYear);
        checkBoxNotify = view.findViewById(R.id.checkBoxNotify);
        textNotifyTime = view.findViewById(R.id.textNotifyTime);

        numberPickerDay.setMinValue(1);
        numberPickerDay.setMaxValue(31);
        numberPickerDay.setValue(1);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        checkBoxYearKnown.setOnCheckedChangeListener((buttonView, isChecked) ->
                editTextYear.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        checkBoxNotify.setOnCheckedChangeListener((buttonView, isChecked) ->
                textNotifyTime.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        updateNotifyTimeText();
        textNotifyTime.setOnClickListener(v -> new TimePickerDialog(
                requireContext(),
                (timePicker, hour, minute) -> {
                    notifyHour = hour;
                    notifyMinute = minute;
                    updateNotifyTimeText();
                },
                notifyHour, notifyMinute, true
        ).show());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_add_title)
                .setView(view)
                .setPositiveButton(R.string.button_save, null)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (trySave()) {
                        dialog.dismiss();
                    }
                })
        );

        return dialog;
    }

    private void updateNotifyTimeText() {
        textNotifyTime.setText(getString(R.string.notify_time_format, notifyHour, notifyMinute));
    }

    private boolean trySave() {
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            editTextName.setError(getString(R.string.error_name_required));
            return false;
        }

        int day = numberPickerDay.getValue();
        int month = spinnerMonth.getSelectedItemPosition() + 1; // 0+1 январь 11+1 декабрь

        Integer year = null;
        if (checkBoxYearKnown.isChecked()) {
            String yearText = editTextYear.getText().toString().trim();
            if (TextUtils.isEmpty(yearText)) {
                editTextYear.setError(getString(R.string.error_year_required));
                return false;
            }
            year = Integer.parseInt(yearText);
        }

        Person person = new Person(name, day, month, year);
        person.notifyDayBefore = checkBoxNotify.isChecked();
        if (person.notifyDayBefore) {
            person.notifyHour = notifyHour;
            person.notifyMinute = notifyMinute;
        }

        Context appContext = requireContext().getApplicationContext();

        PersonViewModel viewModel = new ViewModelProvider(requireActivity()).get(PersonViewModel.class);
        viewModel.insert(person, id -> {
            person.id = (int) id;
            AlarmScheduler.scheduleAll(appContext, person);
        });
        return true;
    }
}