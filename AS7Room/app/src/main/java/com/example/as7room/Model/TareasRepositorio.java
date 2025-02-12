package com.example.as7room.Model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TareasRepositorio {

    private final TareaDAO tareaDAO;

    private final Executor databaseExecutor;

    public TareasRepositorio(Application aplicacion) {
        TareasDatabase baseDeDatos = TareasDatabase.obtenerInstancia(aplicacion);
        tareaDAO = baseDeDatos.tareaDAO();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void insertar(Tarea tarea) {
        databaseExecutor.execute(() -> tareaDAO.insertar(tarea));
    }

    public void actualizar(Tarea tarea) {
        databaseExecutor.execute(() -> tareaDAO.actualizar(tarea));
    }

    public void eliminar(Tarea tarea) {
        databaseExecutor.execute(() -> tareaDAO.eliminar(tarea));
    }

    public LiveData<List<Tarea>> obtenerTodasLasTareas() {
        return tareaDAO.obtenerTodasLasTareas();
    }
}
