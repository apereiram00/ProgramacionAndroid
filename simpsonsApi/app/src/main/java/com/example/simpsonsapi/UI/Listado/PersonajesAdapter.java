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

    private List<Personajes> personajes = new ArrayList<>(); // // Lista de personajes que se va a mostrar en el RecyclerView
    public void setPersonajes(List<Personajes> personajes) { //  // Método para actualizar la lista de personajes
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
        holder.binding.nombrePersonaje.setText(personaje.getCharacter()); // Establezco el nombre del personaje en el TextView
        holder.itemView.setOnClickListener(v -> { // SetOnClick para navegar al DetallesFragment
            Bundle bundle = new Bundle(); // Y creo un bundle para enviar los datos a ese fragment
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

    // Clase interna ViewHolder que mantiene una referencia al binding del ítem (la pude hacer como clase separada pero es poco código en este caso)
    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPersonajeBinding binding;
        ViewHolder(ItemPersonajeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
