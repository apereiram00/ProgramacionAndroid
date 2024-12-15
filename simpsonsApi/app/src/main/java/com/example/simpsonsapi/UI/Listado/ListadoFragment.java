package com.example.simpsonsapi.UI.Listado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.simpsonsapi.databinding.FragmentListadoBinding;

// Fragmento para mostrar la lista de personajes de Los Simpsons
public class ListadoFragment extends Fragment {

    private FragmentListadoBinding binding;
    private PersonajesAdapter adapter; // Adaptador para manejar la lista de personajes en el RecyclerView
    private PersonajeViewModel viewModel; // ViewModel para gestionar y observar los datos de personajes

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListadoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PersonajesAdapter();
        binding.recyclerViewPersonajes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPersonajes.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonajeViewModel.class);

        viewModel.getPersonajes().observe(getViewLifecycleOwner(), personajes -> {
            binding.progressBar.setVisibility(View.GONE);
            if (personajes != null) {
                adapter.setPersonajes(personajes);
            }
        });
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.cargarPersonajes(40); // Con esto pongo el limite a 40 personajes

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

