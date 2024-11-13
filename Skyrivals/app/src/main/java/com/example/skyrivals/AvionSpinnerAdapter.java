package com.example.skyrivals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.skyrivals.databinding.SpinnerItemAvionBinding;

import java.util.List;

public class AvionSpinnerAdapter extends ArrayAdapter<Avion> {
    private final List<Avion> aviones; // Lista de aviones
    private final LayoutInflater inflater; // Inflador para las vistas

    public AvionSpinnerAdapter(Context context, List<Avion> aviones) {
        super(context, 0, aviones); // Llamada al constructor padre
        this.aviones = aviones; // Inicialización de la lista
        this.inflater = LayoutInflater.from(context); // Inicialización del inflater
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return crearVista(position, convertView, parent); // Llamar a crearVista para el elemento de vista seleccionada
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return crearVista(position, convertView, parent); // Llamar a crearVista para los elementos desplegables
    }

    private View crearVista(int position, View convertView, ViewGroup parent) {
        SpinnerItemAvionBinding binding;

        if (convertView == null) {
            binding = SpinnerItemAvionBinding.inflate(inflater, parent, false); // Inflar el layout del ítem
            convertView = binding.getRoot(); // Obtener la vista raíz del binding
            convertView.setTag(binding); // Guardar el binding en la vista
        } else {
            binding = (SpinnerItemAvionBinding) convertView.getTag(); // Recuperar el binding existente
        }

        Avion avion = aviones.get(position); // Obtener el avión correspondiente a la posición

        // Asignar la imagen y el nombre del avión
        binding.imageViewAvion.setImageResource(avion.getImagen()); // Método para obtener el recurso de la imagen
        binding.textViewAvion.setText(avion.getNombre()); // Establecer el nombre del avión

        return convertView; // Retornar la vista configurada
    }
}
