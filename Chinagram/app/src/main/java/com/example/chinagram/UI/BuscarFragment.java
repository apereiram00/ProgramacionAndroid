package com.example.chinagram.UI;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chinagram.GridSpacingItemDecoration; // Importa tu clase existente
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.SearchAdapter;
import com.example.chinagram.Model.Usuario;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentBuscarBinding;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BuscarFragment extends Fragment {

    private FragmentBuscarBinding binding;
    private PerfilViewModel perfilViewModel;
    private String currentUserId;
    private SearchAdapter searchAdapter;
    private MediaGridAdapter mediaGridAdapter; // Nuevo adaptador para la cuadrícula de medios

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        // Configurar RecyclerView para la cuadrícula de imágenes/videos
        setupMediaGrid();

        // Configurar RecyclerView para resultados de búsqueda
        searchAdapter = new SearchAdapter(new ArrayList<>(), user -> {
            Bundle args = new Bundle();
            args.putString("userId", user.usuarioId);
            Navigation.findNavController(view).navigate(R.id.action_buscarFragment_to_usuarioPerfilFragment, args);
        });
        binding.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchResultsRecyclerView.setAdapter(searchAdapter);

        // Buscar usuarios en tiempo real y alternar visibilidad
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.mediaGridRecyclerView.setVisibility(View.GONE); // Ocultar cuadrícula de medios
                    binding.searchResultsRecyclerView.setVisibility(View.VISIBLE); // Mostrar resultados de búsqueda
                    searchUsers(s.toString());
                } else {
                    binding.mediaGridRecyclerView.setVisibility(View.VISIBLE); // Mostrar cuadrícula de medios
                    binding.searchResultsRecyclerView.setVisibility(View.GONE); // Ocultar resultados de búsqueda
                    searchAdapter.setUsers(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupMediaGrid() {
        List<String> mediaUrls = new ArrayList<>();
        // Añadir URLs de las fotos (9 fotos)
        for (int i = 1; i <= 9; i++) {
            mediaUrls.add("https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1/object/public/galeria-buscador/public/img/foto" + i + ".jpg");
        }
        // Añadir URLs de los videos (5 reels)
        for (int i = 1; i <= 5; i++) {
            mediaUrls.add("https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1/object/public/galeria-buscador/public/videos/reel" + i + ".mp4");
        }

        mediaGridAdapter = new MediaGridAdapter(mediaUrls, url -> {
            // Lógica para abrir detalles del medio (si lo deseas)
        });
        binding.mediaGridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columnas
        binding.mediaGridRecyclerView.setAdapter(mediaGridAdapter);
        binding.mediaGridRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 8, true)); // 3 columnas, 8dp de espaciado, márgenes en bordes
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            searchAdapter.setUsers(new ArrayList<>());
            return;
        }
        perfilViewModel.searchUsersByName(query).observe(getViewLifecycleOwner(), usuarios -> {
            Log.d("BuscarFragment", "Usuarios encontrados en la búsqueda: " + (usuarios != null ? usuarios.size() : 0));
            if (usuarios != null) {
                searchAdapter.setUsers(usuarios);
            } else {
                Log.w("BuscarFragment", "No se encontraron usuarios o la lista es null");
                searchAdapter.setUsers(new ArrayList<>());
            }
        });
    }

    // Clase MediaGridAdapter (como sugerí antes)
    // Dentro de BuscarFragment.java, en la clase MediaGridAdapter
    private static class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.ViewHolder> {
        private final List<String> mediaUrls;
        private final OnItemClickListener listener;
        private final HashMap<Integer, SimpleExoPlayer> playerMap = new HashMap<>();

        MediaGridAdapter(List<String> mediaUrls, OnItemClickListener listener) {
            this.mediaUrls = mediaUrls;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_grid, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String url = mediaUrls.get(position);
            if (url.endsWith(".jpg")) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.playerView.setVisibility(View.GONE);
                if (playerMap.containsKey(Integer.valueOf(position))) {
                    SimpleExoPlayer player = playerMap.get(Integer.valueOf(position));
                    if (player != null) {
                        player.stop(); // Detener el reproductor si estaba activo
                        player.release(); // Liberar recursos
                        playerMap.remove(Integer.valueOf(position));
                    }
                }
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.xi_jinping)
                        .into(holder.imageView);
            } else if (url.endsWith(".mp4")) {
                holder.imageView.setVisibility(View.GONE);
                holder.playerView.setVisibility(View.VISIBLE);
                setupExoPlayer(holder, Integer.valueOf(position), url);
            }
        }

        private void setupExoPlayer(ViewHolder holder, Integer position, String url) {
            SimpleExoPlayer player = playerMap.get(position);
            if (player == null) {
                player = new SimpleExoPlayer.Builder(holder.itemView.getContext()).build();
                playerMap.put(position, player);
                holder.playerView.setPlayer(player);
            }

            // Configurar la fuente de medios (URL del video desde Supabase)
            MediaItem mediaItem = MediaItem.fromUri(url);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true); // Reproducir automáticamente
            player.setRepeatMode(Player.REPEAT_MODE_ALL); // Reproducir en bucle continuo
            player.setVolume(0f); // Silenciar los videos (volumen a 0)

            holder.playerView.setControllerAutoShow(false); // Evita que los controles se muestren automáticamente
            holder.playerView.setControllerHideOnTouch(false); // Evita que los controles aparezcan al tocar
            holder.playerView.setControllerShowTimeoutMs(0); // Desactiva el tiempo de visibilidad de los controles
            holder.playerView.setUseController(false); // Desactiva explícitamente el controlador
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            super.onViewRecycled(holder);
            int position = holder.getAdapterPosition();
            if (playerMap.containsKey(Integer.valueOf(position))) {
                SimpleExoPlayer player = playerMap.get(Integer.valueOf(position));
                if (player != null) {
                    player.stop();
                    player.release();
                    playerMap.remove(Integer.valueOf(position));
                }
            }
            if (holder.playerView != null) {
                holder.playerView.setPlayer(null);
            }
        }

        @Override
        public int getItemCount() {
            return mediaUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            PlayerView playerView;
            private SimpleExoPlayer player; // Mantenemos el campo para consistencia, pero no es necesario modificarlo aquí

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.media_image);
                playerView = itemView.findViewById(R.id.media_player);
            }

            void setPlayer(SimpleExoPlayer player) {
                this.player = player;
            }
        }

        interface OnItemClickListener {
            void onItemClick(String url);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}