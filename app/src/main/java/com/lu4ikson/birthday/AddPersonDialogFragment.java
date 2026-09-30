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

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private static final String ARG_NOTIFY = "notify";
    private static final String ARG_NOTIFY_HOUR = "notify_hour";
    private static final String ARG_NOTIFY_MINUTE = "notify_minute";

    private static final int[] DAYS_IN_MONTH = {
            31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private EditText editTextName;
    private NumberPicker numberPickerDay;
    private Spinner spinnerMonth;
    private CheckBox checkBoxYearKnown;
    private EditText editTextYear;
    private CheckBox checkBoxNotify;
    private TextView textNotifyTime;

    private int notifyHour = 20;
    private int notifyMinute = 0;

    private Integer editingPersonId = null; // null = режим добавления, не null = режим редактирования

    public static AddPersonDialogFragment newInstanceForEdit(Person person) {
        AddPersonDialogFragment fragment = new AddPersonDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, person.id);
        args.putString(ARG_NAME, person.name);
        args.putInt(ARG_DAY, person.day);
        args.putInt(ARG_MONTH, person.month);
        if (person.year != null) {
            args.putInt(ARG_YEAR, person.year);
        }
        args.putBoolean(ARG_NOTIFY, person.notifyDayBefore);
        if (person.notifyHour != null) {
            args.putInt(ARG_NOTIFY_HOUR, person.notifyHour);
        }
        if (person.notifyMinute != null) {
            args.putInt(ARG_NOTIFY_MINUTE, person.notifyMinute);
        }
        fragment.setArguments(args);
        return fragment;
    }

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

        spinnerMonth.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateDayPickerForMonth(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        updateDayPickerForMonth(spinnerMonth.getSelectedItemPosition());

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

        boolean isEditMode = fillFromArgumentsIfEditing();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isEditMode ? R.string.dialog_edit_title : R.string.dialog_add_title)
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

    // Возвращает true, если диалог открыт в режиме редактирования
    private boolean fillFromArgumentsIfEditing() {
        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_ID)) {
            return false;
        }

        editingPersonId = args.getInt(ARG_ID);
        editTextName.setText(args.getString(ARG_NAME));

        int month = args.getInt(ARG_MONTH);
        spinnerMonth.setSelection(month - 1); // может вызвать onItemSelected, если значение меняется

        int day = args.getInt(ARG_DAY);
        numberPickerDay.setValue(day);

        if (args.containsKey(ARG_YEAR)) {
            checkBoxYearKnown.setChecked(true);
            editTextYear.setVisibility(View.VISIBLE);
            editTextYear.setText(String.valueOf(args.getInt(ARG_YEAR)));
        }

        boolean notify = args.getBoolean(ARG_NOTIFY);
        checkBoxNotify.setChecked(notify);
        textNotifyTime.setVisibility(notify ? View.VISIBLE : View.GONE);
        if (args.containsKey(ARG_NOTIFY_HOUR)) {
            notifyHour = args.getInt(ARG_NOTIFY_HOUR);
            notifyMinute = args.getInt(ARG_NOTIFY_MINUTE);
            updateNotifyTimeText();
        }

        return true;
    }

    private void updateDayPickerForMonth(int monthIndex) {
        int maxDay = DAYS_IN_MONTH[monthIndex];
        if (numberPickerDay.getValue() > maxDay) {
            numberPickerDay.setValue(maxDay);
        }
        numberPickerDay.setMaxValue(maxDay);
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
        int month = spinnerMonth.getSelectedItemPosition() + 1;

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

        if (editingPersonId != null) {
            person.id = editingPersonId;
            viewModel.update(person);
            AlarmScheduler.cancelAll(appContext, person.id);
            AlarmScheduler.scheduleAll(appContext, person);
        } else {
            viewModel.insert(person, id -> {
                person.id = (int) id;
                AlarmScheduler.scheduleAll(appContext, person);
            });
        }

        return true;
    }
}