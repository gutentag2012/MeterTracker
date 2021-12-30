package com.joshuagawenda.metertracker;

import static com.joshuagawenda.metertracker.utils.DateUtils.getFromDate;
import static com.joshuagawenda.metertracker.utils.DateUtils.getMonth;
import static com.joshuagawenda.metertracker.utils.DateUtils.getYear;
import static java.util.stream.Collectors.toList;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.joshuagawenda.metertracker.database.DataAggregation;
import com.joshuagawenda.metertracker.database.DataReaderContract.DataEntry;
import com.joshuagawenda.metertracker.database.DataReaderDBHelper;
import com.joshuagawenda.metertracker.utils.DateUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"ConstantConditions", "unused"})
public class ChartFragment extends Fragment {

    private static final String TAG = "ChartFragment";
    Integer[] colorIds = new Integer[]{
            R.color.chart_7,
            R.color.chart_6,
            R.color.chart_5,
            R.color.chart_4,
            R.color.chart_3,
            R.color.chart_2,
            R.color.chart_1,
            R.color.chart_0,
    };
    Integer[] colorIdsTransparent = new Integer[]{
            R.color.chart_7_transparent,
            R.color.chart_6_transparent,
            R.color.chart_5_transparent,
            R.color.chart_4_transparent,
            R.color.chart_3_transparent,
            R.color.chart_2_transparent,
            R.color.chart_1_transparent,
            R.color.chart_0_transparent,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ArrayDeque<Integer> colors = Arrays.stream(colorIds)
                .map(e -> ContextCompat.getColor(requireContext(), e))
                .collect(Collectors.toCollection(ArrayDeque::new));
        Calendar cal = Calendar.getInstance();
        DataAggregation type = ((MainActivity) getActivity()).selectedType;
        DataReaderDBHelper dbHelper = new DataReaderDBHelper(requireContext());
        List<DataEntry> dataEntries = type == null ? dbHelper.selectAll() : dbHelper.selectAll(type.type, type.unit);

        LineChart lineChart = view.findViewById(R.id.line_chart);
        BarChart barChart = view.findViewById(R.id.bar_chart);
        barChart.setVisibility(View.GONE);
        setupLineChart(lineChart, colors, cal, type, dataEntries);
//        setupBarChart(barChart, colors, cal, type, dataEntries);
        return view;
    }

    private void setupLineChart(LineChart lineChart, ArrayDeque<Integer> colors, Calendar cal, DataAggregation type, @NonNull List<DataEntry> dataEntries) {
        List<ILineDataSet> typeMap = dataEntries.stream()
                .collect(Collectors.groupingBy(e -> getYear(e.date)))
                .entrySet()
                .stream()
                .map(e -> {
                    List<DataEntry> value = e.getValue();
                    List<Entry> entryList = IntStream.range(1, value.size())
                            .mapToObj(i -> new DataEntry[]{value.get(i - 1), value.get(i)})
                            .map(ee -> {
                                Date date = ee[0].date;
                                cal.setTimeInMillis(0L);
                                int month = getMonth(date);
                                int fromDate = getFromDate(date, Calendar.WEEK_OF_MONTH);
                                cal.set(Calendar.MONTH, month);
                                cal.set(Calendar.WEEK_OF_MONTH, fromDate);
                                float timeInMillis = cal.getTimeInMillis();
                                float value_diff = ee[0].value - ee[1].value;
                                return new Entry(timeInMillis, value_diff);
                            })
                            .sorted(new EntryXComparator())
                            .collect(toList());
                    LineDataSet lineDataSet = new LineDataSet(entryList, "Year " + e.getKey());
                    lineDataSet.setValueTextColor(Color.WHITE);
                    Integer popped_color = colors.pollFirst();
                    lineDataSet.setCircleColor(popped_color);
                    lineDataSet.setCircleHoleColor(R.color.transparent);
                    lineDataSet.setColor(popped_color);
                    lineDataSet.setLineWidth(2f);
//                    lineDataSet.setFillDrawable(ContextCompat.getDrawable(requireContext(), colorIdsTransparent[colorIds.length - colors.size() - 1]));
                    lineDataSet.setMode(LineDataSet.Mode.STEPPED);
//                    lineDataSet.setDrawCircles(false);
//                    lineDataSet.setDrawFilled(true);
//                    lineDataSet.setDrawValues(true);
                    return lineDataSet;
                })
                .collect(toList());
        LineData data = new LineData(typeMap);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DateUtils.dateToString(new Date((long) value), "MMMM");
            }
        });
        xAxis.setLabelCount(6);
        xAxis.setTextColor(Color.WHITE);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%s %s", value, type == null ? "No UNIT" : type.unit);
            }
        });
        yAxis.setTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getDescription().setEnabled(false);
        lineChart.setMaxVisibleValueCount(30);
        lineChart.setScaleMinima(1.5f, 1f);
        lineChart.setScaleYEnabled(false);
        lineChart.setBorderColor(Color.WHITE);
        // TODO Add selected marker popup: https://github.com/PhilJay/MPAndroidChart/wiki/IMarker-Interface
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private void setupBarChart(BarChart barChart, ArrayDeque<Integer> colors, Calendar cal, DataAggregation type, @NonNull List<DataEntry> dataEntries) {
        float start = 0f;
        List<IBarDataSet> typeMap = dataEntries.stream()
                .collect(Collectors.groupingBy(e -> getYear(e.date)))
                .entrySet()
                .stream()
                .map(e -> {
                    List<DataEntry> value = e.getValue();
                    List<BarEntry> entryList = IntStream.range(1, value.size())
                            .mapToObj(i -> new DataEntry[]{value.get(i - 1), value.get(i)})
                            .map(ee -> {
                                Date date = ee[0].date;
                                cal.setTimeInMillis(0L);
                                int month = getMonth(date);
                                int fromDate = getFromDate(date, Calendar.WEEK_OF_MONTH);
                                cal.set(Calendar.MONTH, month);
                                cal.set(Calendar.WEEK_OF_MONTH, fromDate);
                                float timeInMillis = cal.getTimeInMillis();
                                float value_diff = ee[0].value - ee[1].value;
                                return new BarEntry(timeInMillis, value_diff);
                            })
                            .sorted(new EntryXComparator())
                            .collect(toList());
                    BarDataSet barDataSet = new BarDataSet(entryList, "Year " + e.getKey());
                    barDataSet.setValueTextColor(Color.WHITE);
                    Integer popped_color = colors.pop();
                    barDataSet.setColor(popped_color);
                    return barDataSet;
                })
                .collect(toList());
        BarData data = new BarData(typeMap);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DateUtils.dateToString(new Date((long) value), "MMMM");
            }
        });
        xAxis.setLabelCount(6);
        xAxis.setTextColor(Color.WHITE);
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%s %s", value, type == null ? "No UNIT" : type.unit);
            }
        });
        yAxis.setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(Color.WHITE);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(30);
        barChart.setScaleMinima(1.5f, 1f);
        barChart.setScaleYEnabled(false);
        barChart.setBorderColor(Color.WHITE);
        data.setBarWidth(6E8f);
        barChart.setFitBars(true);
        barChart.setData(data);
        barChart.groupBars(0f, 2E8f, 1E7f);
        barChart.invalidate();
    }
}