package com.joshuagawenda.metertracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt("id");
            String type = getArguments().getString("type");
            String unit = getArguments().getString("unit");
            float value = getArguments().getFloat("value");
            Date date = DateUtils.stringToDate(getArguments().getString("date"));
            entry = new DataReaderContract.DataEntry(id, type, unit, value, date);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_measurement, container, false);
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(requireContext());
        AutoCompleteTextView typeField = view.findViewById(R.id.type);
        TextInputEditText measurementTextView = view.findViewById(R.id.measurement_unit);
        TextInputEditText dateTextView = view.findViewById(R.id.date);
        TextInputEditText valueTextView = view.findViewById(R.id.value);
        TextInputLayout typeFieldLayout = view.findViewById(R.id.type_layout);
        TextInputLayout measurementTextViewLayout = view.findViewById(R.id.measurement_unit_layout);
        TextInputLayout valueTextViewLayout = view.findViewById(R.id.value_layout);

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

        getActivity().findViewById(R.id.save_button).setOnClickListener(v -> {
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
                    date
            );
            if (this.entry == null)
                dbHelper.insertAll(dataEntry);
            else
                dbHelper.update(dataEntry);
            ((MainActivity) getActivity()).getNavController().navigateUp();
        });
        return view;
    }
}