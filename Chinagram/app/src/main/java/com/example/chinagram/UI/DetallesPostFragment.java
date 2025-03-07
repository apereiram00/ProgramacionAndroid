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
import java.util.Objects;

public class DetallesPostFragment extends Fragment {

    private FragmentDetallesPostBinding binding;
    private PostViewModel postViewModel;
    private String idUsuario; // Renombrado: usuarioId -> idUsuario
    private Post publicacionActual; // Renombrado: currentPost -> publicacionActual
    private boolean estaLiked = false; // Renombrado: isLiked -> estaLiked
    private final List<Comment> comentarios = new ArrayList<>(); // Renombrado: comments -> comentarios

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetallesPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Obtengo la publicación desde los argumentos
        assert getArguments() != null;
        publicacionActual = getArguments().getParcelable("post");
        if (publicacionActual == null) {
            Log.e("DetallesPostFragment", "No se recibió una publicación válida");
            return;
        }

        configurarDetallesPublicacion(); // Renombrado: setupPostDetails -> configurarDetallesPublicacion
        configurarBotonMeGusta(); // Renombrado: setupLikeButton -> configurarBotonMeGusta
        configurarBotonComentario(); // Renombrado: setupCommentButton -> configurarBotonComentario
        configurarBotonEliminar(); // Renombrado: setupDeleteButton -> configurarBotonEliminar
        cargarComentarios(); // Renombrado: loadComments -> cargarComentarios
    }

    private void configurarDetallesPublicacion() {
        // Muestro la imagen de la publicación
        Glide.with(this)
                .load(publicacionActual.imagenUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.postImage);

        // Muestro el título (usamos "descripcion" como definiste en Post)
        binding.postTitle.setText(publicacionActual.descripcion);

        // Cargo estado inicial de "Me gusta"
        verificarEstadoMeGusta(); // Renombrado: checkLikeStatus -> verificarEstadoMeGusta
    }

    private void configurarBotonMeGusta() {
        binding.likeButton.setOnClickListener(v -> {
            estaLiked = !estaLiked;
            actualizarIconoMeGusta(); // Renombrado: updateLikeIcon -> actualizarIconoMeGusta
            postViewModel.toggleLike(idUsuario, publicacionActual.postId, estaLiked);
            Log.d("DetallesPostFragment", "Me gusta actualizado por idUsuario=" + idUsuario + ": " + estaLiked);
            actualizarConteoMeGusta(); // Renombrado: updateLikeCount -> actualizarConteoMeGusta
        });
    }

    private void configurarBotonComentario() {
        binding.addCommentButton.setOnClickListener(v -> {
            String comentario = binding.commentEditText.getText().toString().trim(); // Renombrado: comment -> comentario
            if (!comentario.isEmpty()) {
                postViewModel.addComment(idUsuario, publicacionActual.postId, comentario);
                binding.commentEditText.setText("");
                Log.d("DetallesPostFragment", "Comentario añadido por idUsuario=" + idUsuario + ": " + comentario);
            }
        });
    }

    private void configurarBotonEliminar() {
        // Solo muestro el botón de eliminar si el usuario logueado es el propietario
        if (idUsuario.equals(publicacionActual.usuarioPostId)) {
            binding.deletePostButton.setVisibility(View.VISIBLE);
            binding.deletePostButton.setOnClickListener(v -> {
                postViewModel.deletePost(publicacionActual.postId, exito -> { // Renombrado: success -> exito
                    if (exito) {
                        Log.d("DetallesPostFragment", "Publicación eliminada exitosamente por idUsuario=" + idUsuario + ", regresando al perfil");
                        NavController navController = Navigation.findNavController(binding.getRoot());
                        navController.getBackStackEntry(R.id.perfilFragment);
                        navController.popBackStack(R.id.perfilFragment, false);
                    } else {
                        Log.e("DetallesPostFragment", "Error al eliminar la publicación");
                    }
                });
            });
        } else {
            binding.deletePostButton.setVisibility(View.GONE);
        }
    }

    private void verificarEstadoMeGusta() {
        postViewModel.getLikeStatus(idUsuario, publicacionActual.postId).observe(getViewLifecycleOwner(), estaLiked -> {
            if (estaLiked != null) {
                this.estaLiked = estaLiked;
                actualizarIconoMeGusta();
                actualizarConteoMeGusta();
            }
        });
    }

    private void actualizarIconoMeGusta() {
        binding.likeButton.setImageTintList(estaLiked ?
                getResources().getColorStateList(R.color.red, null) :
                getResources().getColorStateList(R.color.black, null));
    }

    private void actualizarConteoMeGusta() {
        postViewModel.getLikeCount(publicacionActual.postId).observe(getViewLifecycleOwner(), conteo -> { // Renombrado: count -> conteo
            if (conteo != null) {
                binding.likeCountText.setText(conteo + " Me gusta");
            } else {
                binding.likeCountText.setText("0 Me gusta");
            }
        });
    }

    private void cargarComentarios() {
        postViewModel.getComments(publicacionActual.postId).observe(getViewLifecycleOwner(), comentarios -> {
            if (comentarios != null) {
                this.comentarios.clear();
                this.comentarios.addAll(comentarios);
                actualizarVistaComentarios(); // Renombrado: updateCommentsView -> actualizarVistaComentarios
            }
        });
    }

    private void actualizarVistaComentarios() {
        StringBuilder textoComentarios = new StringBuilder("\n"); // Renombrado: commentsText -> textoComentarios
        for (Comment comentario : comentarios) {
            postViewModel.getUsuarioName(comentario.usuarioCommentId).observe(getViewLifecycleOwner(), nombre -> {
                if (nombre != null) {
                    textoComentarios.append(nombre).append(": ").append(comentario.text).append("\n");
                    binding.commentsTextView.setText(textoComentarios.toString());
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