// Author: Álvaro Pereira
// Date: 24-02-2025 (modified 03-03-2025)

package com.example.chinagram.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.chinagram.R;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "chinagram_prefs"; // Nombre del archivo de preferencias compartidas
    private static final String KEY_EMAIL = "key_email";        // Correo del usuario
    private static final String KEY_RECUERDAME = "key_recuerdame"; // Estado del "Recuérdame"
    private static final String KEY_THEME = "key_theme";       // Tema de la app (claro/oscuro)
    private static final String KEY_ACCESS_TOKEN = "key_imgur_access_token"; // Token de acceso de Imgur
    private static final String KEY_REFRESH_TOKEN = "key_imgur_refresh_token"; // Token de refresco de Imgur

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) { // Constructor
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarLogin(String email, boolean rememberMe) { // Guardo el correo y el estado del checkbox "Recuérdame"
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_RECUERDAME, rememberMe);
        editor.apply();
    }

    public String recuperarEmail() { // Recupero el correo guardado, devuelve null si no existe
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public boolean recuerdameOn() { // Verifico si el checkbox "Recuérdame" está activado, falso por defecto
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

    public void limpiarLogin() { // Elimino las preferencias relacionadas con el login
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_RECUERDAME);
        editor.apply();
    }

    public void limpiarTodo() { // Elimino todas las preferencias, incluyendo el tema y tokens
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Metodo para manejar tokens de Imgur
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

    public String recuperarAccessToken() { // Recupero el accessToken, devuelvo null si no existe
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String recuperarRefreshToken() { // Recupero el refreshToken, devuelvo null si no existe
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public void limpiarImgurTokens() { // Elimino solo los tokens de Imgur
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }
}