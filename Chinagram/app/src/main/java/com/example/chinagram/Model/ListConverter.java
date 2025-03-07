package com.example.chinagram.Model;

import androidx.room.TypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListConverter {
    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) return "";
        return String.join(",", list);
    }

    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(data.split(",")));
    }
}