package com.example.skyrivals;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritosFragment extends Fragment {
    private RecyclerView recyclerView;
    private AvionAdapter adapter;
    private FavoritosViewModel favoritosViewModel;
    private DetallesViewModel detallesViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar los ViewModels
        favoritosViewModel = new ViewModelProvider(requireActivity()).get(FavoritosViewModel.class);
        detallesViewModel = new ViewModelProvider(requireActivity()).get(DetallesViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_favoritos);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        // Crear el adaptador y asignarlo al RecyclerView
        adapter = new AvionAdapter(favoritosViewModel, detallesViewModel, true); // Indica que es adaptador de favoritos
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Observa cambios en la lista de favoritos y actualiza el adaptador automáticamente
        favoritosViewModel.getAvionesFavoritos().observe(getViewLifecycleOwner(), new Observer<List<Avion>>() {
            @Override
            public void onChanged(List<Avion> avionesFavoritos) {
                adapter.updateAviones(avionesFavoritos); // Llama a `updateAviones` para actualizar la lista en el adaptador
            }
        });

        // Implementar deslizamiento para eliminar favoritos
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Avion avion = adapter.getAviones().get(position);

                    // Diálogo de confirmación para eliminar
                    new AlertDialog.Builder(getContext())
                            .setTitle("Eliminar favorito")
                            .setMessage("¿Estás seguro de que deseas eliminar " + avion.getNombre() + " de favoritos?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                favoritosViewModel.removeFavorito(avion);
                            })
                            .setNegativeButton("No", (dialog, which) -> adapter.notifyItemChanged(position))
                            .show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }
}
