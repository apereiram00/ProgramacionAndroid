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

import com.example.todolist.databinding.FragmentEdicionBinding;

public class EdicionFragment extends Fragment {

    private FragmentEdicionBinding binding;
    private TareasViewModel tareasViewModel;

    private String nombreOriginal;
    private String descripcionOriginal;
    private int prioridadOriginal;
    private int posicionTarea;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEdicionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);
        NavController navController = Navigation.findNavController(view);

        if (getArguments() != null) {
            nombreOriginal = getArguments().getString("titulo");
            descripcionOriginal = getArguments().getString("descripcion");
            prioridadOriginal = getArguments().getInt("prioridad");
            posicionTarea = getArguments().getInt("position");
        }

        binding.inputNombreTarea.setText(nombreOriginal);
        binding.inputDescripcionTarea.setText(descripcionOriginal);
        binding.spinnerPrioridad.setSelection(prioridadOriginal - 1);

        binding.inputNombreTarea.setFocusable(false);
        binding.inputDescripcionTarea.setFocusable(false);

        binding.btnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Abandonar")
                    .setMessage("¿Quiere abandonar la edición de la tarea?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        navController.popBackStack();
                    })
                    .show();
        });

        binding.btnAceptar.setOnClickListener(v -> {
            String nuevoNombre = binding.inputNombreTarea.getText().toString().trim();
            String nuevaDescripcion = binding.inputDescripcionTarea.getText().toString().trim();
            int nuevaPrioridad = binding.spinnerPrioridad.getSelectedItemPosition() + 1;

            boolean seModifico = !nuevoNombre.equals(nombreOriginal)
                    || !nuevaDescripcion.equals(descripcionOriginal)
                    || nuevaPrioridad != prioridadOriginal;

            if (seModifico) {
                tareasViewModel.editarTarea(posicionTarea, nuevaPrioridad, nuevaDescripcion);

                mostrarMensajeExito();
                navController.popBackStack();
            } else {
                mostrarMensajeFracaso();
            }
        });
    }

    private void mostrarMensajeExito() {
        new AlertDialog.Builder(requireContext())
                .setTitle("TAREA MODIFICADA")
                .setMessage("La tarea se ha modificado con éxito")
                .setIcon(android.R.drawable.checkbox_on_background)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void mostrarMensajeFracaso() {
        new AlertDialog.Builder(requireContext())
                .setTitle("TAREA NO MODIFICADA")
                .setMessage("La tarea no ha sufrido modificaciones")
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
