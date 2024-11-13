package com.example.skyrivals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DetallesViewModel extends ViewModel {
    private final MutableLiveData<Avion> avion = new MutableLiveData<>();

    public LiveData<Avion> getAvion() {
        return avion;
    }

    public void setAvion(Avion avion) {
        this.avion.setValue(avion);
    }
}
