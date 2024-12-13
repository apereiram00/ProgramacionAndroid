package com.example.simpsonsapi.retrofit;

import com.example.simpsonsapi.model.Personajes;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface InterfazDeServicio {
    @GET("quotes")
    Call<List<Personajes>> obtenerPersonajes(@Query("count") int count);
}
