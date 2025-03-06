package com.example.chinagram.utils;

import android.util.Log;
import androidx.annotation.DrawableRes;
import com.example.chinagram.R;

public class DrawableUtils {

    private static final String TAG = "DrawableUtils";

    // Mapa de URLs de drawables a IDs de recursos
    private static final String[] DRAWABLE_URLS = {
            "drawable://xi_jinping",
            "drawable://panda",
            "drawable://china_flag",
            "drawable://panda_feed",
            "drawable://rice_field",
            "drawable://factory"
    };

    // Método para obtener un recurso drawable a partir de una URL
    @DrawableRes
    public static int getResourceFromDrawable(String drawableUrl) {
        Log.d(TAG, "Cargando drawable: " + drawableUrl);
        if (drawableUrl == null) {
            return R.drawable.placeholder;
        }
        switch (drawableUrl) {
            case "drawable://xi_jinping":
                return R.drawable.xi_jinping;
            case "drawable://panda":
                return R.drawable.panda;
            case "drawable://china_flag":
                return R.drawable.china_flag;
            case "drawable://panda_feed":
                return R.drawable.panda_feed;
            case "drawable://rice_field":
                return R.drawable.rice_field;
            case "drawable://factory":
                return R.drawable.factory;
            default:
                return R.drawable.placeholder;
        }
    }

    public static String getRandomDrawableUrl() {
        java.util.Random random = new java.util.Random();
        String photoUrl = DRAWABLE_URLS[random.nextInt(DRAWABLE_URLS.length)];
        Log.d(TAG, "Foto aleatoria seleccionada: " + photoUrl);
        return photoUrl;
    }
}