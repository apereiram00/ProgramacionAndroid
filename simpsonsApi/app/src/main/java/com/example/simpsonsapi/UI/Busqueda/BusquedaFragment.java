package com.example.simpsonsapi.UI.Busqueda;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.example.simpsonsapi.R;
import com.example.simpsonsapi.databinding.FragmentBusquedaBinding;
import com.example.simpsonsapi.model.Personajes;
import com.example.simpsonsapi.UI.Listado.PersonajeViewModel;

import java.util.List;

public class BusquedaFragment extends Fragment {

    private FragmentBusquedaBinding binding;
    private PersonajeViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBusquedaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PersonajeViewModel.class);
        binding.imagenBusqueda.setImageResource(R.drawable.ic_question_mark);
        binding.nombreBusqueda.setText("");
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    buscarPersonaje(query.trim());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void buscarPersonaje(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }
        Personajes personajeEncontrado = buscarPersonajePorNombre(nombre.trim());

        if (personajeEncontrado != null) {
            Glide.with(requireContext())
                    .load(personajeEncontrado.getImage())
                    .into(binding.imagenBusqueda);
            binding.nombreBusqueda.setText(personajeEncontrado.getCharacter());
            binding.quoteBusqueda.setText(personajeEncontrado.getQuote());
        } else {
            binding.imagenBusqueda.setImageResource(R.drawable.ic_question_mark);
            binding.nombreBusqueda.setText("Personaje no encontrado");
            binding.quoteBusqueda.setText("");
        }
    }

    private Personajes buscarPersonajePorNombre(String nombre) {
        List<Personajes> personajes = viewModel.getPersonajes().getValue();

        if (personajes != null && !personajes.isEmpty()) {
            for (Personajes personaje : personajes) {
                if (personaje.getCharacter().toLowerCase().contains(nombre.toLowerCase())) {
                    return personaje;
                }
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
