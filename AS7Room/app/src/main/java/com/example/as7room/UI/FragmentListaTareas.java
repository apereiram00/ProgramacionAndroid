package com.example.as7room.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.as7room.Model.Tarea;
import com.example.as7room.R;
import com.example.as7room.databinding.FragmentListaTareasBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class FragmentListaTareas extends Fragment {

    private FragmentListaTareasBinding binding;
    private TareasAdapter tareasAdapter;
    private TareasViewModel tareasViewModel;
    private boolean mostrarCompletadas = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListaTareasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);
        if (tareasAdapter == null) {
            tareasAdapter = new TareasAdapter(new ArrayList<>());
        }

        binding.recyclerTareas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTareas.setAdapter(tareasAdapter);

        mostrarCompletadas = requireActivity()
                .getSharedPreferences("AppPreferences", 0)
                .getBoolean("show_completed", false);

        if (tareasAdapter != null) {
            tareasAdapter.setMostrarCompletadas(mostrarCompletadas);
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicion = viewHolder.getAdapterPosition();

                Tarea tarea = tareasAdapter.obtenerTareaEn(posicion);
                FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

                if (usuarioActual != null && tarea.getAutor().equals(usuarioActual.getEmail())) {
                    if (direction == ItemTouchHelper.LEFT) {
                        mostrarDialogoConfirmacionEliminar(tarea, posicion);
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        mostrarDialogoConfirmacionCompletar(tarea, posicion);
                    }
                } else {
                    Toast.makeText(getContext(), "No puedes modificar esta tarea", Toast.LENGTH_SHORT).show();
                    tareasAdapter.notifyItemChanged(posicion);
                }
            }

        }).attachToRecyclerView(binding.recyclerTareas);

        tareasViewModel.obtenerTodasLasTareas().observe(getViewLifecycleOwner(), tareas -> {
            if (tareas != null && !tareas.isEmpty()) {
                tareasAdapter.establecerTareas(tareas);
            }
        });

        binding.btnAgregarTarea.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.listaFragment);
            navController.navigate(R.id.action_fragmentListaTareas_to_fragmentAgregarTarea2);
        });

    }
    public void actualizarFiltroTareas(boolean mostrarCompletadas) {
        this.mostrarCompletadas = mostrarCompletadas;
        if (tareasAdapter == null) {
            tareasAdapter = new TareasAdapter(new ArrayList<>());
            binding.recyclerTareas.setAdapter(tareasAdapter);
        }
        tareasAdapter.setMostrarCompletadas(mostrarCompletadas);
        tareasAdapter.filtrarTareas();
    }

    private void mostrarDialogoConfirmacionEliminar(Tarea tarea, int posicion) {
        new AlertDialog.Builder(getContext())
                .setTitle("Borrar")
                .setMessage("¿Quiere borrar la tarea?")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    tareasViewModel.eliminar(tarea);
                    tareasAdapter.eliminarTareaEn(posicion);
                    Toast.makeText(getContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    tareasAdapter.notifyItemChanged(posicion);
                })
                .create()
                .show();
    }

    private void mostrarDialogoConfirmacionCompletar(Tarea tarea, int posicion) {
        new AlertDialog.Builder(getContext())
                .setTitle("Completar")
                .setMessage("¿Quiere marcar esta tarea como completada?")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    tarea.setCompletada(true);
                    tareasViewModel.actualizar(tarea);
                    tareasAdapter.marcarTareaComoCompletada(posicion);
                    Toast.makeText(getContext(), "Tarea completada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    tareasAdapter.notifyItemChanged(posicion);
                })
                .create()
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

