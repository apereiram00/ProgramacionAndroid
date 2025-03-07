package com.example.chinagram.UI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.chinagram.GridSpacingItemDecoration;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.Post;
import com.example.chinagram.Model.PostAdapter;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentPerfilBinding;
import com.example.chinagram.utils.DrawableUtils;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String userId;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private PostAdapter postAdapter;
    private SeguidoresAdapter seguidoresAdapter;
    private boolean isShowingFollowers = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        perfilViewModel.ensureUserExists(userId);

        setupRecyclerView();
        observeUserData();
        observeUserPosts();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    perfilViewModel.uploadProfileImage(imageUri, userId, null);
                }
            }
        });

        binding.editarPerfilButton.setOnClickListener(v -> navegarEditarPerfil(v));

        // Configurar el RecyclerView para seguidores
        binding.recyclerViewSeguidores.setLayoutManager(new LinearLayoutManager(getContext()));
        seguidoresAdapter = new SeguidoresAdapter(perfilViewModel);
        binding.recyclerViewSeguidores.setAdapter(seguidoresAdapter);

        // Asegurar que los TextView sean interactivos
        binding.seguidoresTextView.setClickable(true);
        binding.seguidoresTextView.setLongClickable(true);
        binding.siguiendoTextView.setClickable(true);
        binding.siguiendoTextView.setLongClickable(true);

        // Eventos de pulsación larga con depuración
        binding.seguidoresTextView.setOnLongClickListener(v -> {
            Log.d("PerfilFragment", "Long click detected on seguidoresTextView");
            showFollowersList();
            return true;
        });
        binding.seguidoresTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP && isShowingFollowers) {
                Log.d("PerfilFragment", "Touch up detected on seguidoresTextView, hiding list");
                hideFollowersList();
            }
            return false;
        });

        binding.siguiendoTextView.setOnLongClickListener(v -> {
            Log.d("PerfilFragment", "Long click detected on siguiendoTextView");
            showFollowingList();
            return true;
        });
        binding.siguiendoTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP && isShowingFollowers) {
                Log.d("PerfilFragment", "Touch up detected on siguiendoTextView, hiding list");
                hideFollowersList();
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.publicacionesRecyclerView.setLayoutManager(gridLayoutManager);
        postAdapter = new PostAdapter();
        binding.publicacionesRecyclerView.setAdapter(postAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        binding.publicacionesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));

        postAdapter.setOnItemClickListener(post -> {
            Bundle args = new Bundle();
            args.putParcelable("post", post);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_perfilFragment_to_detallesPostFragment, args);
        });
    }

    private void observeUserData() {
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                if (getArguments() == null || !getArguments().containsKey("nuevaFotoUrl")) {
                    Glide.with(this)
                            .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? DrawableUtils.obtenerRecursoDesdeDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(binding.fotoPerfilImageView);
                }
                binding.nombreTextView.setText(usuario.nombre);
                binding.biografiaTextView.setText(usuario.biografia);
                binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores));
                binding.siguiendoTextView.setText(String.valueOf(usuario.siguiendo));
                binding.publicacionesTextView.setText(String.valueOf(usuario.publicaciones));
                Log.d("PerfilFragment", "Datos actualizados - Seguidores: " + usuario.seguidores + ", Siguiendo: " + usuario.siguiendo);
            }
        });
    }

    private void observeUserPosts() {
        perfilViewModel.getPostsByUser(userId).observe(getViewLifecycleOwner(), posts -> {
            Log.d("PerfilFragment", "Posts obtenidos: " + (posts != null ? posts.size() : "null"));
            if (posts != null && !posts.isEmpty()) {
                binding.publicacionesTextView.setText(String.valueOf(posts.size()));
                postAdapter.setPosts(posts);
            } else {
                Log.d("PerfilFragment", "No hay posts, mostrando posts predeterminados para userId: " + userId);
                binding.publicacionesTextView.setText("0");
                postAdapter.setPosts(getDefaultPosts());
            }
        });
    }

    private void navegarEditarPerfil(View v) {
        NavController navController = Navigation.findNavController(v);
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build();
        navController.navigate(R.id.action_perfilFragment_to_editarPerfilFragment, null, navOptions);
    }

    private List<Post> getDefaultPosts() {
        List<Post> defaultPosts = new ArrayList<>();
        defaultPosts.add(new Post(userId, "drawable://panda_feed", "Panda en acción", System.currentTimeMillis(), null));
        defaultPosts.add(new Post(userId, "drawable://rice_field", "Arrozales gloriosos", System.currentTimeMillis(), null));
        defaultPosts.add(new Post(userId, "drawable://factory", "Producción al máximo", System.currentTimeMillis(), null));
        return defaultPosts;
    }

    private String formatSeguidores(int seguidores) {
        if (seguidores >= 1000000) {
            return String.format("%.1fM", seguidores / 1000000.0);
        } else if (seguidores >= 1000) {
            return String.format("%.1fK", seguidores / 1000.0);
        } else {
            return String.valueOf(seguidores);
        }
    }

    private void showFollowersList() {
        isShowingFollowers = true;
        binding.recyclerViewSeguidores.setVisibility(View.VISIBLE);
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null && usuario.seguidoresList != null) {
                seguidoresAdapter.setFollowerIds(new ArrayList<>(usuario.seguidoresList), true);
            }
        });
    }

    private void showFollowingList() {
        isShowingFollowers = true;
        binding.recyclerViewSeguidores.setVisibility(View.VISIBLE);
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null && usuario.siguiendoList != null) {
                seguidoresAdapter.setFollowerIds(new ArrayList<>(usuario.siguiendoList), false);
            }
        });
    }

    private void hideFollowersList() {
        isShowingFollowers = false;
        binding.recyclerViewSeguidores.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

// Adaptador para la lista de seguidores/seguidos
class SeguidoresAdapter extends RecyclerView.Adapter<SeguidoresAdapter.ViewHolder> {

    private List<String> userIds = new ArrayList<>();
    private PerfilViewModel perfilViewModel;
    private boolean isFollowersList;

    public SeguidoresAdapter(PerfilViewModel perfilViewModel) {
        this.perfilViewModel = perfilViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seguidor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userId = userIds.get(position);
        perfilViewModel.getUsuario(userId).observeForever(usuario -> {
            if (usuario != null) {
                holder.seguidorNombreTextView.setText(usuario.nombre != null ? usuario.nombre : "Usuario desconocido");
            } else {
                holder.seguidorNombreTextView.setText("Cargando...");
            }
        });
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    public void setFollowerIds(List<String> userIds, boolean isFollowers) {
        this.userIds = userIds;
        this.isFollowersList = isFollowers;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView seguidorNombreTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            seguidorNombreTextView = itemView.findViewById(R.id.seguidorNombreTextView);
        }
    }
}