// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioRepositorio {
    // DAOs para interactuar con la base de datos local
    private UsuarioDAO usuarioDAO;
    private PostDAO postDAO;
    private ImgurClient imgurClient; // Cliente para subir imágenes a Imgur
    private ExecutorService executorService; // Pool de hilos para operaciones asíncronas en segundo plano
    private Context context; // Context de la app, usado para ContentResolver y base de datos

    public UsuarioRepositorio(Context context) { // Constructor
        ChinaDataBase database = ChinaDataBase.getDatabase(context);
        usuarioDAO = database.usuarioDAO();
        postDAO = database.postDAO();
        imgurClient = new ImgurClient();
        executorService = Executors.newFixedThreadPool(4); // 4 hilos para operaciones concurrentes
        this.context = context.getApplicationContext();    // Uso el contexto global
    }

    public LiveData<Usuario> getUsuario(String usuarioId) { // Obtengo un usuario como LiveData para observación en la UI
        return usuarioDAO.getUsuario(usuarioId);
    }

    public void uploadProfileImage(Uri imageUri, String userId) { // Subo una imagen de perfil a Imgur y actualizo o creo el usuario
        Log.d("UsuarioRepositorio", "Iniciando subida para userId: " + userId + " con Uri: " + imageUri.toString()); // Log de depuración del proceso
        imgurClient.uploadImage(imageUri, context.getContentResolver(), new ImgurClient.ImgurCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d("UsuarioRepositorio", "URL recibida: " + imageUrl); // Log de depuración del proceso
                executorService.execute(() -> { // Verifico si el usuario existe en la base de datos
                    Usuario usuario = usuarioDAO.getUsuarioSync(userId);
                    if (usuario != null) { // Actualizo la URL de la foto de perfil
                        usuario.fotoPerfilUrl = imageUrl;
                        usuarioDAO.update(usuario);
                    } else { // Si no existe, creo un usuario nuevo con datos predeterminados
                        Log.w("UsuarioRepositorio", "Usuario no encontrado, creando uno nuevo para userId: " + userId); // Log de depuración del proceso
                        Usuario nuevoUsuario = new Usuario(userId, "Usuario", getRandomBio(), imageUrl, getRandomSeguidores(), 0);
                        usuarioDAO.insert(nuevoUsuario);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UsuarioRepositorio", "Error: " + errorMessage);
            }
        });
    }

    public void ensureUserExists(String userId) { // Aseguro que exista un usuario en la base de datos, creo uno si no está
        executorService.execute(() -> {
            Usuario usuario = usuarioDAO.getUsuarioSync(userId);
            if (usuario == null) {
                Log.d("UsuarioRepositorio", "Creando usuario inicial para userId: " + userId); // Log de depuración del proceso
                String defaultPhoto = getRandomDefaultPhoto();
                Usuario nuevoUsuario = new Usuario(userId, "Usuario", getRandomBio(), defaultPhoto, getRandomSeguidores(), 0);
                usuarioDAO.insert(nuevoUsuario);
            }
        });
    }

    public void updateProfile(String userId, String nombre, String biografia) { // Actualizo el nombre y biografía de un usuario existente
        executorService.execute(() -> {
            Usuario usuario = usuarioDAO.getUsuarioSync(userId);
            if (usuario != null) {
                usuario.nombre = nombre;
                usuario.biografia = biografia;
                usuarioDAO.update(usuario);
                Log.d("UsuarioRepositorio", "Perfil actualizado: " + nombre + ", " + biografia); // Log de depuración del proceso
            } else {
                Log.e("UsuarioRepositorio", "No se pudo actualizar, usuario no encontrado para userId: " + userId); // Log de depuración del proceso
            }
        });
    }

    public LiveData<List<Post>> getPostsByUser(String usuarioId) { // Obtengo los posts de un usuario como LiveData
        return postDAO.getPostsByUser(usuarioId);
    }

    public void insertPost(Post post) { // Inserta un nuevo post en la base de datos (24-02-2025 || No está implementado aun)
        executorService.execute(() -> postDAO.insert(post));
    }

    private String getRandomDefaultPhoto() { // Devuelvo una foto de perfil predeterminada aleatoria
        String[] photos = {
                "drawable://xi_jinping",
                "drawable://panda",
                "drawable://china_flag"
        };
        Random random = new Random();
        return photos[random.nextInt(photos.length)];
    }

    private String getRandomBio() { // Genero una biografía aleatoria de una lista predefinida
        String[] bios = {
                "Amante de la cultura china y devoto de Xi.",
                "Trabajando día a día por una nación fuerte.",
                "China primero, lo demás después."
        };
        Random random = new Random();
        return bios[random.nextInt(bios.length)];
    }

    private int getRandomSeguidores() { // Genero un número aleatorio de seguidores entre 50K y 1M
        Random random = new Random();
        return 50000 + random.nextInt(950001); // Entre 50K y 1M
    }
}