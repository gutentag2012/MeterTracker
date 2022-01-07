package com.joshuagawenda.metertracker;

import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderContract;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMeasurementFragment} factory method to
 * create an instance of this fragment.
 */
public class AddMeasurementFragment extends Fragment {

    private static final String TAG = "AddMeasurementFragment";
    TextInputLayout typeFieldLayout;
    TextInputLayout measurementTextViewLayout;
    TextInputLayout valueTextViewLayout;
    DataReaderDBHelper dbHelper;
    AutoCompleteTextView typeField;
    TextInputEditText measurementTextView;
    TextInputEditText dateTextView;
    TextInputEditText valueTextView;
    CheckBox higherPreferredCheckBox;
    private TextInputLayout dateTextViewLayout;
    private Date date = new Date();
    private DataReaderContract.DataEntry entry;
    private DataAggregation aggregation;
    private int order = -1;
    private Button deleteBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String type = arguments.getString("type");
            String unit = arguments.getString("unit");
            int order = arguments.getInt("order");
            boolean isHigherPositive = arguments.getBoolean("isHigherPositive");
            if (arguments.getBoolean("isEntry")) {
                int id = arguments.getInt("id");
                float value = arguments.getFloat("value");
                this.date = DateUtils.stringToDate(arguments.getString("date"));
                entry = new DataReaderContract.DataEntry(id, type, unit, value, order, this.date, isHigherPositive);
            } else {
                aggregation = new DataAggregation(type, unit, order, isHigherPositive, "", 0, 0, 0, 0);
            }
        }
    }

    // TODO Export Picture of graph
    // TODO Filter years graph

    // TODO Warn duplicate entries

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_measurement, container, false);
        this.dbHelper = new DataReaderDBHelper(requireContext());
        this.typeField = view.findViewById(R.id.type);
        this.measurementTextView = view.findViewById(R.id.measurement_unit);
        this.dateTextViewLayout = view.findViewById(R.id.date_parent);
        this.dateTextView = view.findViewById(R.id.date);
        this.higherPreferredCheckBox = view.findViewById(R.id.higher_check_box);
        this.valueTextView = view.findViewById(R.id.value);
        this.valueTextView.setKeyListener(DigitsKeyListener.getInstance("0123456789.,-"));
        this.valueTextView.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        this.typeFieldLayout = view.findViewById(R.id.type_layout);
        this.measurementTextViewLayout = view.findViewById(R.id.measurement_unit_layout);
        this.valueTextViewLayout = view.findViewById(R.id.value_layout);
        this.deleteBtn = view.findViewById(R.id.delete_btn);

        if (this.aggregation != null) {
            this.valueTextViewLayout.setVisibility(View.GONE);
            this.dateTextViewLayout.setVisibility(View.GONE);
        }

        if (this.entry != null) {
            typeField.setText(this.entry.type);
            measurementTextView.setText(this.entry.unit);
            valueTextView.setText(String.valueOf(this.entry.value));
            dateTextView.setText(DateUtils.dateToString(this.entry.date));
            higherPreferredCheckBox.setChecked(this.entry.isHigherPositive);
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setOnClickListener(e -> {
                dbHelper.delete(this.entry);
                ((MainActivity) getActivity()).getNavController().navigateUp();
            });
        }

        TypeDropdownAdapter adapter = new TypeDropdownAdapter(requireContext(), dbHelper.getAllTypes());
        typeField.setAdapter(adapter);
        typeField.setOnItemClickListener((adapterView, view1, i, l) -> {
            String[] item = adapter.getItem(i);
            Log.e("TAG", "SELECTED" + Arrays.toString(item));
            measurementTextView.setText(item[1]);
            this.order = Integer.parseInt(item[2]);
            // TODO Do not get last, but previous value
            this.valueTextViewLayout.setHelperText(getString(R.string.last_value, Float.parseFloat(item[3]), item[1]));
            higherPreferredCheckBox.setChecked(Boolean.parseBoolean(item[4]));
        });

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            DataAggregation selectedType = activity.selectedType;
            if (selectedType != null) {
                typeField.setText(selectedType.type, false);
                measurementTextView.setText(selectedType.unit);
                higherPreferredCheckBox.setChecked(selectedType.isHigherPositive);
                List<DataReaderContract.DataEntry> oldEntries = dbHelper.selectAll(selectedType.type, selectedType.unit, 2);
                float oldValue = oldEntries.size() > 1 && oldEntries.get(0).value == 0
                        ? oldEntries.get(1).value
                        : oldEntries.size() > 0
                        ? oldEntries.get(0).value
                        : 0f;
                // TODO Do not get last, but previous value
                this.valueTextViewLayout.setHelperText(getString(R.string.last_value, oldValue, selectedType.unit));
            }
        }

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
        boolean isHigherPositive = this.higherPreferredCheckBox.isChecked();
        if (typeField.getText().length() == 0) {
            typeFieldLayout.setError("This field should not be empty!");
            error = true;
        }
        String type = typeField.getText().toString();
        if (measurementTextView.getText().length() == 0) {
            measurementTextViewLayout.setError("This field should not be empty!");
            error = true;
        }
        String unit = measurementTextView.getText().toString();

        if (this.aggregation != null && !error) {
            DataAggregation newAggregation = new DataAggregation(
                    type,
                    unit,
                    this.aggregation.order,
                    isHigherPositive,
                    "", 0, 0, 0, 0
            );
            dbHelper.updateFromAggregation(this.aggregation, newAggregation);
            ((MainActivity) getActivity()).getNavController().navigateUp();
            ((MainActivity) getActivity()).getNavController().navigateUp();
            return;
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
                type,
                unit,
                value,
                this.entry == null ? this.order : this.entry.order,
                date,
                isHigherPositive
        );
        Log.e(TAG, "save: " + dataEntry);
        if (this.entry == null)
            dbHelper.insertAll(dataEntry);
        else
            dbHelper.update(dataEntry, null);
        ((MainActivity) getActivity()).getNavController().navigateUp();
    }
}