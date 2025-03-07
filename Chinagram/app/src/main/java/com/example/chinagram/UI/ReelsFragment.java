package com.example.chinagram.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chinagram.Model.SharedPreferencesHelper; // Importar SharedPreferencesHelper
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentReelsBinding;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

public class ReelsFragment extends Fragment {

    private FragmentReelsBinding binding;
    private ReelsAdapter reelsAdapter;
    private List<String> reelUrls;
    private Handler progressHandler;
    private Runnable progressRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cargar URLs de los reels
        reelUrls = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            reelUrls.add("https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1/object/public/galeria-buscador/public/videos/reel" + i + ".mp4");
        }

        // Configurar RecyclerView con PagerSnapHelper
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.reelsRecyclerView.setLayoutManager(layoutManager);
        reelsAdapter = new ReelsAdapter(reelUrls, getContext()); // Pasar el contexto para SharedPreferencesHelper
        binding.reelsRecyclerView.setAdapter(reelsAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.reelsRecyclerView);

        // Manejar cambio de página para pausar/reproducir videos
        binding.reelsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View snapView = snapHelper.findSnapView(layoutManager);
                    if (snapView != null) {
                        int position = layoutManager.getPosition(snapView);
                        reelsAdapter.playVideoAtPosition(position);
                    }
                }
            }
        });

        // Inicializar el primer video
        reelsAdapter.playVideoAtPosition(0);

        // Configurar la barra de progreso con interactividad
        binding.reelProgressBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                ExoPlayer player = reelsAdapter.getPlayerAtPosition(currentPosition);
                if (player != null) {
                    float progress = event.getX() / v.getWidth();
                    long newPosition = (long) (player.getDuration() * progress);
                    player.seekTo(newPosition);
                }
                return true;
            }
            return false;
        });

        // Configurar la barra de progreso
        progressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                ExoPlayer player = reelsAdapter.getPlayerAtPosition(currentPosition);
                if (player != null) {
                    long duration = player.getDuration();
                    long position = player.getCurrentPosition();
                    if (duration > 0) {
                        int progress = (int) ((position * 100) / duration);
                        binding.reelProgressBar.setProgress(progress);
                        if (progress >= 100) {
                            // Reiniciar el video al finalizar
                            player.seekTo(0);
                            player.setPlayWhenReady(true);
                        }
                    }
                }
                progressHandler.postDelayed(this, 100); // Actualizar cada 100ms
            }
        };
        progressHandler.post(progressRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        reelsAdapter.pauseAllVideos();
        progressHandler.removeCallbacks(progressRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.reelsRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            reelsAdapter.playVideoAtPosition(currentPosition);
        }
        progressHandler.post(progressRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reelsAdapter.releaseAllPlayers();
        progressHandler.removeCallbacks(progressRunnable);
        binding = null;
    }
}

// Adaptador para los Reels
class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelViewHolder> {

    private final List<String> reelUrls;
    private final List<ExoPlayer> players = new ArrayList<>();
    private List<Boolean> isLikedList; // Cambiado a no final para permitir inicialización dinámica
    private int currentPlayingPosition = -1;
    private final SharedPreferencesHelper prefsHelper; // Instancia de SharedPreferencesHelper

    public ReelsAdapter(List<String> reelUrls, Context context) {
        this.reelUrls = reelUrls;
        this.prefsHelper = new SharedPreferencesHelper(context); // Inicializar SharedPreferencesHelper
        // Cargar los estados de "Me gusta" desde SharedPreferences
        this.isLikedList = prefsHelper.recuperarReelsLikes(reelUrls.size());
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        String reelUrl = reelUrls.get(position);

        // Configurar Jetpack Media3 ExoPlayer
        ExoPlayer player = new ExoPlayer.Builder(holder.itemView.getContext()).build();
        holder.playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(reelUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        players.add(position, player);

        // Configurar estado inicial del video
        player.setPlayWhenReady(false);

        // Configurar botón de "Me gusta"
        updateLikeIcon(holder, position);
        holder.likeButton.setOnClickListener(v -> {
            boolean isLiked = isLikedList.get(position);
            isLikedList.set(position, !isLiked);
            updateLikeIcon(holder, position);
            prefsHelper.guardarReelsLikes(isLikedList); // Guardar el estado en SharedPreferences
        });

        // Configurar botón de mute/desmute
        holder.muteButton.setImageResource(player.getVolume() > 0 ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);
        holder.muteButton.setOnClickListener(v -> {
            if (player.getVolume() > 0) {
                player.setVolume(0f);
                holder.muteButton.setImageResource(R.drawable.ic_volume_off);
            } else {
                player.setVolume(1f);
                holder.muteButton.setImageResource(R.drawable.ic_volume_on);
            }
        });
    }

    private void updateLikeIcon(ReelViewHolder holder, int position) {
        holder.likeButton.setImageTintList(isLikedList.get(position) ?
                holder.itemView.getResources().getColorStateList(R.color.red, null) :
                holder.itemView.getResources().getColorStateList(R.color.black, null));
    }

    @Override
    public int getItemCount() {
        return reelUrls.size();
    }

    public void playVideoAtPosition(int position) {
        if (position >= 0 && position < players.size()) {
            // Pausar todos los videos antes de reproducir el nuevo
            for (int i = 0; i < players.size(); i++) {
                ExoPlayer player = players.get(i);
                if (player != null) {
                    player.setPlayWhenReady(false);
                    if (i != position) {
                        player.seekTo(0); // Reiniciar solo los no seleccionados
                    }
                }
            }
            // Reproducir el video seleccionado
            ExoPlayer player = players.get(position);
            if (player != null) {
                player.setPlayWhenReady(true);
                currentPlayingPosition = position;
            }
        }
    }

    public void pauseAllVideos() {
        for (ExoPlayer player : players) {
            if (player != null) {
                player.setPlayWhenReady(false);
            }
        }
        currentPlayingPosition = -1;
    }

    public void releaseAllPlayers() {
        for (ExoPlayer player : players) {
            if (player != null) {
                player.release();
            }
        }
        players.clear();
        currentPlayingPosition = -1;
    }

    public ExoPlayer getPlayerAtPosition(int position) {
        if (position >= 0 && position < players.size()) {
            return players.get(position);
        }
        return null;
    }

    static class ReelViewHolder extends RecyclerView.ViewHolder {
        androidx.media3.ui.PlayerView playerView;
        ImageButton likeButton;
        ImageButton muteButton;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.reelPlayerView);
            likeButton = itemView.findViewById(R.id.likeButton);
            muteButton = itemView.findViewById(R.id.muteButton);
        }
    }
}