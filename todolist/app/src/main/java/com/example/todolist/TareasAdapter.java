package com.example.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todolist.databinding.ItemTareaBinding;
import java.util.List;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {
    private List<Tarea> listaTareas;

    public TareasAdapter(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTareaBinding binding = ItemTareaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TareaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);
        holder.binding.tvTitulo.setText(tarea.getTitulo());
        holder.binding.tvDescripcion.setText(tarea.getDescripcion());
        holder.binding.tvPrioridad.setText("Prioridad: " + tarea.getPrioridad());

        // Botón para editar tarea
        holder.binding.btnEditar.setOnClickListener(v -> {
            // Lógica para editar tarea
            if (holder.onEditarClickListener != null) {
                holder.onEditarClickListener.onEditarClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas != null ? listaTareas.size() : 0;
    }

    public void setTareas(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
        notifyDataSetChanged();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        private final ItemTareaBinding binding;
        private OnEditarClickListener onEditarClickListener;

        public TareaViewHolder(@NonNull ItemTareaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setOnEditarClickListener(OnEditarClickListener listener) {
            this.onEditarClickListener = listener;
        }
    }

    public interface OnEditarClickListener {
        void onEditarClick(int position);
    }
}

