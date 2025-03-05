// com.example.chinagram.UI.PerfilFragment.java
package com.example.chinagram.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.example.chinagram.GridSpacingItemDecoration;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.Post;
import com.example.chinagram.Model.PostAdapter;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentPerfilBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String userId;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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
                    perfilViewModel.uploadProfileImage(imageUri, userId);
                }
            }
        });

        binding.editarPerfilButton.setOnClickListener(v -> navegarEditarPerfil(v));
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.publicacionesRecyclerView.setLayoutManager(gridLayoutManager);
        postAdapter = new PostAdapter();
        binding.publicacionesRecyclerView.setAdapter(postAdapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        binding.publicacionesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));

        // Añadir clic en las publicaciones
        postAdapter.setOnItemClickListener(post -> {
            Bundle args = new Bundle();
            args.putParcelable("post", post);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_perfilFragment_to_detallesPostFragment, args);
        });
    }

    private void observeUserData() {
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                binding.nombreTextView.setText(usuario.nombre);
                binding.biografiaTextView.setText(usuario.biografia);
                binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores));
                binding.siguiendoTextView.setText(String.valueOf(usuario.siguiendo));

                Glide.with(this)
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? getResourceFromDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);
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

    private String formatSeguidores(int seguidores) {
        if (seguidores >= 1000000) {
            return String.format("%.1fM", seguidores / 1000000.0);
        } else if (seguidores >= 1000) {
            return String.format("%.1fK", seguidores / 1000.0);
        }
        return String.valueOf(seguidores);
    }

    private List<Post> getDefaultPosts() {
        List<Post> defaultPosts = new ArrayList<>();
        defaultPosts.add(new Post(userId, "drawable://panda_feed", "Panda en acción", System.currentTimeMillis(), null)); // Añadimos null para fileName
        defaultPosts.add(new Post(userId, "drawable://rice_field", "Arrozales gloriosos", System.currentTimeMillis(), null)); // Añadimos null para fileName
        defaultPosts.add(new Post(userId, "drawable://factory", "Producción al máximo", System.currentTimeMillis(), null)); // Añadimos null para fileName
        return defaultPosts;
    }

    private int getResourceFromDrawable(String drawableUrl) {
        switch (drawableUrl) {
            case "drawable://xi_jinping": return R.drawable.xi_jinping;
            case "drawable://panda": return R.drawable.panda;
            case "drawable://china_flag": return R.drawable.china_flag;
            case "drawable://panda_feed": return R.drawable.panda_feed;
            case "drawable://rice_field": return R.drawable.rice_field;
            case "drawable://factory": return R.drawable.factory;
            default: return R.drawable.placeholder;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}