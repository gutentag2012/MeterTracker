package com.joshuagawenda.metertracker.database;

import static com.joshuagawenda.metertracker.database.DataReaderContract.DataEntry.TABLE_NAME;
import static com.joshuagawenda.metertracker.database.DataReaderContract.SQL_CREATE_ENTRIES;
import static com.joshuagawenda.metertracker.database.DataReaderContract.SQL_DELETE_ENTRIES;
import static com.joshuagawenda.metertracker.database.DataReaderContract.SQL_UPDATE_ENTRIES_V4_TO_V5;
import static com.joshuagawenda.metertracker.database.DataReaderContract.selectConversion;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import com.google.android.material.snackbar.Snackbar;
import com.joshuagawenda.metertracker.MainActivity;
import com.joshuagawenda.metertracker.R;
import com.joshuagawenda.metertracker.utils.CSVWriter;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataReaderDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 5;
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL(SQL_UPDATE_ENTRIES_V4_TO_V5);
        } else {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
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
                if (entry == null)
                    continue;
                ContentValues values = new ContentValues();
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_TYPE, entry.type);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_UNIT, entry.unit);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_VALUE, entry.value);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_ORDER, entry.order);
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_DATE, DateUtils.dateToString(entry.date));
                values.put(DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE, entry.isHigherPositive);
                db.insert(TABLE_NAME, null, values);
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
                TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                "substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 7, 4) DESC, substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 4, 2) DESC, substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 1, 2) DESC"           // The sort order
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

    public List<DataReaderContract.DataEntry> selectAll(String type, String unit) {
        return this.selectAll(type, unit, 0);
    }

    public List<DataReaderContract.DataEntry> selectAll(String type, String unit, int limit) {
        SQLiteDatabase db = getReadableDatabase();
        // Filter results WHERE "title" = 'My Title'
        // String selection = DataReaderContract.DataEntry.COLUMN_NAME_TITLE + " = ?";
        // String[] selectionArgs = { "My Title" };

        try (Cursor cursor = db.query(
                TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                DataReaderContract.DataEntry.COLUMN_NAME_TYPE + "= ? and " + DataReaderContract.DataEntry.COLUMN_NAME_UNIT + " = ?",              // The columns for the WHERE clause
                new String[]{type, unit},          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                "substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 7, 4) DESC, substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 4, 2) DESC, substr(" + DataReaderContract.DataEntry.COLUMN_NAME_DATE + ", 1, 2) DESC",
                limit <= 0 ? null : "" + limit
        )) {
            List<DataReaderContract.DataEntry> rows = new ArrayList<>();
            while (cursor.moveToNext()) {
                List<Object> objects = new ArrayList<>();
                for (Function<Cursor, Object> conversion : DataReaderContract.conversions) {
                    objects.add(conversion.apply(cursor));
                }
                rows.add(new DataReaderContract.DataEntry(objects));
            }
            Collections.reverse(rows);
            List<DataReaderContract.DataEntry> toRemove = new ArrayList<>();
            for (DataReaderContract.DataEntry entry : rows) {
                if (entry.value != 0) {
                    break;
                }
                toRemove.add(entry);
            }
            rows.removeAll(toRemove);
            Collections.reverse(rows);
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
            CSVWriter csvWrite = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            Cursor curCSV = getReadableDatabase().rawQuery(String.format("SELECT * FROM %s", TABLE_NAME), null);
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

    public void importDatabase(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resolver.openInputStream(uri), StandardCharsets.UTF_8))) {
            DataReaderContract.DataEntry[] dataEntries = reader.lines().skip(1).map(line -> {
                String[] split = Arrays.stream(line.split(","))
                        .map(e -> e.replaceAll("\"", ""))
                        .toArray(String[]::new);
                try {
                    int reduce = split.length == 7 ? 0 : 1;
                    return new DataReaderContract.DataEntry(
                            0,
                            split[1 - reduce],
                            split[2 - reduce],
                            Float.parseFloat(split[3 - reduce]),
                            Integer.parseInt(split[4 - reduce]),
                            DateUtils.stringToDate(split[5 - reduce]),
                            !split[6 - reduce].equals("0"));
                } catch (RuntimeException e) {
                    try {
                        int reduce = split.length == 5 ? 0 : 1;
                        return new DataReaderContract.DataEntry(
                                0,
                                split[1 - reduce],
                                split[2 - reduce],
                                Float.parseFloat(split[3 - reduce]),
                                -1,
                                DateUtils.stringToDate(split[4 - reduce]));
                    } catch (RuntimeException ee) {
                        Log.e(TAG, "importDatabase: " + e);
                        return null;
                    }
                }
            }).toArray(DataReaderContract.DataEntry[]::new);
            clear();
            insertAll(dataEntries);
            if (Arrays.stream(dataEntries).anyMatch(Objects::isNull)) {
                Snackbar.make(
                        ((MainActivity) context).findViewById(R.id.root),
                        "An error occurred while importing the data!",
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            Log.e(TAG, "onActivityResult: " + e);
        }
    }

    public List<DataAggregation> aggregate() {
        List<DataAggregation> aggregations = new ArrayList<>();
        Map<String, List<DataReaderContract.DataEntry>> collect = selectAll().stream().collect(Collectors.groupingBy(e -> e.type + ";" + e.unit + ";" + e.isHigherPositive));
        collect.forEach((key, data) -> {
            // Get all the differences (middle value in a row in frontend) between the single weekly values in the list
            double[] differences = IntStream.range(0, data.size())
                    .mapToDouble(i -> {
                        DataReaderContract.DataEntry current = data.get(i);
                        DataReaderContract.DataEntry last = i + 1 < data.size() ? data.get(i + 1) : null;
                        if (last == null) return 0;
                        return current.value - last.value;
                    }).toArray();
            // Average of the last 4 Weeks differences
            double averageCurrent = Arrays.stream(differences).skip(1).limit(4).average().orElse(0);
            // Deviation of the current difference to the average
            double deviation = ((differences[0] - averageCurrent) / (averageCurrent + 0.0f)) * 100.0f;
            if (Double.isInfinite(deviation)) {
                deviation = 100;
            }
            if (Double.isNaN(deviation)) {
                deviation = 0;
            }

            String[] split = key.split(";");
            String type = split[0];
            String unit = split[1];
            boolean isHigherPositive = Boolean.parseBoolean(split[2]);
            aggregations.add(new DataAggregation(
                    type,
                    unit,
                    data.size() > 0 ? data.get(0).order : -1,
                    isHigherPositive,
                    data.size() > 0 ? DateUtils.dateToString(data.get(0).date) : "-",
                    data.size() > 1 ? data.get(0).value - data.get(1).value : 0,
                    ((float) averageCurrent),
                    (float) deviation,
                    data.get(0).value
            ));
        });
        aggregations.sort(Comparator.comparing(DataAggregation::getOrder).thenComparing(DataAggregation::getType));
        return aggregations;
    }

    public void clear() {
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
        getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
    }

    public List<String[]> getAllTypes() {
        SQLiteDatabase db = getReadableDatabase();
        List<String[]> entries = new ArrayList<>();
        try (Cursor cursor = db.query(
                true,
                TABLE_NAME,
                new String[]{
                        DataReaderContract.DataEntry.COLUMN_NAME_TYPE,
                        DataReaderContract.DataEntry.COLUMN_NAME_UNIT,
                        DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE,
                        DataReaderContract.DataEntry.COLUMN_NAME_ORDER
                },
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DataReaderContract.DataEntry.COLUMN_NAME_TYPE));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(DataReaderContract.DataEntry.COLUMN_NAME_UNIT));
                boolean isHigherPositive = cursor.getInt(cursor.getColumnIndexOrThrow(DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE)) != 0;
                int order = cursor.getInt(cursor.getColumnIndexOrThrow(DataReaderContract.DataEntry.COLUMN_NAME_ORDER));
                List<DataReaderContract.DataEntry> oldEntries = this.selectAll(type, unit, 2);
                float oldValue = oldEntries.size() > 1 && oldEntries.get(0).value == 0
                        ? oldEntries.get(1).value
                        : oldEntries.size() > 0
                        ? oldEntries.get(0).value
                        : 0f;
                entries.add(new String[]{type, unit, String.valueOf(order), String.valueOf(oldValue), String.valueOf(isHigherPositive)});
            }
            return entries;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public void delete(DataReaderContract.DataEntry entry) {
        getWritableDatabase()
                .delete(TABLE_NAME, BaseColumns._ID + "= ?", new String[]{String.valueOf(entry.id)});
    }

    public void update(DataReaderContract.DataEntry dataEntry, SQLiteDatabase writableDatabase) {
        if (writableDatabase == null)
            writableDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_TYPE, dataEntry.type); //These Fields should be your String values of actual column names
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_UNIT, dataEntry.unit);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_VALUE, dataEntry.value);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_ORDER, dataEntry.order);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_DATE, DateUtils.dateToString(dataEntry.date));
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE, dataEntry.isHigherPositive);
        writableDatabase.update(TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(dataEntry.id)});
    }

    public void updateFromAggregation(DataAggregation oldAgg, DataAggregation newAgg) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_TYPE, newAgg.type);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_UNIT, newAgg.unit);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_ORDER, newAgg.order);
        cv.put(DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE, newAgg.isHigherPositive);
        String whereClause = DataReaderContract.DataEntry.COLUMN_NAME_TYPE + "=? AND " +
                DataReaderContract.DataEntry.COLUMN_NAME_UNIT + "=? AND " +
                DataReaderContract.DataEntry.COLUMN_NAME_ORDER + "=? AND " +
                DataReaderContract.DataEntry.COLUMN_NAME_POSITIVE + "=?";
        String[] whereArgs = {oldAgg.type, oldAgg.unit, String.valueOf(oldAgg.order), oldAgg.isHigherPositive ? "1" : "0"};
        writableDatabase.update(TABLE_NAME, cv, whereClause, whereArgs);
    }

    public void updateOrder(String type, String unit, int i) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            List<DataReaderContract.DataEntry> dataEntries = selectAll(type, unit);
            for (DataReaderContract.DataEntry e : dataEntries) {
                e.order = i;
                update(e, db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
