package com.joshuagawenda.metertracker.database;

import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public final class DataReaderContract {
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
                    DataEntry._ID + " INTEGER PRIMARY KEY," +
                    DataEntry.COLUMN_NAME_TYPE + " TEXT," +
                    DataEntry.COLUMN_NAME_UNIT + " TEXT," +
                    DataEntry.COLUMN_NAME_VALUE + " REAL," +
                    DataEntry.COLUMN_NAME_DATE + " TEXT)";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;

    public static final ArrayList<Function<Cursor, Object>> conversions = new ArrayList<Function<Cursor, Object>>() {{
        add(cursor -> cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_TYPE)));
        add(cursor -> cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_UNIT)));
        add(cursor -> cursor.getFloat(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_VALUE)));
        add(cursor -> DateUtils.stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_DATE))));
    }};

    public static final Function<Cursor, String[]> selectConversion = curCSV -> new String[]{
            curCSV.getString(0),
            curCSV.getString(1),
            curCSV.getString(2),
            curCSV.getString(3),
            curCSV.getString(4)
    };

    private DataReaderContract() {
    }

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_UNIT = "unit";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATE = "date";
        public String type;
        public String unit;
        public float value;
        public Date date;

        public DataEntry(String type, String unit, float value, Date date) {
            this.type = type;
            this.unit = unit;
            this.value = value;
            this.date = date;
        }

        public DataEntry(List<Object> row) {
            this.type = ((String) row.get(0));
            this.unit = ((String) row.get(1));
            this.value = ((float) row.get(2));
            this.date = ((Date) row.get(3));
        }

        @NonNull
        @Override
        public String toString() {
            return "DataEntry{" +
                    "type='" + type + '\'' +
                    ", unit='" + unit + '\'' +
                    ", value=" + value +
                    ", date=" + date +
                    '}';
        }
    }
}
