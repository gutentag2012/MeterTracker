package com.joshuagawenda.metertracker;

import static com.joshuagawenda.metertracker.database.DatabaseAccessor.CSV_DATA_CREATE_REQUEST;
import static com.joshuagawenda.metertracker.database.DatabaseAccessor.CSV_DATA_OPEN_REQUEST;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.database.DatabaseAccessor;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {
    private static final String TAG = "MainActivity";

    public DataAggregation selectedType = null;
    private FloatingActionButton fab;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(this);
        if (resultCode == RESULT_OK && requestCode == CSV_DATA_CREATE_REQUEST && data != null) {
            Uri uri = data.getData();
            DatabaseAccessor.exportDatabase(this, dbHelper, uri);
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.setType("text/csv");
            startActivity(Intent.createChooser(sharingIntent, "share file with"));
        } else if (resultCode == RESULT_OK && requestCode == CSV_DATA_OPEN_REQUEST && data != null) {
            Uri uri = data.getData();
            dbHelper.importDatabase(this, uri);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavHostFragment fragmentById = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (fragmentById != null) {
            NavController navController = fragmentById.getNavController();
            AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
            navController.addOnDestinationChangedListener(this);
        }
        this.fab = findViewById(R.id.floating_action_button);
        this.fab.setOnClickListener(v -> {
                    try {
                        if (fragmentById != null) {
                            fragmentById.getNavController().navigate(R.id.action_global_to_addMeasurementFragment);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
        );
        SharedPreferences main = getSharedPreferences("Main", Context.MODE_PRIVATE);
//        main.edit().putString("lastReminder", null).apply();
        String lastReminder = main.getString("lastReminder", null);
        Calendar cal = Calendar.getInstance();
        Log.e(TAG, "LastReminder: " + lastReminder);
        if (lastReminder == null || DateUtils.stringToDate(lastReminder).compareTo(cal.getTime()) <= 0) {
            final String title = "Reminder";
            final String text = "Note down the weekly values!";
            NotificationReceiver.cancelNotification(this, title, text);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 7);
            }
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Log.e(TAG, "Create Reminder at " + cal.getTime());
            main.edit().putString("lastReminder", DateUtils.dateToString(cal.getTime())).apply();
            NotificationReceiver.scheduleNotification(this, cal.getTimeInMillis(), title, text);
        }
//        new DataReaderDBHelper(this).updateUnit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_export) {
            DatabaseAccessor.createFileForExport(this);
        } else if (item.getItemId() == R.id.toolbar_import) {
            DatabaseAccessor.openFileForImport(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public NavController getNavController() {
        NavHostFragment fragmentById = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return fragmentById == null ? null : fragmentById.getNavController();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment fragmentById = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (fragmentById != null)
            fragmentById.getNavController().navigateUp();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        NavController navController = getNavController();
        if (navController != null && navController.getCurrentDestination().getId() != R.id.addMeasurementFragment) {
            menuInflater.inflate(R.menu.toolbar, menu);
        }
        return true;
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        invalidateOptionsMenu();
        findViewById(R.id.save_button).setVisibility(destination.getId() != R.id.addMeasurementFragment ? View.GONE : View.VISIBLE);
        if (this.fab != null) {
            if (destination.getId() == R.id.addMeasurementFragment) {
                this.fab.hide();
            } else {
                this.fab.show();
            }
        }
        if (destination.getId() == R.id.dashboardFragment) {
            this.selectedType = null;
        }
    }
}