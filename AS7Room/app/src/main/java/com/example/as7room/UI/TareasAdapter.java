package com.example.as7room.UI;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.as7room.Model.Tarea;
import com.example.as7room.R;
import com.example.as7room.databinding.ItemTareaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareasViewHolder> {

    private List<Tarea> listaTareas;
    private List<Tarea> listaFiltrada;
    private boolean mostrarCompletadas = false;


    public TareasAdapter(List<Tarea> tareas) {
        this.listaTareas = tareas != null ? tareas : new ArrayList<>();
        this.listaFiltrada = new ArrayList<>(this.listaTareas);
    }

    @NonNull
    @Override
    public TareasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTareaBinding binding = ItemTareaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TareasViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TareasViewHolder holder, int position) {
        if (position < 0 || position >= listaFiltrada.size()) {
            return;
        }

        Tarea tareaActual = listaFiltrada.get(position);
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual != null && tareaActual.getAutor().equals(usuarioActual.getEmail())) {
            holder.itemView.setOnLongClickListener(v -> {
                eliminarTareaEn(position);
                return true;
            });


            holder.itemView.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                Bundle bundle = new Bundle();
                bundle.putSerializable("tarea", tareaActual);

                if (navController.getCurrentDestination().getId() == R.id.fragmentListaTareas) {
                    navController.navigate(R.id.action_fragmentListaTareas_to_fragmentDetalleTarea, bundle);
                } else if (navController.getCurrentDestination().getId() == R.id.fragmentBusqueda) {
                    navController.navigate(R.id.action_fragmentBusqueda_to_fragmentDetalleTarea, bundle);
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
            holder.itemView.setOnClickListener(null);
        }

        holder.binding.textoTitulo.setText(tareaActual.getNombre());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = dateFormat.format(new Date(tareaActual.getFecha()));
        holder.binding.textoFecha.setText(fechaFormateada);

        holder.binding.textoAutor.setText(tareaActual.getAutor());

        long fechaActual = System.currentTimeMillis();
        long diferencia = tareaActual.getFecha() - fechaActual;

        if (diferencia < 0) {
            holder.binding.textoFecha.setTextColor(Color.RED);
        } else if (diferencia <= 7 * 24 * 60 * 60 * 1000L) {
            holder.binding.textoFecha.setTextColor(Color.YELLOW);
        } else {
            holder.binding.textoFecha.setTextColor(Color.GREEN);
        }

        if (tareaActual.isCompletada()) {
            holder.binding.textoTitulo.setPaintFlags(holder.binding.textoTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.textoTitulo.setTextColor(Color.GRAY);
            if (!mostrarCompletadas) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
        } else {
            holder.binding.textoTitulo.setPaintFlags(holder.binding.textoTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.binding.textoTitulo.setTextColor(Color.BLACK);
            holder.itemView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void establecerTareas(List<Tarea> tareas) {
        this.listaTareas = tareas != null ? tareas : new ArrayList<>();
        listaFiltrada.clear();
        listaFiltrada.addAll(tareas);
        notifyDataSetChanged();
    }

    public Tarea obtenerTareaEn(int posicion) {
        return listaFiltrada.get(posicion);
    }

    public void eliminarTareaEn(int posicion) {
        Tarea tarea = listaFiltrada.get(posicion);
        listaTareas.remove(tarea);
        listaFiltrada.remove(tarea);
        notifyItemRemoved(posicion);
    }

    public void marcarTareaComoCompletada(int posicion) {
        Tarea tarea = listaFiltrada.get(posicion);
        tarea.setCompletada(true);
        notifyItemChanged(posicion);
    }

    public void filtrarTareas() {
        List<Tarea> tareasFiltradas = new ArrayList<>();

        for (Tarea tarea : listaTareas) {
            if (mostrarCompletadas || !tarea.isCompletada()) {
                tareasFiltradas.add(tarea);
            }
        }

        listaFiltrada.clear();
        listaFiltrada.addAll(tareasFiltradas);
        notifyDataSetChanged();
    }

    public void setMostrarCompletadas(boolean mostrarCompletadas) {
        this.mostrarCompletadas = mostrarCompletadas;
        filtrarTareas();
    }

    static class TareasViewHolder extends RecyclerView.ViewHolder {
        private final ItemTareaBinding binding;

        public TareasViewHolder(@NonNull ItemTareaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
