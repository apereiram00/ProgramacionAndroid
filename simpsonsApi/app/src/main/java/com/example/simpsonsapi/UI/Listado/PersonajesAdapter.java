package com.example.simpsonsapi.UI.Listado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simpsonsapi.R;
import com.example.simpsonsapi.databinding.ItemPersonajeBinding;
import com.example.simpsonsapi.model.Personajes;

import java.util.ArrayList;
import java.util.List;

public class PersonajesAdapter extends RecyclerView.Adapter<PersonajesAdapter.ViewHolder> {

    private List<Personajes> personajes = new ArrayList<>();

    public void setPersonajes(List<Personajes> personajes) {
        this.personajes = personajes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPersonajeBinding binding = ItemPersonajeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Personajes personaje = personajes.get(position);
        holder.binding.nombrePersonaje.setText(personaje.getCharacter());
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("character", personaje.getCharacter());
            bundle.putString("quote", personaje.getQuote());
            bundle.putString("imageUrl", personaje.getImage());
            Navigation.findNavController(v).navigate(R.id.action_listadoFragment_to_detalleFragment2, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return personajes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPersonajeBinding binding;
        ViewHolder(ItemPersonajeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
