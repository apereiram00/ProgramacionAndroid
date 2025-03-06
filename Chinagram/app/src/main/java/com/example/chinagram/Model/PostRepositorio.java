// com.example.chinagram.Model.PostRepositorio.java
package com.example.chinagram.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostRepositorio {
    private final PostDAO postDAO;
    private final LikeDAO likeDAO;
    private final CommentDAO commentDAO;
    private final UsuarioDAO usuarioDAO;
    private final ExecutorService executorService;
    private final Context context;

    public interface UpdateCallback {
        void onUpdateComplete();
        void onError(String error);
    }

    public interface DeleteCallback {
        void onDeleteComplete(boolean success);
    }

    public PostRepositorio(Context context) {
        ChinaDataBase database = ChinaDataBase.getDatabase(context);
        postDAO = database.postDAO();
        likeDAO = database.likeDAO();
        commentDAO = database.commentDAO();
        usuarioDAO = database.usuarioDAO();
        executorService = Executors.newFixedThreadPool(4);
        this.context = context.getApplicationContext();
    }

    public void uploadPost(Uri imageUri, String usuarioId, String descripcion, UpdateCallback callback) {
        Log.d("PostRepositorio", "Intentando subir publicación a Supabase para usuarioId: " + usuarioId + " con Uri: " + imageUri.toString());
        executorService.execute(() -> {
            try {
                File file = new File(context.getCacheDir(), "post_" + usuarioId + ".jpg");
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    Log.e("PostRepositorio", "No se pudo abrir el InputStream para Uri: " + imageUri);
                    if (callback != null) callback.onError("No se pudo abrir el InputStream");
                    return;
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                Log.d("PostRepositorio", "Creando archivo temporal en: " + file.getAbsolutePath());

                String bucketName = "post-perfil";
                String uniqueFileName = "post_" + UUID.randomUUID().toString() + ".jpg";
                String filePath = "public/" + usuarioId + "/" + uniqueFileName;
                String photoUrl = SupabaseClient.uploadFile(bucketName, filePath, file);

                Log.d("PostRepositorio", "URL generada por Supabase para publicación: " + photoUrl);

                Post post = new Post(usuarioId, photoUrl, descripcion, System.currentTimeMillis(), uniqueFileName);
                postDAO.insert(post);
                Log.d("PostRepositorio", "Publicación guardada en Room con URL: " + photoUrl + ", Descripción: " + descripcion + ", fileName: " + uniqueFileName);

                Usuario usuario = usuarioDAO.getUsuarioSync(usuarioId);
                if (usuario != null) {
                    usuario.publicaciones += 1;
                    usuarioDAO.update(usuario);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("usuarios").document(usuarioId)
                            .update("publicaciones", usuario.publicaciones)
                            .addOnSuccessListener(aVoid -> Log.d("PostRepositorio", "Publicaciones actualizadas en Firestore: " + usuario.publicaciones))
                            .addOnFailureListener(e -> Log.e("PostRepositorio", "Error al actualizar publicaciones en Firestore: " + e.getMessage()));
                }

                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onUpdateComplete();
                    });
                }
            } catch (IOException e) {
                Log.e("PostRepositorio", "Error al subir publicación: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }
        });
    }

    public LiveData<List<Post>> getPostsByUser(String usuarioId) {
        return postDAO.getPostsByUser(usuarioId);
    }

    public void deletePost(int postId, DeleteCallback callback) {
        executorService.execute(() -> {
            try {
                // Obtener el Post para obtener el fileName
                Post post = postDAO.getPostById(postId);
                if (post != null && post.fileName != null) {
                    Log.d("PostRepositorio", "Intentando eliminar archivo de Supabase con fileName: " + post.fileName);
                    String bucketName = "post-perfil";
                    String filePath = "public/" + post.usuarioPostId + "/" + post.fileName;
                    try {
                        SupabaseClient.deleteFile(bucketName, filePath); // Usamos el método deleteFile que crearemos
                        Log.d("PostRepositorio", "Archivo eliminado de Supabase para postId: " + postId);
                    } catch (IOException e) {
                        Log.e("PostRepositorio", "Error al eliminar archivo de Supabase: " + e.getMessage());
                    }
                } else {
                    Log.w("PostRepositorio", "No se encontró el Post o fileName para postId: " + postId);
                }

                // Eliminar de Room
                postDAO.delete(postId);
                Log.d("PostRepositorio", "Publicación eliminada de Room con ID: " + postId);

                // Actualizar conteo de publicaciones en Room y Firestore
                if (post != null) {
                    Usuario usuario = usuarioDAO.getUsuarioSync(post.usuarioPostId);
                    if (usuario != null && usuario.publicaciones > 0) {
                        usuario.publicaciones -= 1;
                        usuarioDAO.update(usuario);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("usuarios").document(post.usuarioPostId)
                                .update("publicaciones", usuario.publicaciones)
                                .addOnSuccessListener(aVoid -> Log.d("PostRepositorio", "Publicaciones actualizadas en Firestore: " + usuario.publicaciones))
                                .addOnFailureListener(e -> Log.e("PostRepositorio", "Error al actualizar publicaciones en Firestore: " + e.getMessage()));
                    }
                }

                // Notificar en el hilo principal
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onDeleteComplete(true);
                    });
                }
            } catch (Exception e) {
                Log.e("PostRepositorio", "Error al eliminar publicación: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onDeleteComplete(false);
                    });
                }
            }
        });
    }

    public void toggleLike(String usuarioId, int postId, boolean isLiked) {
        executorService.execute(() -> {
            if (isLiked) {
                likeDAO.insert(new Like(usuarioId, postId));
            } else {
                likeDAO.delete(usuarioId, postId);
            }
            Log.d("PostRepositorio", "Estado de Me gusta actualizado: usuarioId=" + usuarioId + ", postId=" + postId + ", liked=" + isLiked);
        });
    }

    public void addComment(String usuarioId, int postId, String comment) {
        executorService.execute(() -> {
            // Obtener el nombre del usuario desde UsuarioDAO usando usuarioId
            Usuario usuario = usuarioDAO.getUsuarioSync(usuarioId); // Me aseguro de que UsuarioDAO tenga getUsuarioSync
            String nombre = (usuario != null) ? usuario.nombre : "Usuario Desconocido";
            commentDAO.insert(new Comment(postId, usuarioId, nombre, comment, System.currentTimeMillis()));
            Log.d("PostRepositorio", "Comentario añadido: postId=" + postId + ", usuarioId=" + usuarioId + ", nombre=" + nombre + ", comentario=" + comment);
        });
    }

    public LiveData<List<Comment>> getComments(int postId) {
        return commentDAO.getCommentsByPost(postId); // Uso LiveData desde CommentDAO
    }

    public LiveData<Boolean> getLikeStatus(String usuarioId, int postId) {
        return likeDAO.existsLiveData(usuarioId, postId);
    }

    public LiveData<Integer> getLikeCount(int postId) {
        return likeDAO.getLikeCount(postId);
    }
}