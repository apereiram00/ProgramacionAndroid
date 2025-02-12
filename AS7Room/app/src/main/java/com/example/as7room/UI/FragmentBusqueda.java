package com.example.as7room.UI;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.as7room.R;
import com.example.as7room.Model.Tarea;
import com.example.as7room.databinding.FragmentBusquedaBinding;
import java.util.ArrayList;
import java.util.List;

public class FragmentBusqueda extends Fragment {

    private FragmentBusquedaBinding binding;
    private TareasAdapter tareasAdapter;
    private TareasViewModel tareasViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBusquedaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tareasAdapter = new TareasAdapter(new ArrayList<>());
        binding.recyclerBusqueda.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerBusqueda.setAdapter(tareasAdapter);
        tareasViewModel = new ViewModelProvider(requireActivity()).get(TareasViewModel.class);

        tareasViewModel.obtenerTodasLasTareas().observe(getViewLifecycleOwner(), tareas -> {
            if (tareas != null) {
                tareasAdapter.establecerTareas(tareas);
            }
        });

        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarTareas(newText);
                return true;
            }
        });
    }

    private void filtrarTareas(String texto) {
        if (TextUtils.isEmpty(texto)) {
            tareasAdapter.establecerTareas(tareasViewModel.obtenerTodasLasTareas().getValue());
        } else {
            List<Tarea> tareasFiltradas = new ArrayList<>();
            for (Tarea tarea : tareasViewModel.obtenerTodasLasTareas().getValue()) {
                if (tarea.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    tareasFiltradas.add(tarea);
                }
            }
            tareasAdapter.establecerTareas(tareasFiltradas);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
