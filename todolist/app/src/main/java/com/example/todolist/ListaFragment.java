package com.example.todolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.databinding.FragmentListaBinding;

public class ListaFragment extends Fragment {
    private FragmentListaBinding binding;
    private TareasViewModel tareasViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListaBinding.inflate(inflater, container, false);
        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);

        // Configurar RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        TareasAdapter adapter = new TareasAdapter(tareasViewModel.getListaTareas().getValue());
        binding.recyclerView.setAdapter(adapter);

        // Observar cambios en la lista de tareas
        tareasViewModel.getListaTareas().observe(getViewLifecycleOwner(), adapter::setTareas);

        // Swipe para eliminar
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                tareasViewModel.eliminarTarea(position);
            }
        }).attachToRecyclerView(binding.recyclerView);

        // Botón para crear nueva tarea
        binding.btnCrearTarea.setOnClickListener(v -> {
            // Navegar a CrearFragment
        });

        return binding.getRoot();
    }
}
