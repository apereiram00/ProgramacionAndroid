package com.example.chinagram.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.example.chinagram.GridSpacingItemDecoration;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.PostAdapter;
import com.example.chinagram.Model.UsuarioRepositorio; // Añadimos esta importación para IsFollowingCallback
import com.example.chinagram.R;
import com.example.chinagram.databinding.BusquedaPerfilBinding;
import com.example.chinagram.utils.DrawableUtils;
import com.google.firebase.auth.FirebaseAuth;

public class BusquedaPerfilFragment extends Fragment {

    private BusquedaPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String userId;
    private String currentUserId;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BusquedaPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userId = getArguments().getString("userId");
        if (userId == null) {
            userId = currentUserId; // Por defecto, mi perfil
        }
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        setupRecyclerView();
        observeUserData();
        setupFollowButton();
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
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_usuarioPerfilFragment_to_detallesPostFragment, args);
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
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? DrawableUtils.getResourceFromDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);
            }
        });

        perfilViewModel.getPostsByUser(userId).observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                binding.publicacionesRecyclerView.setVisibility(View.VISIBLE);
                postAdapter.setPosts(posts);
            } else {
                binding.publicacionesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void setupFollowButton() {
        if (currentUserId.equals(userId)) {
            // Si es mi propio perfil, oculto o deshabilito el botón de seguir
            binding.followButton.setVisibility(View.GONE);
            return;
        }

        // Cambiado a UsuarioRepositorio.IsFollowingCallback
        perfilViewModel.isFollowing(currentUserId, userId, isFollowing -> {
            if (isFollowing) {
                binding.followButton.setText("Dejar de Seguir");
                binding.followButton.setOnClickListener(v -> unfollowUser());
            } else {
                binding.followButton.setText("Seguir");
                binding.followButton.setOnClickListener(v -> followUser());
            }
        });
    }

    private void followUser() {
        perfilViewModel.followUser(currentUserId, userId, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                requireActivity().runOnUiThread(() -> {
                    binding.followButton.setText("Dejar de Seguir");
                    binding.followButton.setOnClickListener(v -> unfollowUser());
                    updateFollowerCount(); // Actualizo contadores después de seguir
                });
            }

            @Override
            public void onError(String error) {
                Log.e("BusquedaPerfilFragment", "Error al seguir usuario: " + error); // Cambiado el tag a "BusquedaPerfilFragment"
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error al seguir: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unfollowUser() {
        perfilViewModel.unfollowUser(currentUserId, userId, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                requireActivity().runOnUiThread(() -> {
                    binding.followButton.setText("Seguir");
                    binding.followButton.setOnClickListener(v -> followUser());
                    updateFollowerCount(); // Actualizo contadores después de dejar de seguir
                });
            }

            @Override
            public void onError(String error) {
                Log.e("BusquedaPerfilFragment", "Error al dejar de seguir usuario: " + error); // Cambiado el tag a "BusquedaPerfilFragment"
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error al dejar de seguir: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateFollowerCount() {
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores));
            }
        });
    }

    private String formatSeguidores(int seguidores) {
        if (seguidores >= 1000000) {
            return String.format("%.1fM", seguidores / 1000000.0);
        } else if (seguidores >= 1000) {
            return String.format("%.1fK", seguidores / 1000000.0);
        }
        return String.valueOf(seguidores);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}