package com.joshuagawenda.metertracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderContract;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    TextView title;
    TextView last_week;
    TextView average_month;
    TextView difference;
    TextView last_date;
    DataEntryAdapter adapter;

    private DataAggregation aggregation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String type = getArguments().getString("type");
            String unit = getArguments().getString("unit");
            int order = getArguments().getInt("order");
            String lastDate = getArguments().getString("lastDate");
            float averageMonth = getArguments().getFloat("averageMonth");
            float lastWeek = getArguments().getFloat("lastWeek");
            float monthlyDifference = getArguments().getFloat("monthlyDifference");
            this.aggregation = new DataAggregation(type, unit, order, lastDate, lastWeek, averageMonth, monthlyDifference);
        }
    }

    private void update(DataReaderDBHelper dbHelper) {
        this.aggregation = dbHelper.aggregate()
                .stream()
                .filter(e -> e.type.equals(this.aggregation.type) && e.unit.equals(this.aggregation.unit))
                .findFirst().orElse(null);
        if(this.aggregation==null)
            return;

        title.setText(this.aggregation.type);
        last_week.setText(String.format(Locale.getDefault(), "%.2f", this.aggregation.lastWeek));
        average_month.setText(String.format(Locale.getDefault(), "%.2f", this.aggregation.average));
        boolean positive = this.aggregation.monthlyDifference >= 0;
        difference.setText(String.format(Locale.getDefault(), "%s%.2f%%", positive ? "+" : "", this.aggregation.monthlyDifference));
        difference.setTextColor(ContextCompat.getColor(requireContext(), positive ? R.color.red : R.color.green));
        last_date.setText(this.aggregation.lastDate);

        adapter.setItems(dbHelper.selectAll(this.aggregation.type, this.aggregation.unit));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        title = view.findViewById(R.id.title);
        last_week = view.findViewById(R.id.last_week);
        average_month = view.findViewById(R.id.average_month);
        difference = view.findViewById(R.id.difference);
        last_date = view.findViewById(R.id.last_date);

        RecyclerView recyclerView = view.findViewById(R.id.body);
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(requireContext());
        List<DataReaderContract.DataEntry> dataEntries = dbHelper.selectAll(this.aggregation.type, this.aggregation.unit);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DataEntryAdapter(dataEntries);
        adapter.setOnDelete(entry -> {
            dbHelper.delete(entry);
            update(dbHelper);
        });
        adapter.setOnUpdate(entry -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id", entry.id);
            bundle.putString("type", entry.type);
            bundle.putString("unit", entry.unit);
            bundle.putFloat("value", entry.value);
            bundle.putInt("order", entry.order);
            bundle.putString("date", DateUtils.dateToString(entry.date));
            ((MainActivity) getActivity()).getNavController().navigate(R.id.action_global_to_addMeasurementFragment, bundle);
        });
        recyclerView.setAdapter(adapter);

        update(dbHelper);

        return view;
    }
}