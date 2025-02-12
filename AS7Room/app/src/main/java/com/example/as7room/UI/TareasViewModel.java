package com.example.as7room.UI;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.as7room.Model.Tarea;
import com.example.as7room.Model.TareasRepositorio;

import java.util.List;

public class TareasViewModel extends AndroidViewModel {

    private final TareasRepositorio repositorio;
    private final LiveData<List<Tarea>> todasLasTareas;

    public TareasViewModel(Application aplicacion) {
        super(aplicacion);
        repositorio = new TareasRepositorio(aplicacion);
        todasLasTareas = repositorio.obtenerTodasLasTareas();
    }

    public void insertar(Tarea tarea) {
        repositorio.insertar(tarea);
    }

    public void actualizar(Tarea tarea) {
        repositorio.actualizar(tarea);
    }

    public void eliminar(Tarea tarea) {
        repositorio.eliminar(tarea);
    }

    public LiveData<List<Tarea>> obtenerTodasLasTareas() {
        return todasLasTareas;
    }
}
