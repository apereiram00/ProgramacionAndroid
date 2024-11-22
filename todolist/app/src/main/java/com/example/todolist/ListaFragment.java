package com.example.todolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.databinding.FragmentListaBinding;

public class ListaFragment extends Fragment {
    private FragmentListaBinding binding;
    private TareasViewModel tareasViewModel;
    private TareasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TareasAdapter(tareasViewModel.getListaTareas().getValue(), position -> {
            Tarea tarea = tareasViewModel.getListaTareas().getValue().get(position);

            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putString("titulo", tarea.getTitulo());
            args.putString("descripcion", tarea.getDescripcion());
            args.putInt("prioridad", tarea.getPrioridad());
            args.putInt("position", position);
            navController.navigate(R.id.action_listaFragment_to_edicionFragment, args);
        });
        binding.recyclerView.setAdapter(adapter);

        tareasViewModel.getListaTareas().observe(getViewLifecycleOwner(), tareas -> {
            adapter.updateTareas(tareas);
        });

        binding.btnCrearTarea.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_listaFragment_to_crearFragment);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Tarea tarea = tareasViewModel.getListaTareas().getValue().get(position);

                new AlertDialog.Builder(requireContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Borrar")
                        .setMessage("¿Quiere borrar la tarea: " + tarea.getTitulo() + "?")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            tareasViewModel.eliminarTarea(position);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
