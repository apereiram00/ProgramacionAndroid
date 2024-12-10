package com.example.todolist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.databinding.ItemTareaBinding;

import java.util.List;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {

    private List<Tarea> tareas;
    private final OnEditarTareaListener editarTareaListener;

    public interface OnEditarTareaListener {
        void onEditarTarea(int position);
    }

    public TareasAdapter(List<Tarea> tareas,  OnEditarTareaListener listener) {
        this.tareas = tareas;
        this.editarTareaListener = listener;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTareaBinding binding = ItemTareaBinding.inflate(inflater, parent, false);
        return new TareaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = tareas.get(position);
        holder.bind(tarea);
        holder.binding.btnEditar.setOnClickListener(v -> editarTareaListener.onEditarTarea(position));
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    public void updateTareas(List<Tarea> tareas) {
        this.tareas = tareas;
        notifyDataSetChanged();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        private final ItemTareaBinding binding;

        public TareaViewHolder(ItemTareaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Tarea tarea) {
            binding.numeroPrioridad.setText(String.valueOf(tarea.getPrioridad()));
            binding.NombreTarea.setText(tarea.getTitulo());
            binding.DescripcionTarea.setText(tarea.getDescripcion());
        }
    }
}
