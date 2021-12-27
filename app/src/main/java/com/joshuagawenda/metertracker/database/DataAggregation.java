package com.joshuagawenda.metertracker.database;

import androidx.annotation.NonNull;

public class DataAggregation {
    public final String type;
    public final String unit;
    public final int order;
    public final boolean isHigherPositive;
    public final String lastDate;
    public final float lastWeek;
    public final float average;
    public final float monthlyDifference;

    public String getType() {
        return type;
    }

    public int getOrder() {
        return order;
    }

    public DataAggregation(String type, String unit, int order, boolean isHigherPositive, String lastDate, float lastWeek, float average, float monthlyDifference) {
        this.type = type;
        this.unit = unit;
        this.order = order;
        this.isHigherPositive = isHigherPositive;
        this.lastDate = lastDate;
        this.lastWeek = lastWeek;
        this.average = average;
        this.monthlyDifference = monthlyDifference;
    }

    @NonNull
    @Override
    public String toString() {
        return "DataAggregation{" +
                "type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", order=" + order +
                ", isHigherPositive=" + isHigherPositive +
                ", lastDate=" + lastDate +
                ", lastWeek=" + lastWeek +
                ", averageMonth=" + average +
                ", monthlyDifference=" + monthlyDifference +
                '}';
    }
}
