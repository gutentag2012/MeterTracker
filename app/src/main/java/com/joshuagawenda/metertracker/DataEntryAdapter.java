 package com.joshuagawenda.metertracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuagawenda.metertracker.database.DataReaderContract;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.List;
import java.util.Locale;

 public class DataEntryAdapter extends RecyclerView.Adapter<DataEntryAdapter.ViewHolder> {
    private final List<DataReaderContract.DataEntry> entries;
     private Consumer<DataReaderContract.DataEntry> onDelete;
     private Consumer<DataReaderContract.DataEntry> onUpdate;

     public DataEntryAdapter(List<DataReaderContract.DataEntry> entries) {
        this.entries = entries;
     }

     public void setOnDelete(Consumer<DataReaderContract.DataEntry> onDelete) {
         this.onDelete = onDelete;
     }

     public void setOnUpdate(Consumer<DataReaderContract.DataEntry> onUpdate) {
         this.onUpdate = onUpdate;
     }

     @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater from = LayoutInflater.from(parent.getContext());
        View inflate = from.inflate(R.layout.element_data_entry, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    public void setItems(List<DataReaderContract.DataEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView value;
        private final TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.value = itemView.findViewById(R.id.value);
            this.date = itemView.findViewById(R.id.date);
        }

        public void bind(DataReaderContract.DataEntry entry) {
            this.value.setText(String.format(Locale.getDefault(), "%.2f %s", entry.value, entry.unit));
            this.date.setText(DateUtils.dateToString(entry.date));
            super.itemView.setOnClickListener(v -> {
                if(onUpdate!=null)
                    onUpdate.accept(entry);
            });
            super.itemView.setOnLongClickListener(view -> {
                if(onDelete!=null)
                    onDelete.accept(entry);
                return true;
            });
        }
    }
}
