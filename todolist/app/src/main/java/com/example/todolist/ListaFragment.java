package com.example.todolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        NavController navController = Navigation.findNavController(view);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TareasAdapter(tareasViewModel.getListaTareas().getValue(), position -> {
            Tarea tarea = tareasViewModel.getListaTareas().getValue().get(position);
            tareasViewModel.setTareaSeleccionada(tarea);
            navController.navigate(R.id.action_listaFragment_to_edicionFragment);
        });
        binding.recyclerView.setAdapter(adapter);

        tareasViewModel.getListaTareas().observe(getViewLifecycleOwner(), adapter::updateTareas);
        binding.btnCrearTarea.setOnClickListener(v -> navController.navigate(R.id.action_listaFragment_to_crearFragment));
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Tarea tarea = tareasViewModel.getListaTareas().getValue().get(position);

                bloquearInterfaz(true);

                tareasViewModel.eliminarTarea(position, new TareaCallback() {
                    @Override
                    public void onStart() {}

                    @Override
                    public void onSuccess() {
                        desbloquearInterfaz();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        desbloquearInterfaz();
                        mostrarError("Error al eliminar la tarea: " + tarea.getTitulo());
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void bloquearInterfaz(boolean bloquear) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (bloquear) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnCrearTarea.setEnabled(false);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCrearTarea.setEnabled(true);
                }
            });
        }
    }

    private void desbloquearInterfaz() {
        bloquearInterfaz(false);
    }

    private void mostrarError(String mensaje) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("ERROR")
                        .setMessage(mensaje)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Aceptar", null)
                        .show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
