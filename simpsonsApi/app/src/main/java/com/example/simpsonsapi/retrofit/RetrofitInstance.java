package com.example.simpsonsapi.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Clase para gestionar la instancia de Retrofit
public class RetrofitInstance {
    private static final String BASE_URL = "https://thesimpsonsquoteapi.glitch.me/"; // Base URL de la API
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
