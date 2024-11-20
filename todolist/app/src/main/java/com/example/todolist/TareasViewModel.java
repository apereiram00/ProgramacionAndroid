package com.example.todolist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class TareasViewModel extends ViewModel {
    private final MutableLiveData<List<Tarea>> listaTareas = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Tarea>> getListaTareas() {
        return listaTareas;
    }

    public void agregarTarea(Tarea tarea) {
        List<Tarea> tareas = listaTareas.getValue();
        if (tareas != null) {
            tareas.add(tarea);
            listaTareas.setValue(tareas);
        }
    }

    public void eliminarTarea(int posicion) {
        List<Tarea> tareas = listaTareas.getValue();
        if (tareas != null && posicion >= 0 && posicion < tareas.size()) {
            tareas.remove(posicion);
            listaTareas.setValue(tareas);
        }
    }

    public void editarTarea(int posicion, int nuevaPrioridad, String nuevaDescripcion) {
        List<Tarea> tareas = listaTareas.getValue();
        if (tareas != null && posicion >= 0 && posicion < tareas.size()) {
            Tarea tarea = tareas.get(posicion);
            tarea.setPrioridad(nuevaPrioridad);
            tarea.setDescripcion(nuevaDescripcion);
            listaTareas.setValue(tareas);
        }
    }
}

