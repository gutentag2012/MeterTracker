package com.joshuagawenda.metertracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DataAggregationAdapter extends RecyclerView.Adapter<DataAggregationAdapter.ViewHolder> {
    private final MainActivity mainActivity;
    private final List<DataAggregation> aggregations = new ArrayList<>();

    public DataAggregationAdapter(MainActivity mainActivity, List<DataAggregation> aggregations) {
        this.mainActivity = mainActivity;
        setItems(aggregations);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater from = LayoutInflater.from(parent.getContext());
        View inflate = from.inflate(R.layout.element_aggregate_data, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(aggregations.get(position));
    }

    @Override
    public int getItemCount() {
        return this.aggregations.size();
    }

    public void setItems(List<DataAggregation> aggregate) {
        this.aggregations.clear();
        this.aggregations.addAll(aggregate);
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(this.mainActivity);
        for (int i = 0; i < aggregate.size(); i++) {
            DataAggregation dataAggregation = aggregate.get(i);
            if (dataAggregation.order != i) {
                dbHelper.updateOrder(dataAggregation.type, dataAggregation.unit, i);
            }
        }
        this.notifyDataSetChanged();
    }

    public void swap(int fromPosition, int toPosition) {
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(this.mainActivity);
        dbHelper.updateOrder(aggregations.get(fromPosition).type, aggregations.get(fromPosition).unit, toPosition);
        dbHelper.updateOrder(aggregations.get(toPosition).type, aggregations.get(toPosition).unit, fromPosition);
        Collections.swap(aggregations, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView last_week;
        private final TextView average_month;
        private final TextView difference;
        private final TextView last_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.last_week = itemView.findViewById(R.id.last_week);
            this.average_month = itemView.findViewById(R.id.average_month);
            this.difference = itemView.findViewById(R.id.difference);
            this.last_date = itemView.findViewById(R.id.last_date);
        }

        public void bind(DataAggregation dataAggregation) {
            this.title.setText(dataAggregation.type);
            this.last_week.setText(String.format(Locale.getDefault(), "%s%.2f", dataAggregation.lastWeek >= 0 ? "+" : "", dataAggregation.lastWeek));
            this.average_month.setText(String.format(Locale.getDefault(), "%s%.2f", dataAggregation.average >= 0 ? "+" : "", dataAggregation.average));
            boolean positive = dataAggregation.monthlyDifference >= 0;
            this.difference.setText(String.format(Locale.getDefault(), "%s%.2f%%", positive ? "+" : "", dataAggregation.monthlyDifference));
            this.difference.setTextColor(ContextCompat.getColor(super.itemView.getContext(), positive ^ dataAggregation.isHigherPositive ? R.color.red : R.color.green));
            this.last_date.setText(dataAggregation.lastDate);
            super.itemView.setOnClickListener(v -> {
                if(this.itemView.getContext() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) this.itemView.getContext();
                    mainActivity.selectedType = dataAggregation;
                }
                Bundle bundle = new Bundle();
                bundle.putString("type", dataAggregation.type);
                bundle.putString("unit", dataAggregation.unit);
                bundle.putInt("order", dataAggregation.order);
                bundle.putBoolean("isHigherPositive", dataAggregation.isHigherPositive);
                bundle.putString("lastDate", dataAggregation.lastDate);
                bundle.putFloat("averageMonth", dataAggregation.average);
                bundle.putFloat("lastWeek", dataAggregation.lastWeek);
                bundle.putFloat("monthlyDifference", dataAggregation.monthlyDifference);
                mainActivity.getNavController().navigate(R.id.action_dashboardFragment_to_historyFragment, bundle);
            });
        }
    }
}
