package com.example.todolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.todolist.databinding.FragmentCrearBinding;

public class CrearFragment extends Fragment {
    private FragmentCrearBinding binding;
    private TareasViewModel tareasViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCrearBinding.inflate(inflater, container, false);
        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);

        // Configurar Spinner para prioridades
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.prioridades_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPrioridad.setAdapter(adapter);

        // Botón "Aceptar"
        binding.btnAceptar.setOnClickListener(v -> {
            String titulo = binding.etTitulo.getText().toString().trim();
            String descripcion = binding.etDescripcion.getText().toString().trim();
            int prioridad = binding.spinnerPrioridad.getSelectedItemPosition() + 1;

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                tareasViewModel.agregarTarea(new Tarea(titulo, descripcion, prioridad));
                Toast.makeText(requireContext(), "Tarea creada", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed(); // Regresar al fragmento anterior
            }
        });

        // Botón "Cancelar"
        binding.btnCancelar.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        return binding.getRoot();
    }
}
