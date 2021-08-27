package com.joshuagawenda.metertracker.database;

import androidx.annotation.NonNull;

public class DataAggregation {
    public final String type;
    public final String unit;
    public final String lastDate;
    public final float lastWeek;
    public final float averageMonth;
    public final float monthlyDifference;

    public DataAggregation(String type, String unit, String lastDate, float lastWeek, float averageMonth, float monthlyDifference) {
        this.type = type;
        this.unit = unit;
        this.lastDate = lastDate;
        this.lastWeek = lastWeek;
        this.averageMonth = averageMonth;
        this.monthlyDifference = monthlyDifference;
    }

    @NonNull
    @Override
    public String toString() {
        return "DataAggregation{" +
                "type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", lastDate=" + lastDate +
                ", lastWeek=" + lastWeek +
                ", averageMonth=" + averageMonth +
                ", monthlyDifference=" + monthlyDifference +
                '}';
    }
}
