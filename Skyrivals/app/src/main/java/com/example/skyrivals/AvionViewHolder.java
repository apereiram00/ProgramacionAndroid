package com.example.skyrivals;

import android.graphics.Color;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.skyrivals.databinding.ItemAvionBinding;

public class AvionViewHolder extends RecyclerView.ViewHolder {
    public final ItemAvionBinding binding;

    public AvionViewHolder(ItemAvionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Avion avion) {
        binding.nombreAvion.setText(avion.getNombre());
        binding.imagenAvion.setImageResource(avion.getImagen());

        // Limpiar el contenedor de tags
        binding.tagsContainer.removeAllViews();
        for (String tipo : avion.getTipos()) {
            TextView tagView = new TextView(binding.tagsContainer.getContext());
            tagView.setText(tipo);
            if ("Caza".equals(tipo)) {
                tagView.setTextColor(Color.GREEN);
                tagView.setBackgroundResource(R.drawable.tag_background_caza);
            } else if ("Interceptor".equals(tipo)) {
                tagView.setTextColor(Color.BLUE);
                tagView.setBackgroundResource(R.drawable.tag_background_interceptor);
            }
            binding.tagsContainer.addView(tagView);
        }
    }
}
