package com.example.skyrivals;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skyrivals.databinding.ItemAvionBinding;

import java.util.ArrayList;
import java.util.List;

public class AvionAdapter extends RecyclerView.Adapter<AvionViewHolder> {
    private final List<Avion> aviones = new ArrayList<>();
    private final FavoritosViewModel favoritosViewModel;
    private final DetallesViewModel detallesViewModel;
    private final boolean isFavoritos;

    public AvionAdapter(FavoritosViewModel favoritosViewModel, DetallesViewModel detallesViewModel, boolean isFavoritos) {
        this.favoritosViewModel = favoritosViewModel;
        this.detallesViewModel = detallesViewModel;
        this.isFavoritos = isFavoritos;
    }

    @NonNull
    @Override
    public AvionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAvionBinding binding = ItemAvionBinding.inflate(inflater, parent, false);
        return new AvionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvionViewHolder holder, int position) {
        Avion avion = aviones.get(position);
        holder.bind(avion);
        holder.binding.cardView.setCardBackgroundColor(position % 2 == 0 ? Color.parseColor("#E0F7FA") : Color.parseColor("#FFEBEE"));

        if (isFavoritos) {
            holder.binding.faviconFavorito.setVisibility(View.GONE);
            holder.binding.flagFavorito.setVisibility(View.VISIBLE);

            switch (avion.getPais()) {
                case "Alemania":
                    holder.binding.flagFavorito.setImageResource(R.drawable.ger_flag);
                    break;
                case "EEUU":
                    holder.binding.flagFavorito.setImageResource(R.drawable.eeuu_flag);
                    break;
                case "Inglaterra":
                    holder.binding.flagFavorito.setImageResource(R.drawable.eng_flag);
                    break;
                case "URSS":
                    holder.binding.flagFavorito.setImageResource(R.drawable.ussr_flag);
                    break;
                default:
                    holder.binding.flagFavorito.setImageResource(0);
                    holder.binding.flagFavorito.setVisibility(View.GONE);
                    break;
            }
        } else {
            boolean isFavorito = favoritosViewModel.getAvionesFavoritos().getValue().contains(avion);
            holder.binding.faviconFavorito.setImageResource(isFavorito ? R.drawable.favactivo : R.drawable.favinactivo);
            holder.binding.faviconFavorito.setVisibility(View.VISIBLE);
            holder.binding.flagFavorito.setVisibility(View.GONE);

            holder.binding.faviconFavorito.setOnClickListener(v -> {
                if (!isFavorito) {
                    favoritosViewModel.addFavorito(avion);
                    holder.binding.faviconFavorito.setImageResource(R.drawable.favactivo);
                }
            });
        }

        holder.binding.cardView.setOnClickListener(v -> {
            detallesViewModel.setAvion(avion);
            int orientation = holder.binding.getRoot().getContext().getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                NavController navController = Navigation.findNavController(v);
                int currentDestination = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentDestination == R.id.avionesFragment) {
                    navController.navigate(R.id.action_avionesFragment_to_detallesFragment);
                } else if (currentDestination == R.id.favoritosFragment) {
                    navController.navigate(R.id.action_favoritosFragment_to_detallesFragment);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return aviones.size();
    }

    public void updateAviones(List<Avion> nuevosAviones) {
        aviones.clear();
        if (nuevosAviones != null) {
            aviones.addAll(nuevosAviones);
        }
        notifyDataSetChanged();
    }

    public List<Avion> getAviones() {
        return aviones;
    }
}