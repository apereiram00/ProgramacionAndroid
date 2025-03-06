package com.example.chinagram.UI;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.chinagram.GridSpacingItemDecoration;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.SearchAdapter;
import com.example.chinagram.Model.Usuario;
import com.example.chinagram.Model.UsuarioRepositorio;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentBuscarBinding;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BuscarFragment extends Fragment {

    private FragmentBuscarBinding binding;
    private PerfilViewModel perfilViewModel;
    private String currentUserId;
    private SearchAdapter searchAdapter;
    private MediaGridAdapter mediaGridAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usuarioRepositorio = new UsuarioRepositorio(requireContext());
        currentUserId = mAuth.getCurrentUser().getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        // Configuro RecyclerView para la cuadrícula de imágenes/videos
        setupMediaGrid();

        // Configuro RecyclerView para resultados de búsqueda
        searchAdapter = new SearchAdapter(new ArrayList<>(), user -> {
            Bundle args = new Bundle();
            args.putString("userId", user.usuarioId);
            Navigation.findNavController(view).navigate(R.id.action_buscarFragment_to_usuarioPerfilFragment, args);
        });
        binding.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchResultsRecyclerView.setAdapter(searchAdapter);

        // Busco usuarios en tiempo real y alterno visibilidad
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (!query.isEmpty()) {
                    binding.mediaGridRecyclerView.setVisibility(View.GONE);
                    binding.searchResultsRecyclerView.setVisibility(View.VISIBLE);
                    checkRestrictedTerms(query);
                    if (!isAccountRestricted()) {
                        searchUsers(query);
                    }
                } else {
                    binding.mediaGridRecyclerView.setVisibility(View.VISIBLE);
                    binding.searchResultsRecyclerView.setVisibility(View.GONE);
                    searchAdapter.setUsers(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean isAccountRestricted() {
        return false; // Simplificado, se manejará en el flujo de restrictAccount
    }

    private void setupMediaGrid() {
        List<String> mediaUrls = new ArrayList<>();
        // Añado URLs de las fotos (9 fotos)
        for (int i = 1; i <= 9; i++) {
            mediaUrls.add("https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1/object/public/galeria-buscador/public/fotos/foto" + i + ".jpg");
        }
        // Añado URLs de los videos (5 reels)
        for (int i = 1; i <= 5; i++) {
            mediaUrls.add("https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1/object/public/galeria-buscador/public/videos/reel" + i + ".mp4");
        }

        binding.mediaGridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columnas
        binding.mediaGridRecyclerView.setAdapter(mediaGridAdapter);
        binding.mediaGridRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 8, true)); // 3 columnas, 8dp de espaciado, márgenes en bordes
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            Log.d("BuscarFragment", "Query vacía, limpiando resultados");
            searchAdapter.setUsers(new ArrayList<>());
            return;
        }
        Log.d("BuscarFragment", "Iniciando búsqueda con query: " + query);
        perfilViewModel.searchUsersByName(query).observe(getViewLifecycleOwner(), usuarios -> {
            Log.d("BuscarFragment", "Usuarios encontrados en la búsqueda: " + (usuarios != null ? usuarios.size() : 0));
            if (usuarios != null) {
                for (Usuario usuario : usuarios) {
                    Log.d("BuscarFragment", "Usuario en resultados: " + usuario.nombre + " (ID: " + usuario.usuarioId + ")");
                }
                searchAdapter.setUsers(usuarios);
            } else {
                Log.w("BuscarFragment", "No se encontraron usuarios o la lista es null");
                searchAdapter.setUsers(new ArrayList<>());
            }
        });
    }

    private void checkRestrictedTerms(String query) {
        List<String> restrictedTerms = new ArrayList<>();
        restrictedTerms.add("twitter");
        restrictedTerms.add("facebook");
        restrictedTerms.add("vpn gratis");

        for (String term : restrictedTerms) {
            if (query.contains(term)) {
                Log.w("BuscarFragment", "Término restringido detectado: " + term);
                restrictAccount();
                return;
            }
        }
    }

    private void restrictAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("usuarios").document(userId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("BuscarFragment", "Datos del usuario eliminados de Firestore");
                        usuarioRepositorio.deleteUser(userId);
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("BuscarFragment", "Cuenta eliminada con éxito de Authentication");
                                        if (isAdded()) {
                                            new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                                                    .setMessage("Tu cuenta ha sido restringida por violar las políticas de Chinagram.")
                                                    .setPositiveButton("Entendido", (dialog, which) -> {
                                                        dialog.dismiss();
                                                        NavController navController = Navigation.findNavController(requireView());
                                                        NavOptions navOptions = new NavOptions.Builder()
                                                                .setPopUpTo(R.id.homeFragment, true)
                                                                .build();
                                                        try {
                                                            navController.navigate(R.id.loginFragment, null, navOptions);
                                                        } catch (IllegalArgumentException e) {
                                                            Log.e("BuscarFragment", "Error al navegar: " + e.getMessage());
                                                        }
                                                    })
                                                    .show();
                                        }
                                    } else {
                                        Log.w("BuscarFragment", "Error al eliminar cuenta de Authentication", task.getException());
                                        if (isAdded()) {
                                            new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                                                    .setMessage("Error al restringir cuenta: " + task.getException().getMessage())
                                                    .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                                                    .show();
                                        }
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BuscarFragment", "Error al eliminar datos de Firestore: " + e.getMessage());
                        if (isAdded()) {
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                                    .setMessage("Error al restringir cuenta: " + e.getMessage())
                                    .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    });
        } else {
            Log.e("BuscarFragment", "Usuario no autenticado al intentar restringir");
            if (isAdded()) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                        .setMessage("No hay usuario autenticado para restringir")
                        .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

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
                        player.stop();
                        player.release();
                        playerMap.remove(Integer.valueOf(position));
                    }
                }
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.xi_jinping) // Mantengo como fallback
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    Log.e("Glide", "Error al cargar imagen: " + url + ", " + e.getMessage());
                                    Toast.makeText(holder.itemView.getContext(), "Error al cargar imagen: " + url, Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "Imagen cargada correctamente: " + url);
                                return false;
                            }
                        })
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

            // Configurar la fuente de medios
            MediaItem mediaItem = MediaItem.fromUri(url);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setVolume(0f);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    if (error.getCause() instanceof HttpDataSource.HttpDataSourceException) {
                        Log.e("ExoPlayer", "Error al cargar video: " + url + ", " + error.getMessage());
                        Toast.makeText(holder.itemView.getContext(), "Error al cargar video: " + url, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.playerView.setControllerAutoShow(false);
            holder.playerView.setControllerHideOnTouch(false);
            holder.playerView.setControllerShowTimeoutMs(0);
            holder.playerView.setUseController(false);
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

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.media_image);
                playerView = itemView.findViewById(R.id.media_player);
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