package com.example.chinagram.Model;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PerfilViewModel extends AndroidViewModel {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PostRepositorio postRepositorio;
    private final MutableLiveData<Boolean> updatesCompleted = new MutableLiveData<>(false);

    public PerfilViewModel(@NonNull Application application) {
        super(application);
        usuarioRepositorio = new UsuarioRepositorio(application);
        postRepositorio = new PostRepositorio(application);
    }

    public LiveData<Usuario> getUsuario(String usuarioId) {
        return usuarioRepositorio.getUsuario(usuarioId);
    }

    public void uploadProfileImage(Uri imageUri, String userId) {
        usuarioRepositorio.uploadProfileImage(imageUri, userId, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                updatesCompleted.postValue(true); // Usar postValue() en lugar de setValue()
            }

            @Override
            public void onError(String error) {
                Log.e("PerfilViewModel", "Error al subir imagen: " + error);
            }
        });
    }

    public void ensureUserExists(String userId) {
        usuarioRepositorio.ensureUserExists(userId);
    }

    public void updateProfile(String userId, String nombre, String biografia) {
        usuarioRepositorio.updateProfile(userId, nombre, biografia, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                updatesCompleted.postValue(true); // Usar postValue() en lugar de setValue()
            }

            @Override
            public void onError(String error) {
                Log.e("PerfilViewModel", "Error al actualizar perfil: " + error);
            }
        });
    }

    public LiveData<List<Post>> getPostsByUser(String userId) {
        return postRepositorio.getPostsByUser(userId);
    }

    public LiveData<Boolean> getUpdatesCompleted() {
        return updatesCompleted;
    }

    public void followUser(String currentUserId, String targetUserId, UsuarioRepositorio.UpdateCallback callback) {
        usuarioRepositorio.followUser(currentUserId, targetUserId, callback);
    }

    public void unfollowUser(String currentUserId, String targetUserId, UsuarioRepositorio.UpdateCallback callback) {
        usuarioRepositorio.unfollowUser(currentUserId, targetUserId, callback);
    }

    public void isFollowing(String currentUserId, String targetUserId, final UsuarioRepositorio.IsFollowingCallback callback) {
        usuarioRepositorio.isFollowing(currentUserId, targetUserId, callback);
    }

    // Método para buscar usuarios por nombre
    public LiveData<List<Usuario>> searchUsersByName(String query) {
        return usuarioRepositorio.searchUsersByName(query);
    }
}