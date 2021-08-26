package com.joshuagawenda.metertracker;

import static com.joshuagawenda.metertracker.database.DatabaseAccessor.CSV_DATA_REQUEST;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderContract;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.database.DatabaseAccessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CSV_DATA_REQUEST && data != null) {
            DataReaderDBHelper dbHelper = new DataReaderDBHelper(this);
            DatabaseAccessor.exportDatabase(this, dbHelper, data.getData());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(this);
//        DatabaseAccessor.dummyData(dbHelper);
        List<DataAggregation> aggregate = dbHelper.aggregate();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);
        return true;
    }
}