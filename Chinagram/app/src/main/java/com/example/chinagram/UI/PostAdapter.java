// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.UI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chinagram.Model.Post;
import com.example.chinagram.R;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>(); // Lista de publicaciones a mostrar

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    // Vinculo los datos de los Posts al ViewHolder en la posición dada
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        int resourceId = getResourceFromDrawable(post.imagenUrl); // Convierto drawableUrl a ID de recurso
        Log.d("PostAdapter", "Cargando imagen para post " + position + ": " + post.imagenUrl + " -> Resource ID: " + resourceId); // Log de depuración de proceso
        Glide.with(holder.itemView.getContext())
                .load(resourceId) // Cargo el recurso drawable
                .placeholder(R.drawable.placeholder) // Imagen temporal mientras carga
                .error(R.drawable.placeholder) // Imagen en caso de error
                .into(holder.postImageView); // Destino de la imagen
    }

    // Devuelvo el número total de ítems en la lista
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Establezco la lista de publicaciones reales y notifica cambios
    public void setPosts(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>(); // Evito null con lista vacía
        Log.d("PostAdapter", "Estableciendo " + this.posts.size() + " publicaciones reales"); // Log de depuración de proceso
        notifyDataSetChanged(); // Actualizo toda la vista
    }

    // Establezco publicaciones predeterminadas y notifica cambios
    public void setDefaultPosts(List<Post> defaultPosts) {
        this.posts = defaultPosts; // Asumo que los posts por defecto no son null
        Log.d("PostAdapter", "Estableciendo " + this.posts.size() + " publicaciones predeterminadas"); // Log de depuración de proceso
        notifyDataSetChanged(); // Actualizo toda la vista
    }

    // Método para convertir una URL drawable en un ID de recurso drawable
    private int getResourceFromDrawable(String drawableUrl) {
        Log.d("PostAdapter", "Resolviendo drawable: " + drawableUrl);
        switch (drawableUrl) {
            case "drawable://panda_feed": return R.drawable.panda_feed;
            case "drawable://rice_field": return R.drawable.rice_field;
            case "drawable://factory": return R.drawable.factory;
            case "drawable://xi_jinping": return R.drawable.xi_jinping;
            case "drawable://panda": return R.drawable.panda;
            case "drawable://china_flag": return R.drawable.china_flag;
            default: return R.drawable.placeholder;
        }
    }

    // Clase interna para el ViewHolder que contiene las vistas de cada ítem
    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView; // Imagen de la publicación

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.post_image); // Vinculo la vista
        }
    }
}