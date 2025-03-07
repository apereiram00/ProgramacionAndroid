package com.example.chinagram.utils;

import android.util.Log;
import androidx.annotation.DrawableRes;
import com.example.chinagram.R;

public class DrawableUtils {

    private static final String ETIQUETA = "DrawableUtils"; // Renombrado: TAG -> ETIQUETA

    // Mapa de URLs de drawables a IDs de recursos (sin cambio en el nombre del array porque es una constante técnica)
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
    public static int obtenerRecursoDesdeDrawable(String urlDrawable) { // Renombrado: getResourceFromDrawable -> obtenerRecursoDesdeDrawable
        Log.d(ETIQUETA, "Cargando drawable: " + urlDrawable);
        if (urlDrawable == null) {
            return R.drawable.placeholder;
        }
        switch (urlDrawable) {
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

    public static String obtenerUrlDrawableAleatoria() { // Renombrado: getRandomDrawableUrl -> obtenerUrlDrawableAleatoria
        java.util.Random random = new java.util.Random();
        String urlFoto = DRAWABLE_URLS[random.nextInt(DRAWABLE_URLS.length)]; // Renombrado: photoUrl -> urlFoto
        Log.d(ETIQUETA, "Foto aleatoria seleccionada: " + urlFoto);
        return urlFoto;
    }
}