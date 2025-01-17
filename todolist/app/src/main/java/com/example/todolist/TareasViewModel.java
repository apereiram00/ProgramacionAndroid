package com.example.todolist;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class TareasViewModel extends ViewModel {
    private final MutableLiveData<List<Tarea>> listaTareas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Tarea> tareaSeleccionada = new MutableLiveData<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Boolean> mostrarCompletadas = new MutableLiveData<>(false);
    public LiveData<List<Tarea>> getListaTareas() {
        return listaTareas;
    }

    public void agregarTarea(Tarea tarea, TareaCallback callback) {
        callback.onStart();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep((long) (1000 + Math.random() * 4000));
                List<Tarea> tareas = listaTareas.getValue();
                if (tareas != null) {
                    tareas.add(tarea);
                    Collections.sort(tareas, Comparator.comparingInt(Tarea::getPrioridad));
                    listaTareas.postValue(tareas);
                }
                callback.onSuccess();
            } catch (InterruptedException e) {
                callback.onError("Error al agregar tarea");
            }
        });
    }
    public void eliminarTarea(int posicion, TareaCallback callback) {
        callback.onStart();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep((long) (1000 + Math.random() * 4000));
                List<Tarea> tareas = listaTareas.getValue();
                if (tareas != null && posicion >= 0 && posicion < tareas.size()) {
                    tareas.remove(posicion);
                    listaTareas.postValue(tareas);
                    mainHandler.post(callback::onSuccess);
                } else {
                    mainHandler.post(() -> callback.onError("No se encontró la tarea a eliminar"));
                }
            } catch (InterruptedException e) {
                mainHandler.post(() -> callback.onError("Error al eliminar la tarea"));
            }
        });
    }


    public void editarTarea(int posicion, int nuevaPrioridad, String nuevaDescripcion, TareaCallback callback) {
        callback.onStart();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep((long) (1000 + Math.random() * 4000));
                List<Tarea> tareas = listaTareas.getValue();
                if (tareas != null && posicion >= 0 && posicion < tareas.size()) {
                    Tarea tarea = tareas.get(posicion);
                    tarea.setPrioridad(nuevaPrioridad);
                    tarea.setDescripcion(nuevaDescripcion);
                    listaTareas.postValue(tareas);
                    mainHandler.post(callback::onSuccess);
                } else {
                    mainHandler.post(() -> callback.onError("No se encontró la tarea a editar"));
                }
            } catch (InterruptedException e) {
                mainHandler.post(() -> callback.onError("Error al editar la tarea"));
            }
        });
    }

    public void setTareaSeleccionada(Tarea tarea) {
        tareaSeleccionada.setValue(tarea);
    }

    public LiveData<Tarea> getTareaSeleccionada() {
        return tareaSeleccionada;
    }

    public void setMostrarCompletadas(boolean mostrar) {
        mostrarCompletadas.setValue(mostrar);
    }
}
