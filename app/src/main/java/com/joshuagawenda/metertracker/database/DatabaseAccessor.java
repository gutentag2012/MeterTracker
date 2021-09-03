package com.joshuagawenda.metertracker.database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.joshuagawenda.metertracker.MainActivity;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class DatabaseAccessor {
    public static final int CSV_DATA_CREATE_REQUEST = 2;
    public static final int CSV_DATA_OPEN_REQUEST = 3;

    private DatabaseAccessor() {
    }

    public static void openFileForImport(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, CSV_DATA_OPEN_REQUEST);
    }

    public static void createFileForExport(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "database.csv");

        activity.startActivityForResult(intent, CSV_DATA_CREATE_REQUEST);
    }

    public static void exportDatabase(Context context, DataReaderDBHelper dbHelper, Uri outputUri) {
        File file = dbHelper.exportDBToTemp(context);
        try (InputStream is = new FileInputStream(file); OutputStream os = context.getContentResolver().openOutputStream(outputUri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dummyData(DataReaderDBHelper dbHelper) {
        dbHelper.insertAll(
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 45.6f, -1, DateUtils.stringToDate("16/08/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 42.4f, -1, DateUtils.stringToDate("09/08/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 35.32f, -1, DateUtils.stringToDate("02/08/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 55.78f, -1, DateUtils.stringToDate("25/07/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 43.3f, -1, DateUtils.stringToDate("18/07/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 47.1f, -1, DateUtils.stringToDate("11/07/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 42.5f, -1, DateUtils.stringToDate("03/06/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 51.3f, -1, DateUtils.stringToDate("27/06/2020")),
                new DataReaderContract.DataEntry(0, "Strom unten", "kwh", 36.94f, -1, DateUtils.stringToDate("20/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 105.3f, -1, DateUtils.stringToDate("16/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 102.76f, -1, DateUtils.stringToDate("09/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 114.91f, -1, DateUtils.stringToDate("02/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 117.3f, -1, DateUtils.stringToDate("25/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 108.7f, -1, DateUtils.stringToDate("18/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 111.78f, -1, DateUtils.stringToDate("11/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 90.5f, -1, DateUtils.stringToDate("03/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 106.7f, -1, DateUtils.stringToDate("27/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "liter", 104.4f, -1, DateUtils.stringToDate("20/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 12.3f, -1, DateUtils.stringToDate("16/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 32.76f, -1, DateUtils.stringToDate("09/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 37.91f, -1, DateUtils.stringToDate("02/08/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 28.3f, -1, DateUtils.stringToDate("25/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 33.7f, -1, DateUtils.stringToDate("18/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 29.78f, -1, DateUtils.stringToDate("11/07/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 31.5f, -1, DateUtils.stringToDate("03/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 34.7f, -1, DateUtils.stringToDate("27/06/2020")),
                new DataReaderContract.DataEntry(0, "Oben", "kwh", 26.4f, -1, DateUtils.stringToDate("20/06/2020"))
        );
    }
}
