package com.example.simpsonsapi.UI.Listado;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.simpsonsapi.model.Personajes;
import com.example.simpsonsapi.retrofit.InterfazDeServicio;
import com.example.simpsonsapi.retrofit.RetrofitInstance;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonajeViewModel extends ViewModel {

    private MutableLiveData<List<Personajes>> personajesLiveData = new MutableLiveData<>();
    private boolean cargados = false; // Variable para verificar si los personajes ya fueron cargados

    public LiveData<List<Personajes>> getPersonajes() {
        return personajesLiveData;
    }

    public void cargarPersonajes(int count) {
        // Solo se cargan los personajes si no se han cargado previamente
        if (cargados) {
            return; // Si ya se cargaron no se vuelve a hacer la solicitud
        }

        InterfazDeServicio servicio = RetrofitInstance.getInstance().create(InterfazDeServicio.class);
        Call<List<Personajes>> call = servicio.obtenerPersonajes(count);

        call.enqueue(new Callback<List<Personajes>>() {
            @Override
            public void onResponse(Call<List<Personajes>> call, Response<List<Personajes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Personajes> personajesFiltrados = filtrarPersonajesUnicos(response.body());
                    personajesLiveData.setValue(personajesFiltrados);
                    cargados = true; // Manera cutre de comprobar que los personajes han sido cargados
                } else {
                    personajesLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Personajes>> call, Throwable t) {
                personajesLiveData.setValue(null);
            }
        });
    }

    private List<Personajes> filtrarPersonajesUnicos(List<Personajes> personajes) {
        Set<String> nombresUnicos = new HashSet<>();
        List<Personajes> personajesUnicos = new ArrayList<>();

        for (Personajes personaje : personajes) {
            if (nombresUnicos.add(personaje.getCharacter())) {
                personajesUnicos.add(personaje);
            }
        }
        return personajesUnicos;
    }
}



