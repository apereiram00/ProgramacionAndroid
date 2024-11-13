package com.example.skyrivals;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.skyrivals.databinding.FragmentCombateBinding;

import java.util.List;

public class CombateFragment extends Fragment {

    private FragmentCombateBinding binding; // Variable para el View Binding
    private AvionViewModel avionViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout usando View Binding
        binding = FragmentCombateBinding.inflate(inflater, container, false);
        avionViewModel = new ViewModelProvider(requireActivity()).get(AvionViewModel.class);

        // Configurar listeners y cargar aviones
        setupSpinnerListeners();
        setupSimularButton();

        // Observa los aviones del ViewModel
        avionViewModel.getAviones().observe(getViewLifecycleOwner(), new Observer<List<Avion>>() {
            @Override
            public void onChanged(List<Avion> aviones) {
                configurarSpinners(aviones); // Configura los spinners con la lista de aviones
            }
        });

        return binding.getRoot(); // Retornar la vista raíz del binding
    }

    private void configurarSpinners(List<Avion> aviones) {
        // Crea el adaptador y lo asigna a los spinners
        AvionSpinnerAdapter adapter = new AvionSpinnerAdapter(requireContext(), aviones);
        binding.spinnerAvion1.setAdapter(adapter); // Asignar adaptador al primer spinner
        binding.spinnerAvion2.setAdapter(adapter); // Asignar adaptador al segundo spinner
    }

    private void setupSpinnerListeners() {
        // Listener para el primer spinner
        binding.spinnerAvion1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Lógica cuando se selecciona un avión en spinnerAvion1
                Log.d("CombateFragment", "Avión 1 seleccionado: " + parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Lógica cuando no hay selección
            }
        });

        // Listener para el segundo spinner
        binding.spinnerAvion2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Lógica cuando se selecciona un avión en spinnerAvion2
                Log.d("CombateFragment", "Avión 2 seleccionado: " + parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Lógica cuando no hay selección
            }
        });
    }

    private void setupSimularButton() {
        // Configurar el botón de simulación
        binding.buttonSimular.setOnClickListener(v -> simularCombate());
    }

    private void simularCombate() {
        Avion avion1 = (Avion) binding.spinnerAvion1.getSelectedItem(); // Obtener avión seleccionado del primer spinner
        Avion avion2 = (Avion) binding.spinnerAvion2.getSelectedItem(); // Obtener avión seleccionado del segundo spinner

        // Validar selección de aviones
        if (avion1 == null || avion2 == null || avion1.equals(avion2)) {
            binding.textResultado.setText("Selecciona dos aviones diferentes.");
            binding.cardViewResultado.setVisibility(View.GONE); // Ocultar CardView si hay error
            return;
        }

        // Simular combate y mostrar resultados
        String resultado = Simulador.simular(avion1, avion2); // Simular combate entre aviones
        binding.textResultado.setText(resultado); // Mostrar resultado de la simulación

        // Mostrar detalles del avión ganador
        Avion ganador = determinarGanador(avion1, avion2);
        mostrarDetallesGanador(ganador); // Mostrar detalles del avión ganador

        // Mostrar el CardView con el resultado
        binding.cardViewResultado.setVisibility(View.VISIBLE); // Hacer visible el CardView
    }

    private Avion determinarGanador(Avion avion1, Avion avion2) {
        // Lógica para determinar el ganador, basado en atributos (puedes ajustar la lógica aquí)
        if (avion1.getVelocidadMaxima() > avion2.getVelocidadMaxima()) {
            return avion1;
        } else if (avion1.getVelocidadMaxima() < avion2.getVelocidadMaxima()) {
            return avion2;
        } else {
            // En caso de empate, decidir aleatoriamente
            return Math.random() < 0.5 ? avion1 : avion2;
        }
    }

    private void mostrarDetallesGanador(Avion ganador) {
        // Mostrar detalles del avión ganador
        String detallesGanador = "Ganador: " + ganador.getNombre() + "\n" +
                "Velocidad Máxima: " + ganador.getVelocidadMaxima() + " km/h\n" +
                "Número de Armas: " + ganador.getNumeroArmas() + "\n" +
                "Maniobrabilidad: " + ganador.getManiobrabilidad() + "\n" +
                "Construido en: " + ganador.getFechaConstruccion() + "\n" +
                "Descripción: " + ganador.getDescripcionHistorica() + "\n" +
                "País: " + ganador.getPais();

        binding.textResultado.setText(detallesGanador); // Mostrar detalles en el TextView
        // Mostrar la imagen del avión ganador
        binding.imageViewAvion.setImageResource(ganador.getImagen2()); // Asegúrate de tener este método en la clase Avion
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruye
    }
}
