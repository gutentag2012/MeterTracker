package com.joshuagawenda.metertracker.utils;

import com.joshuagawenda.metertracker.database.DataReaderContract;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    public static <T> List<Integer> getIndexesInserted(List<T> original, List<T> added) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < added.size(); i++) {
            T entry = added.get(i);
            if(!original.contains(entry))
                indexes.add(i);
        }
        return indexes;
    }
    public static <T> List<Integer> getIndexesRemoved(List<T> original, List<T> added) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            T entry = original.get(i);
            if(!added.contains(entry))
                indexes.add(i);
        }
        return indexes;
    }
}
