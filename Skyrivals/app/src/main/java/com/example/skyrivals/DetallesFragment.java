package com.example.skyrivals;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log; // Importa Log para los mensajes de depuración
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.skyrivals.databinding.FragmentDetallesBinding;

import java.util.List;

public class DetallesFragment extends Fragment {

    private FragmentDetallesBinding binding;
    private DetallesViewModel detallesViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetallesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa el ViewModel
        detallesViewModel = new ViewModelProvider(requireActivity()).get(DetallesViewModel.class);

        // Observa los cambios en el avión seleccionado
        detallesViewModel.getAvion().observe(getViewLifecycleOwner(), avion -> {
            if (avion != null) {
                // Actualiza la UI con los detalles del avión seleccionado
                binding.nombreAvionDetalle.setText(avion.getNombre());
                binding.imagenAvionDetalle.setImageResource(avion.getImagen());
                binding.velocidadMaxima.setText("Velocidad Máxima: " + avion.getVelocidadMaxima()+" km/h");
                binding.numeroArmas.setText("Número de Ametralladoras/Cañones: " + avion.getNumeroArmas());
                binding.fechaConstruccion.setText("Fecha de Construcción: " + avion.getFechaConstruccion());
                binding.descripcionHistorica.setText(avion.getDescripcionHistorica());

                // Manejar la visualización de tags solo en orientación horizontal
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    List<String> tipos = avion.getTipos();
                    if (binding.tagsContainer != null) {
                        binding.tagsContainer.removeAllViews();
                        for (String tipo : tipos) {
                            TextView tagView = new TextView(binding.tagsContainer.getContext());
                            tagView.setText(tipo);
                            tagView.setPadding(8, 4, 8, 4);

                            // Colorear según el tipo de avión
                            if ("Caza".equals(tipo)) {
                                tagView.setTextColor(Color.GREEN);
                                tagView.setBackgroundResource(R.drawable.tag_background_caza);
                            } else if ("Interceptor".equals(tipo)) {
                                tagView.setTextColor(Color.BLUE);
                                tagView.setBackgroundResource(R.drawable.tag_background_interceptor);
                            } else {
                                tagView.setTextColor(Color.BLACK);
                                tagView.setBackgroundColor(Color.LTGRAY);
                            }
                            binding.tagsContainer.addView(tagView);
                        }
                    }
                }
            }
        });

        // Verifica la orientación y maneja el botón solo si existe
        if (binding.botonVolver != null) {
            Log.d("DetallesFragment", "botonVolver encontrado");
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Asegura que el botón esté visible en vertical y configura su acción
                binding.botonVolver.setVisibility(View.VISIBLE);
                binding.botonVolver.setOnClickListener(v -> {
                    Log.d("DetallesFragment", "botonVolver clicado");
                    requireActivity().onBackPressed();
                });
            } else {
                // Oculta el botón en horizontal si existe
                binding.botonVolver.setVisibility(View.GONE);
            }
        } else {
            Log.e("DetallesFragment", "botonVolver es null");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpiar la referencia de binding para evitar fugas de memoria
    }
}
