package com.joshuagawenda.metertracker.database;

import static com.joshuagawenda.metertracker.database.DataReaderContract.SQL_CREATE_ENTRIES;
import static com.joshuagawenda.metertracker.database.DataReaderContract.SQL_DELETE_ENTRIES;
import static com.joshuagawenda.metertracker.database.DataReaderContract.conversions;
import static com.joshuagawenda.metertracker.database.DataReaderContract.selectConversion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import com.joshuagawenda.metertracker.utils.CSVWriter;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class DataReaderDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "DataReader.db";
    private static final String TAG = "DataReaderDBHelper";

    public DataReaderDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertAll(DataReaderContract.DataEntry... entries) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (DataReaderContract.DataEntry entry : entries) {
                ContentValues values = new ContentValues();
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_TYPE, entry.type);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_UNIT, entry.unit);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_VALUE, entry.value);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_DATE, DateUtils.dateToString(entry.date));
                db.insert(DataReaderContract.DataEntry.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<DataReaderContract.DataEntry> selectAll() {
        SQLiteDatabase db = getReadableDatabase();
        // Filter results WHERE "title" = 'My Title'
        // String selection = DataReaderContract.DataEntry.COLUMN_NAME_TITLE + " = ?";
        // String[] selectionArgs = { "My Title" };

        try (Cursor cursor = db.query(
                DataReaderContract.DataEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                DataReaderContract.DataEntry.COLUMN_NAME_DATE + " DESC"               // The sort order
        )) {
            List<DataReaderContract.DataEntry> rows = new ArrayList<>();
            while (cursor.moveToNext()) {
                List<Object> objects = new ArrayList<>();
                for (Function<Cursor, Object> conversion : DataReaderContract.conversions) {
                    objects.add(conversion.apply(cursor));
                }
                rows.add(new DataReaderContract.DataEntry(objects));
            }
            return rows;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public File exportDBToTemp(Context context) {
        File exportDir = new File(context.getCacheDir(), "");
        if (!exportDir.exists()) {
            Log.e(TAG, "exportDB: failed because no valid file was supplied");
            exportDir.mkdir();
        }

        File file = new File(exportDir, "exportDB.csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = getReadableDatabase().rawQuery(String.format("SELECT * FROM %s", DataReaderContract.DataEntry.TABLE_NAME), null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                csvWrite.writeNext(selectConversion.apply(curCSV));
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("DATA_READER_DATABASE", sqlEx.getMessage(), sqlEx);
        }
        return file;
    }

    public List<DataAggregation> aggregate() {
        List<DataAggregation> aggregations = new ArrayList<>();
        Map<String, List<DataReaderContract.DataEntry>> collect = selectAll().stream().collect(Collectors.groupingBy(e -> e.type + ";" + e.unit));
        collect.forEach((key, data) -> {
            double sum = data.stream()
                    .limit(4)
                    .mapToDouble(e -> e.value)
                    .sum();
            double sumLastMonth = data.stream()
                    .skip(4)
                    .limit(4)
                    .mapToDouble(e -> e.value)
                    .sum();
            double difference = (sum - sumLastMonth) / sumLastMonth * 100;
            float average = ((float) sum) / Math.min(data.size(), 4);

            String[] split = key.split(";");
            String type = split[0];
            String unit = split[1];
            aggregations.add(new DataAggregation(
                    type,
                    unit,
                    data.size() > 1 ? data.get(1).value : 0,
                    average,
                    data.size() >= 8 ? (float) difference : 0));
        });
        return aggregations;
    }

    public void clear() {
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
        getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
    }
}
