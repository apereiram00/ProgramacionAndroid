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
import com.example.todolist.databinding.FragmentEdicionBinding;

public class EdicionFragment extends Fragment {
    private FragmentEdicionBinding binding;
    private TareasViewModel tareasViewModel;
    private int posicion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEdicionBinding.inflate(inflater, container, false);
        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);

        // Obtener posición de la tarea a editar
        if (getArguments() != null) {
            posicion = getArguments().getInt("posicion");
        }

        Tarea tarea = tareasViewModel.getListaTareas().getValue().get(posicion);
        binding.tvTitulo.setText(tarea.getTitulo());
        binding.etDescripcion.setText(tarea.getDescripcion());

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.prioridades_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPrioridad.setAdapter(adapter);
        binding.spinnerPrioridad.setSelection(tarea.getPrioridad() - 1);

        // Botón "Aceptar"
        binding.btnAceptar.setOnClickListener(v -> {
            String nuevaDescripcion = binding.etDescripcion.getText().toString().trim();
            int nuevaPrioridad = binding.spinnerPrioridad.getSelectedItemPosition() + 1;

            if (!nuevaDescripcion.equals(tarea.getDescripcion()) || nuevaPrioridad != tarea.getPrioridad()) {
                tareasViewModel.editarTarea(posicion, nuevaPrioridad, nuevaDescripcion);
                Toast.makeText(requireContext(), "Tarea actualizada", Toast.LENGTH_SHORT).show();
            }
            requireActivity().onBackPressed();
        });

        // Botón "Cancelar"
        binding.btnCancelar.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        return binding.getRoot();
    }
}
