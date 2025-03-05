// com.example.chinagram.UI.DetallesPostFragment.java
package com.example.chinagram.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.chinagram.Model.Comment;
import com.example.chinagram.Model.Post;
import com.example.chinagram.Model.PostViewModel;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentDetallesPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class DetallesPostFragment extends Fragment {

    private FragmentDetallesPostBinding binding;
    private PostViewModel postViewModel;
    private String usuarioId; // Usamos usuarioId para consistencia con tus modelos
    private Post currentPost;
    private boolean isLiked = false;
    private List<Comment> comments = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetallesPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Obtener el Post desde los argumentos
        currentPost = getArguments().getParcelable("post");
        if (currentPost == null) {
            Log.e("DetallesPostFragment", "No se recibió un Post válido");
            return;
        }

        setupPostDetails();
        setupLikeButton();
        setupCommentButton();
        setupDeleteButton();
        loadComments();
    }

    private void setupPostDetails() {
        // Mostrar la imagen del post
        Glide.with(this)
                .load(currentPost.imagenUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.postImage);

        // Mostrar el título (usamos "descripcion" como definiste en Post)
        binding.postTitle.setText(currentPost.descripcion);

        // Cargar estado inicial de "Me gusta" (persistente, desde Room)
        checkLikeStatus();
    }

    private void setupLikeButton() {
        binding.likeButton.setOnClickListener(v -> {
            isLiked = !isLiked;
            updateLikeIcon();
            postViewModel.toggleLike(usuarioId, currentPost.postId, isLiked);
            Log.d("DetallesPostFragment", "Me gusta actualizado por usuarioId=" + usuarioId + ": " + isLiked);
            updateLikeCount(); // Actualizar el conteo de Me gusta (implementaremos esto)
        });
    }

    private void setupCommentButton() {
        binding.addCommentButton.setOnClickListener(v -> {
            String comment = binding.commentEditText.getText().toString().trim();
            if (!comment.isEmpty()) {
                postViewModel.addComment(usuarioId, currentPost.postId, comment);
                binding.commentEditText.setText(""); // Limpiar el campo
                Log.d("DetallesPostFragment", "Comentario añadido por usuarioId=" + usuarioId + ": " + comment);
            }
        });
    }

    private void setupDeleteButton() {
        // Solo mostrar el botón de eliminar si el usuario logueado es el propietario
        if (usuarioId.equals(currentPost.usuarioId)) {
            binding.deletePostButton.setVisibility(View.VISIBLE);
            binding.deletePostButton.setOnClickListener(v -> {
                postViewModel.deletePost(currentPost.postId, success -> {
                    if (success) {
                        Log.d("DetallesPostFragment", "Publicación eliminada exitosamente por usuarioId=" + usuarioId + ", regresando al perfil");
                        NavController navController = Navigation.findNavController(binding.getRoot());
                        // Verificar si PerfilFragment está en el back stack antes de pop
                        if (navController.getBackStackEntry(R.id.perfilFragment) != null) {
                            navController.popBackStack(R.id.perfilFragment, false); // Navegar de vuelta a PerfilFragment
                        } else {
                            Log.w("DetallesPostFragment", "PerfilFragment no está en el back stack, navegando directamente");
                            // Navegar explícitamente a PerfilFragment si no está en el back stack
                            Bundle args = new Bundle();
                            args.putString("userId", currentPost.usuarioId); // Pasar el usuarioId del perfil
                            navController.navigate(R.id.perfilFragment, args);
                        }
                    } else {
                        Log.e("DetallesPostFragment", "Error al eliminar la publicación");
                    }
                });
            });
        } else {
            binding.deletePostButton.setVisibility(View.GONE);
        }
    }

    private void checkLikeStatus() {
        postViewModel.getLikeStatus(usuarioId, currentPost.postId).observe(getViewLifecycleOwner(), isLiked -> {
            if (isLiked != null) {
                this.isLiked = isLiked;
                updateLikeIcon();
                updateLikeCount(); // Actualizar el conteo de Me gusta también
            }
        });
    }

    private void updateLikeIcon() {
        binding.likeButton.setImageTintList(isLiked ?
                getResources().getColorStateList(R.color.red, null) : // Coloreado (rojo)
                getResources().getColorStateList(R.color.black, null)); // Descoloreado (negro)
    }

    private void updateLikeCount() {
        postViewModel.getLikeCount(currentPost.postId).observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.likeCountText.setText(count + " Me gusta");
            } else {
                binding.likeCountText.setText("0 Me gusta");
            }
        });
    }

    private void loadComments() {
        postViewModel.getComments(currentPost.postId).observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                this.comments.clear();
                this.comments.addAll(comments);
                updateCommentsView();
            }
        });
    }

    // com.example.chinagram.UI.DetallesPostFragment.java
    private void updateCommentsView() {
        StringBuilder commentsText = new StringBuilder("\n");
        for (Comment comment : comments) {
            // Obtener el nombre actual del usuario usando usuarioId desde Usuario
            postViewModel.getUsuarioName(comment.usuarioId).observe(getViewLifecycleOwner(), nombre -> {
                if (nombre != null) {
                    commentsText.append(nombre).append(": ").append(comment.text).append("\n");
                    binding.commentsTextView.setText(commentsText.toString());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}