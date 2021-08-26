package com.joshuagawenda.metertracker.database;

public class DataAggregation {
    public final String type;
    public final String unit;
    public final float lastWeek;
    public final float lastMonth;
    public final float monthlyDifference;

    public DataAggregation(String type, String unit, float lastWeek, float lastMonth, float monthlyDifference) {
        this.type = type;
        this.unit = unit;
        this.lastWeek = lastWeek;
        this.lastMonth = lastMonth;
        this.monthlyDifference = monthlyDifference;
    }

    @Override
    public String toString() {
        return "DataAggregation{" +
                "type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", lastWeek=" + lastWeek +
                ", lastMonth=" + lastMonth +
                ", monthlyDifference=" + monthlyDifference +
                '}';
    }
}
