package com.example.skyrivals;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.skyrivals.databinding.FragmentUssrBinding;

import java.util.List;

public class UssrFragment extends Fragment {

    private FragmentUssrBinding binding;
    private AvionViewModel avionViewModel;
    private FavoritosViewModel favoritosViewModel; // Instancia del FavoritosViewModel
    private DetallesViewModel detallesViewModel; // Instancia del DetallesViewModel
    private AvionAdapter avionAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUssrBinding.inflate(inflater, container, false);

        // Inicializa los ViewModels
        avionViewModel = new ViewModelProvider(requireActivity()).get(AvionViewModel.class);
        favoritosViewModel = new ViewModelProvider(requireActivity()).get(FavoritosViewModel.class); // Asegúrate de que el ViewModel esté correctamente importado
        detallesViewModel = new ViewModelProvider(requireActivity()).get(DetallesViewModel.class); // Asegúrate de que el ViewModel esté correctamente importado

        setupRecyclerView();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        avionAdapter = new AvionAdapter(favoritosViewModel, detallesViewModel, false); // Especifica que no es favoritos
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(avionAdapter);

        // Observa el LiveData de aviones de USAF
        avionViewModel.getAvionesUssr().observe(getViewLifecycleOwner(), new Observer<List<Avion>>() {
            @Override
            public void onChanged(List<Avion> aviones) {
                avionAdapter.updateAviones(aviones); // Actualiza la lista en el adaptador
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpiar el binding
    }
}
