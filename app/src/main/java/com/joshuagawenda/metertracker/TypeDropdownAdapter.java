package com.joshuagawenda.metertracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TypeDropdownAdapter extends ArrayAdapter<String[]> {

    private final List<String[]> allEntries;
    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<String[]> suggestion = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                suggestion.addAll(allEntries);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (String[] entry : allEntries) {
                    boolean contains1 = entry[0].contains(filterPattern);
                    boolean contains2 = entry[1].contains(filterPattern);
                    if (contains1 || contains2)
                        suggestion.add(entry);
                }
            }
            results.values = suggestion;
            results.count = suggestion.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List<String[]>) filterResults.values);
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((String[]) resultValue)[0];
        }
    };

    public TypeDropdownAdapter(Context context, List<String[]> objects) {
        super(context, 0, objects);
        this.allEntries = objects;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String[] element = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.element_dropdown, parent, false);
        }
        TextView typeTextView = convertView.findViewById(R.id.type);
        TextView unitTextView = convertView.findViewById(R.id.measurement_unit);
        typeTextView.setText(element[0]);
        unitTextView.setText(element[1]);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }
}
