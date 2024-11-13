package com.example.skyrivals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class FavoritosViewModel extends ViewModel {
    private MutableLiveData<List<Avion>> avionesFavoritos = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Avion>> getAvionesFavoritos() {
        return avionesFavoritos;
    }

    public void addFavorito(Avion avion) {
        List<Avion> currentFavoritos = avionesFavoritos.getValue();
        if (currentFavoritos != null && !currentFavoritos.contains(avion)) {
            currentFavoritos.add(avion);
            avionesFavoritos.setValue(currentFavoritos);
        }
    }

    public void removeFavorito(Avion avion) {
        List<Avion> currentFavoritos = avionesFavoritos.getValue();
        if (currentFavoritos != null) {
            currentFavoritos.remove(avion);
            avionesFavoritos.setValue(currentFavoritos);
        }
    }
}

