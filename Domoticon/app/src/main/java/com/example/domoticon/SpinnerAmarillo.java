package com.example.domoticon;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerAmarillo extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public SpinnerAmarillo(Context context, String[] values) {
        super(context, android.R.layout.simple_spinner_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;

        if ("2".equals(values[position]) || "3".equals(values[position])) {
            textView.setTextColor(Color.YELLOW);
        }
        int padding = 30;
        textView.setPadding(padding, padding, padding, padding);

        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        if ("2".equals(values[position]) || "3".equals(values[position])) {
            textView.setTextColor(Color.YELLOW);
        }
        return view;
    }
}

