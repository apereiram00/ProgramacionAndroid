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

import com.example.todolist.databinding.FragmentCrearBinding;

public class CrearFragment extends Fragment {

    private FragmentCrearBinding binding;
    private TareasViewModel tareasViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCrearBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);
        NavController navController = Navigation.findNavController(view);

        binding.btnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Abandonar")
                    .setMessage("¿Quiere abandonar la creación de la tarea?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Aceptar", (dialog, which) -> navController.popBackStack())
                    .show();
        });

        binding.btnAceptar.setOnClickListener(v -> {
            String nombreTarea = binding.inputNombreTarea.getText().toString().trim();
            String descripcionTarea = binding.inputDescripcionTarea.getText().toString().trim();
            int prioridadSeleccionada = binding.spinnerPrioridad.getSelectedItemPosition() + 1;

            binding.inputLayoutNombreTarea.setError(null);
            binding.inputLayoutDescripcionTarea.setError(null);

            boolean valid = true;
            if (nombreTarea.isEmpty()) {
                binding.inputLayoutNombreTarea.setError("Debe especificar un Nombre");
                valid = false;
            }
            if (descripcionTarea.isEmpty()) {
                binding.inputLayoutDescripcionTarea.setError("Debe especificar una Descripción");
                valid = false;
            }
            if (!valid) return;

            boolean tareaExiste = verificarTareaExiste(nombreTarea);

            if (tareaExiste) {
                binding.inputLayoutNombreTarea.setError("Ya existe una tarea con ese nombre");
            } else {
                Tarea nuevaTarea = new Tarea(nombreTarea, descripcionTarea, prioridadSeleccionada);
                binding.progressBar.setVisibility(View.VISIBLE);

                tareasViewModel.agregarTarea(nuevaTarea, new TareaCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            mostrarMensajeExito();
                            navController.popBackStack();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Error")
                                    .setMessage(errorMessage)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Aceptar", null)
                                    .show();
                        });
                    }
                });
            }
        });
    }

    private boolean verificarTareaExiste(String nombreTarea) {
        if (tareasViewModel.getListaTareas().getValue() != null) {
            for (Tarea tarea : tareasViewModel.getListaTareas().getValue()) {
                if (tarea.getTitulo().equalsIgnoreCase(nombreTarea)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void mostrarMensajeExito() {
        new AlertDialog.Builder(requireContext())
                .setTitle("TAREA CREADA")
                .setMessage("La tarea ha sido creada con éxito")
                .setIcon(android.R.drawable.checkbox_on_background)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
