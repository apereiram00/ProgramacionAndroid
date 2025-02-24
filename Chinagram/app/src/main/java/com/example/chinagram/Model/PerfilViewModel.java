// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PerfilViewModel extends AndroidViewModel {
    private UsuarioRepositorio repositorio; // Repositorio para manejar la lógica de datos (base de datos y red)

    public PerfilViewModel(@NonNull Application application) { // Constructor
        super(application);
        repositorio = new UsuarioRepositorio(application);
    }

    public LiveData<Usuario> getUsuario(String usuarioId) { // Obtengo un usuario como LiveData para observarlo en la UI
        return repositorio.getUsuario(usuarioId);
    }

    public void uploadProfileImage(Uri imageUri, String userId) { // Subo una imagen de perfil a Imgur y actualizo - creo el usuario
        repositorio.uploadProfileImage(imageUri, userId);
    }

    public void ensureUserExists(String userId) { // Aseguro que exista un usuario en la base de datos; lo creo si es necesario
        repositorio.ensureUserExists(userId);
    }

    public void updateProfile(String userId, String nombre, String biografia) { // Actualizo el nombre y la biografía de un usuario
        repositorio.updateProfile(userId, nombre, biografia);
    }

    public LiveData<List<Post>> getPostsByUser(String usuarioId) { // Obtengo los posts de un usuario como LiveData para observarlos en la UI
        return repositorio.getPostsByUser(usuarioId);
    }
}