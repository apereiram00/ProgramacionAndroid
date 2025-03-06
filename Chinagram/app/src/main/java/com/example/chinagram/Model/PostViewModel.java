// com.example.chinagram.Model.PostViewModel.java
package com.example.chinagram.Model;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PostViewModel extends AndroidViewModel {
    private final PostRepositorio repositorio;
    private final MutableLiveData<Boolean> postUploadCompleted = new MutableLiveData<>(false);
    private final UsuarioRepositorio usuarioRepositorio;

    public PostViewModel(@NonNull Application application) {
        super(application);
        repositorio = new PostRepositorio(application);
        usuarioRepositorio = new UsuarioRepositorio(application);
    }

    public void uploadPost(Uri imageUri, String usuarioId, String descripcion) {
        repositorio.uploadPost(imageUri, usuarioId, descripcion, new PostRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                postUploadCompleted.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e("PostViewModel", "Error al subir publicación: " + error);
            }
        });
    }

    public LiveData<List<Post>> getPostsByUser(String usuarioId) {
        return repositorio.getPostsByUser(usuarioId);
    }

    public LiveData<Boolean> getPostUploadCompleted() {
        return postUploadCompleted;
    }

    public void deletePost(int postId, DeleteCallback callback) {
        repositorio.deletePost(postId, new PostRepositorio.DeleteCallback() {
            @Override
            public void onDeleteComplete(boolean success) {
                callback.onDeleteComplete(success);
            }
        });
    }

    public void toggleLike(String usuarioId, int postId, boolean isLiked) {
        repositorio.toggleLike(usuarioId, postId, isLiked);
    }

    public void addComment(String usuarioId, int postId, String comment) {
        repositorio.addComment(usuarioId, postId, comment);
    }

    public LiveData<List<Comment>> getComments(int postId) {
        return repositorio.getComments(postId);
    }

    public LiveData<Boolean> getLikeStatus(String usuarioId, int postId) {
        return repositorio.getLikeStatus(usuarioId, postId);
    }

    public interface DeleteCallback {
        void onDeleteComplete(boolean success);
    }

    public LiveData<Integer> getLikeCount(int postId) {
        return repositorio.getLikeCount(postId);
    }

    public LiveData<String> getUsuarioName(String usuarioId) {
        return usuarioRepositorio.getUsuarioName(usuarioId);
    }

}