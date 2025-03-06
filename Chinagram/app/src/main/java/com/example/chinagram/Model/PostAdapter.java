// com.example.chinagram.Model.PostAdapter.java
package com.example.chinagram.Model;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chinagram.R;
import com.example.chinagram.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public PostAdapter() {
        this.posts = new ArrayList<>();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(holder.getAdapterPosition());

        if (post.imagenUrl != null && !post.imagenUrl.isEmpty()) {
            if (post.imagenUrl.startsWith("http://") || post.imagenUrl.startsWith("https://")) {
                // Cargar URL remota (Supabase) directamente con Glide
                Glide.with(holder.itemView.getContext())
                        .load(post.imagenUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.postImageView);
            } else {
                // Cargar drawable local (por ejemplo, publicaciones predeterminadas)
                int resourceId = getResourceFromDrawable(post.imagenUrl);
                if (resourceId != R.drawable.placeholder) { // Verificar si el recurso existe
                    Log.d("PostAdapter", "Cargando drawable local: " + post.imagenUrl + " -> " + resourceId);
                    Glide.with(holder.itemView.getContext())
                            .load(resourceId)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(holder.postImageView);
                } else {
                    Log.w("PostAdapter", "Drawable no encontrado para: " + post.imagenUrl);
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.placeholder)
                            .into(holder.postImageView);
                }
            }
        } else {
            Log.e("PostAdapter", "imagenUrl es null o vacío para el post en posición: " + position);
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.placeholder)
                    .into(holder.postImageView);
        }

        // Solo hacer clicable las publicaciones con URLs remotas (no predeterminadas)
        if (post.imagenUrl != null && (post.imagenUrl.startsWith("http://") || post.imagenUrl.startsWith("https://"))) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(post);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null); // Desactivar clic para publicaciones predeterminadas
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = (posts != null) ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private int getResourceFromDrawable(String drawableUrl) {
        return DrawableUtils.getResourceFromDrawable(drawableUrl);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.post_image);
        }
    }
}