package com.joshuagawenda.metertracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshuagawenda.metertracker.database.DataReaderDBHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    private static final String TAG = "Fragment:Dashboard";

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(requireContext());

        RecyclerView body = view.findViewById(R.id.body);
        body.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        DataAggregationAdapter adapter = new DataAggregationAdapter(((MainActivity) getActivity()), dbHelper.aggregate());
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.swap(fromPosition, toPosition);
return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(body);
        body.setAdapter(adapter);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(() -> {
            Log.e(TAG, "onCreateView: " + dbHelper.aggregate());
            adapter.setItems(dbHelper.aggregate());
            refreshLayout.setRefreshing(false);
        });
        return view;
    }
}