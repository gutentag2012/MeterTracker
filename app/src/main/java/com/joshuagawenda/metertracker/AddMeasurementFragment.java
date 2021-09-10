package com.joshuagawenda.metertracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.joshuagawenda.metertracker.database.DataReaderContract;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMeasurementFragment} factory method to
 * create an instance of this fragment.
 */
public class AddMeasurementFragment extends Fragment {

    private Date date = new Date();
    private DataReaderContract.DataEntry entry;
    private int order = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt("id");
            String type = getArguments().getString("type");
            String unit = getArguments().getString("unit");
            float value = getArguments().getFloat("value");
            int order = getArguments().getInt("order");
            this.date = DateUtils.stringToDate(getArguments().getString("date"));
            entry = new DataReaderContract.DataEntry(id, type, unit, value, order, this.date);
        }
    }
    TextInputLayout typeFieldLayout;
    TextInputLayout measurementTextViewLayout;
    TextInputLayout valueTextViewLayout;
    DataReaderDBHelper dbHelper;
    AutoCompleteTextView typeField;
    TextInputEditText measurementTextView;
    TextInputEditText dateTextView;
    TextInputEditText valueTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_measurement, container, false);
        this.dbHelper = new DataReaderDBHelper(requireContext());
        this.typeField = view.findViewById(R.id.type);
        this.measurementTextView = view.findViewById(R.id.measurement_unit);
        this.dateTextView = view.findViewById(R.id.date);
        this.valueTextView = view.findViewById(R.id.value);
        this.typeFieldLayout = view.findViewById(R.id.type_layout);
        this.measurementTextViewLayout = view.findViewById(R.id.measurement_unit_layout);
        this.valueTextViewLayout = view.findViewById(R.id.value_layout);

        if (this.entry != null) {
            typeField.setText(this.entry.type);
            measurementTextView.setText(this.entry.unit);
            valueTextView.setText(String.valueOf(this.entry.value));
            dateTextView.setText(DateUtils.dateToString(this.entry.date));
        }

        TypeDropdownAdapter adapter = new TypeDropdownAdapter(requireContext(), dbHelper.getAllTypes());
        typeField.setAdapter(adapter);
        typeField.setOnItemClickListener((adapterView, view1, i, l) -> {
            String[] item = adapter.getItem(i);
            measurementTextView.setText(item[1]);
            this.order = Integer.parseInt(item[2]);
        });

        dateTextView.setText(DateUtils.dateToString(this.date));
        dateTextView.setOnClickListener(v ->
                DateUtils.displayDatePicker(
                        getActivity().getSupportFragmentManager(),
                        this.date,
                        "Select a Date",
                        date -> dateTextView.setText(DateUtils.dateToString(this.date = date)),
                        null
                )
        );

        valueTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
            save();
            return true;
        });

        getActivity().findViewById(R.id.save_button).setOnClickListener(v -> {
            save();
        });
        return view;
    }

    private void save() {
        boolean error = false;
        if (typeField.getText().length() == 0) {
            typeFieldLayout.setError("This field should not be empty!");
            error = true;
        }
        if (measurementTextView.getText().length() == 0) {
            measurementTextViewLayout.setError("This field should not be empty!");
            error = true;
        }
        if (valueTextView.getText().length() == 0) {
            valueTextViewLayout.setError("This field should not be empty!");
            error = true;
        }
        float value = 0;
        try {
            value = Float.parseFloat(valueTextView.getText().toString());
        } catch (NumberFormatException ignored) {
            valueTextViewLayout.setError("This is not a valid number!");
            error = true;
        }

        if (error)
            return;


        DataReaderContract.DataEntry dataEntry = new DataReaderContract.DataEntry(
                this.entry == null ? 0 : this.entry.id,
                typeField.getText().toString(),
                measurementTextView.getText().toString(),
                value,
                this.entry == null ? this.order : this.entry.order,
                date
        );
        if (this.entry == null)
            dbHelper.insertAll(dataEntry);
        else
            dbHelper.update(dataEntry, null);
        ((MainActivity) getActivity()).getNavController().navigateUp();
    }
}