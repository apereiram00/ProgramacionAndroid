// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "chinagram_prefs"; // Nombre del archivo de preferencias compartidas
    private static final String KEY_EMAIL = "key_email";        // Correo del usuario
    private static final String KEY_RECUERDAME = "key_recuerdame"; // Estado del "Recuérdame"
    private static final String KEY_THEME = "key_theme";       // Tema de la app (claro/oscuro)

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) { // Constructor: Inicializo SharedPreferences con el contexto y modo privado
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarLogin(String email, boolean rememberMe) { // Guardo el correo y el estado del checkbox "Recuérdame"
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);        // Almaceno el email
        editor.putBoolean(KEY_RECUERDAME, rememberMe); // Almaceno el estado del checkbox
        editor.apply();                            // Aplico cambios
    }

    public String recuperarEmail() { // Recupero el correo guardado, devuelve null si no existe
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public boolean recuerdameOn() { // Verifico si el checkbox "Recuérdame" está activado, falso por defecto
        return sharedPreferences.getBoolean(KEY_RECUERDAME, false);
    }

    public void guardarTema(int themeMode) {// Guardo el modo del tema (por ejemplo, MODE_NIGHT_YES o MODE_NIGHT_NO)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, themeMode);       // Almaceno el valor del tema
        editor.apply();                            // Aplica cambios
    }

    public int recuperarTema() { // Recupero el tema guardado, uso modo oscuro por defecto si no hay valor
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES);
    }

    public void limpiarLogin() { // Elimino las preferencias relacionadas con el login
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);                  // Borro el email
        editor.remove(KEY_RECUERDAME);             // Borraoel estado de "Recuérdame"
        editor.apply();                            // Aplico cambios
    }

    public void limpiarTodo() { // Elimino todas las preferencias, incluyendo el tema (esta función es un poco inútil)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();                            // Borro todo el contenido
        editor.apply();                            // Aplico cambios
    }
}