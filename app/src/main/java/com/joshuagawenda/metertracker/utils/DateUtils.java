package com.joshuagawenda.metertracker.utils;

import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static int getFromDate(Date date, int field){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(field);
    }
    public static int getYear(Date date) {
        return getFromDate(date, Calendar.YEAR);
    }
    public static int getMonth(Date date) {
        return getFromDate(date, Calendar.MONTH);
    }
    public static int getDay(Date date) {
        return getFromDate(date, Calendar.DAY_OF_YEAR);
    }

    public static long getDaysBetween(Date a, Date b) {
        return TimeUnit.MILLISECONDS.toDays(b.getTime() - a.getTime());
    }

    public static String dateToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return simpleDateFormat.format(date);
    }
    public static String dateToString(Date date) {
        return dateToString(date, "dd/MM/yyyy");
    }

    public static Date stringToDate(String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return simpleDateFormat.parse(string);
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static void displayDatePicker(FragmentManager fm, Date startDate, String title,
                                         Consumer<Date> positiveListener,
                                         Runnable negativeListener) {
        Calendar cal = Calendar.getInstance();

        // Preserve hours and minutes of day
        cal.setTime(startDate);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(startDate.getTime())
                .setTitleText(title)
                .build();

        if (negativeListener != null)
            datePicker.addOnNegativeButtonClickListener(v -> negativeListener.run());
        datePicker.addOnPositiveButtonClickListener(selection -> {
            cal.setTime(new Date(selection));
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            positiveListener.accept(cal.getTime());
        });

        datePicker.show(fm, "DatePicker");
    }

    public static void displayTimePicker(FragmentManager fm, Date startTime, String title,
                                         Consumer<Date> positiveListener,
                                         Runnable negativeListener) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .setTitleText(title)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        if (negativeListener != null)
            timePicker.addOnNegativeButtonClickListener(v -> negativeListener.run());
        timePicker.addOnPositiveButtonClickListener(v -> {
            cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            cal.set(Calendar.MINUTE, timePicker.getMinute());
            positiveListener.accept(cal.getTime());
        });

        timePicker.show(fm, timePicker.toString());
    }
}
