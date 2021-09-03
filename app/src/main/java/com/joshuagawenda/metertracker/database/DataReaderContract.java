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
                    DataEntry.COLUMN_NAME_ORDER + " INTEGER," +
                    DataEntry.COLUMN_NAME_DATE + " TEXT)";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;

    public static final ArrayList<Function<Cursor, Object>> conversions = new ArrayList<Function<Cursor, Object>>() {{
        add(cursor -> cursor.getInt(cursor.getColumnIndexOrThrow(DataEntry._ID)));
        add(cursor -> cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_TYPE)));
        add(cursor -> cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_UNIT)));
        add(cursor -> cursor.getFloat(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_VALUE)));
        add(cursor -> cursor.getInt(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_ORDER)));
        add(cursor -> DateUtils.stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_DATE))));
    }};

    public static final Function<Cursor, String[]> selectConversion = curCSV -> new String[]{
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry._ID)),
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_TYPE)),
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_UNIT)),
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_VALUE)),
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_ORDER)),
            curCSV.getString(curCSV.getColumnIndexOrThrow(DataEntry.COLUMN_NAME_DATE))
    };

    private DataReaderContract() {
    }

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_UNIT = "unit";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_ORDER = "entry_order";
        public static final String COLUMN_NAME_DATE = "date";
        public final int id;
        public String type;
        public String unit;
        public float value;
        public int order;
        public Date date;

        public DataEntry(int _ID, String type, String unit, float value, int order, Date date) {
            this.id = _ID;
            this.type = type;
            this.unit = unit;
            this.value = value;
            this.order = order;
            this.date = date;

        }

        public DataEntry(List<Object> row) {
            this(((int) row.get(0)), ((String) row.get(1)), ((String) row.get(2)), ((float) row.get(3)),((int) row.get(4)),((Date) row.get(5)));
        }

        @NonNull
        @Override
        public String toString() {
            return "DataEntry{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    ", unit='" + unit + '\'' +
                    ", value=" + value +
                    ", order=" + order +
                    ", date=" + date +
                    '}';
        }
    }
}
