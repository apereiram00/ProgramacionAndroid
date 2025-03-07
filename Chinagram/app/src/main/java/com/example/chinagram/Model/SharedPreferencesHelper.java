package com.example.chinagram.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "chinagram_prefs";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_RECUERDAME = "key_recuerdame";
    private static final String KEY_THEME = "key_theme";
    private static final String KEY_ACCESS_TOKEN = "key_imgur_access_token";
    private static final String KEY_REFRESH_TOKEN = "key_imgur_refresh_token";
    private static final String KEY_REELS_LIKES = "key_reels_likes"; // Nueva clave para los "Me gusta"

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Métodos existentes (sin cambios)
    public void guardarLogin(String email, boolean rememberMe) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_RECUERDAME, rememberMe);
        editor.apply();
    }

    public String recuperarEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public boolean recuerdameOn() {
        return sharedPreferences.getBoolean(KEY_RECUERDAME, false);
    }

    public void guardarTema(int themeMode) {
        Log.d("SharedPreferencesHelper", "Guardando tema: " + (themeMode == AppCompatDelegate.MODE_NIGHT_YES ? "Oscuro" : "Claro"));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, themeMode);
        editor.apply();
    }

    public int recuperarTema() {
        int themeMode = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        Log.d("SharedPreferencesHelper", "Recuperando tema: " + (themeMode == AppCompatDelegate.MODE_NIGHT_YES ? "Oscuro" : "Claro"));
        return themeMode;
    }

    public void limpiarLogin() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_RECUERDAME);
        editor.apply();
    }

    public void limpiarTodo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void guardarImgurTokens(String accessToken, String refreshToken) {
        Log.d("SharedPreferencesHelper", "Guardando tokens de Imgur: accessToken=" + (accessToken != null ? "presente" : "null") + ", refreshToken=" + (refreshToken != null ? "presente" : "null"));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (accessToken != null) {
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
        } else {
            editor.remove(KEY_ACCESS_TOKEN);
        }
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_REFRESH_TOKEN);
        }
        editor.apply();
    }

    public String recuperarAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String recuperarRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public void limpiarImgurTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }

    // Nuevos métodos para los "Me gusta" de los reels
    public void guardarReelsLikes(List<Boolean> isLikedList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String likesString = String.join(",", isLikedList.toString()
                .replace("[", "")  // Eliminar corchetes
                .replace("]", "")
                .replace(" ", "")); // Convertir a formato "true,false,false,..."
        editor.putString(KEY_REELS_LIKES, likesString);
        editor.apply();
        Log.d("SharedPreferencesHelper", "Guardando likes: " + likesString);
    }

    public List<Boolean> recuperarReelsLikes(int size) {
        String likesString = sharedPreferences.getString(KEY_REELS_LIKES, null);
        List<Boolean> isLikedList = new ArrayList<>();

        if (likesString != null && !likesString.isEmpty()) {
            String[] likesArray = likesString.split(",");
            for (String value : likesArray) {
                isLikedList.add(Boolean.parseBoolean(value.trim()));
            }
            // Asegurar que la lista tenga el tamaño correcto (por si cambió el número de reels)
            while (isLikedList.size() < size) {
                isLikedList.add(false);
            }
            if (isLikedList.size() > size) {
                isLikedList = isLikedList.subList(0, size);
            }
        } else {
            // Si no hay datos guardados, inicializar con false
            for (int i = 0; i < size; i++) {
                isLikedList.add(false);
            }
        }

        Log.d("SharedPreferencesHelper", "Recuperando likes: " + isLikedList);
        return isLikedList;
    }
}