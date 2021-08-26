package com.joshuagawenda.metertracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String dateToString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return simpleDateFormat.format(date);
    }
    public static Date stringToDate(String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return simpleDateFormat.parse(string);
        } catch (ParseException ignored) {
            return new Date();
        }
    }
}
