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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.example.chinagram.GridSpacingItemDecoration;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.PostAdapter;
import com.example.chinagram.Model.Usuario;
import com.example.chinagram.Model.UsuarioRepositorio;
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
    private boolean isFollowing = false; // Estado local para el botón

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BusquedaPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener userId y currentUserId
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userId = getArguments() != null ? getArguments().getString("userId") : null;
        if (userId == null) {
            userId = currentUserId; // Por defecto, mi perfil
        }

        // Inicializar ViewModel con Factory
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
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_busquedaPerfilFragment_to_detallesPostFragment, args);
        });
    }

    private void observeUserData() {
        final MutableLiveData<Usuario> usuarioLiveData = new MutableLiveData<>();
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                usuarioLiveData.setValue(usuario); // Usar MutableLiveData para control
                binding.nombreTextView.setText(usuario.nombre);
                binding.biografiaTextView.setText(usuario.biografia);
                binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores)); // Asegurar que se actualice
                binding.siguiendoTextView.setText(String.valueOf(usuario.siguiendo));     // Asegurar que se actualice
                binding.publicacionesTextView.setText(String.valueOf(usuario.publicaciones));

                Glide.with(this)
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? DrawableUtils.obtenerRecursoDesdeDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);

                // Solo establecer el estado inicial una vez
                if (!isInitialStateSet) {
                    perfilViewModel.isFollowing(currentUserId, userId, isFollowing -> {
                        this.isFollowing = isFollowing;
                        updateFollowButton();
                        isInitialStateSet = true;
                    });
                }
            }
        });

        perfilViewModel.getPostsByUser(userId).observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                binding.publicacionesRecyclerView.setVisibility(View.VISIBLE);
                postAdapter.setPosts(posts);
                binding.publicacionesTextView.setText(String.valueOf(posts.size()));
            } else {
                binding.publicacionesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private boolean isInitialStateSet = false;

    private void setupFollowButton() {
        if (currentUserId.equals(userId)) {
            binding.followButton.setVisibility(View.GONE);
            return;
        }

        // Verificar el estado inicial de seguimiento
        perfilViewModel.isFollowing(currentUserId, userId, isFollowing -> {
            this.isFollowing = isFollowing;
            updateFollowButton();
        });

        // Configurar el listener del botón
        binding.followButton.setOnClickListener(v -> {
            if (isFollowing) {
                unfollowUser();
            } else {
                followUser();
            }
        });
    }

    private void updateFollowButton() {
        binding.followButton.setText(isFollowing ? "Dejar de Seguir" : "Seguir");
    }

    private void followUser() {
        if (isFollowing) return; // Evitar múltiples seguimientos
        isFollowing = true;
        updateFollowButton();
        perfilViewModel.followUser(currentUserId, userId, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                requireActivity().runOnUiThread(() -> {
                    // Actualizar UI solo una vez
                    perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
                        if (usuario != null) {
                            binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores));
                        }
                    });
                    Log.d("BusquedaPerfilFragment", "Usuario seguido correctamente");
                });
            }

            @Override
            public void onError(String error) {
                Log.e("BusquedaPerfilFragment", "Error al seguir usuario: " + error);
                requireActivity().runOnUiThread(() -> {
                    isFollowing = false;
                    updateFollowButton();
                    Toast.makeText(requireContext(), "Error al seguir: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unfollowUser() {
        if (!isFollowing) return; // Evitar múltiples unfollows
        isFollowing = false;
        updateFollowButton();
        perfilViewModel.unfollowUser(currentUserId, userId, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                requireActivity().runOnUiThread(() -> {
                    // Actualizar UI solo una vez
                    perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
                        if (usuario != null) {
                            binding.seguidoresTextView.setText(formatSeguidores(usuario.seguidores));
                        }
                    });
                    Log.d("BusquedaPerfilFragment", "Usuario dejado de seguir correctamente");
                });
            }

            @Override
            public void onError(String error) {
                Log.e("BusquedaPerfilFragment", "Error al dejar de seguir usuario: " + error);
                requireActivity().runOnUiThread(() -> {
                    isFollowing = true;
                    updateFollowButton();
                    Toast.makeText(requireContext(), "Error al dejar de seguir: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateFollowerCount(boolean isFollowing) {
        // Evitar actualizaciones repetidas
        if (isUpdating) return;
        isUpdating = true;
        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                int newFollowers = isFollowing ? usuario.seguidores + 1 : usuario.seguidores - 1;
                binding.seguidoresTextView.setText(formatSeguidores(newFollowers));
                if (isFollowing && !usuario.seguidoresList.contains(currentUserId)) {
                    perfilViewModel.updateFollowers(userId, newFollowers, new UsuarioRepositorio.UpdateCallback() {
                        @Override
                        public void onUpdateComplete() {
                            Log.d("BusquedaPerfilFragment", "Seguidores actualizados correctamente para " + userId);
                            isUpdating = false;
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("BusquedaPerfilFragment", "Error al actualizar seguidores: " + error);
                            Toast.makeText(requireContext(), "Error al actualizar seguidores: " + error, Toast.LENGTH_SHORT).show();
                            isUpdating = false;
                        }
                    });
                } else if (!isFollowing && usuario.seguidoresList.contains(currentUserId)) {
                    perfilViewModel.updateFollowers(userId, newFollowers, new UsuarioRepositorio.UpdateCallback() {
                        @Override
                        public void onUpdateComplete() {
                            Log.d("BusquedaPerfilFragment", "Seguidores actualizados correctamente para " + userId);
                            isUpdating = false;
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("BusquedaPerfilFragment", "Error al actualizar seguidores: " + error);
                            Toast.makeText(requireContext(), "Error al actualizar seguidores: " + error, Toast.LENGTH_SHORT).show();
                            isUpdating = false;
                        }
                    });
                } else {
                    isUpdating = false;
                }
            }
        });
    }

    private boolean isUpdating = false;

    private String formatSeguidores(int seguidores) {
        if (seguidores >= 1000000) {
            return String.format("%.1fM", seguidores / 1000000.0);
        } else if (seguidores >= 1000) {
            return String.format("%.1fK", seguidores / 1000.0);
        }
        return String.valueOf(seguidores);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}