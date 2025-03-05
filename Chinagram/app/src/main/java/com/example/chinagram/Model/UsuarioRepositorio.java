package com.example.chinagram.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UsuarioRepositorio {
    private final UsuarioDAO usuarioDAO;
    private final ExecutorService executorService;
    private final Context context;

    public interface SearchCallback {
        void onSearchComplete(List<Usuario> users);
    }

    public interface UpdateCallback {
        void onUpdateComplete();
        void onError(String error);
    }

    public interface IsFollowingCallback {
        void onResult(boolean isFollowing);
    }

    public UsuarioRepositorio(Context context) {
        ChinaDataBase database = ChinaDataBase.getDatabase(context);
        usuarioDAO = database.usuarioDAO();
        executorService = Executors.newFixedThreadPool(4);
        this.context = context.getApplicationContext();
    }

    public LiveData<Usuario> getUsuario(String usuarioId) {
        return usuarioDAO.getUsuario(usuarioId);
    }

    public void ensureUserExists(String userId) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDAO.getUsuarioSync(userId);
            if (usuario == null) {
                Log.w("UsuarioRepositorio", "Usuario no encontrado en Room, verificando en Firestore para userId: " + userId);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            executorService.execute(() -> { // Mover la inserción a executorService
                                if (documentSnapshot.exists()) {
                                    // Si existe en Firestore, usa los datos de ahí
                                    String nombre = documentSnapshot.getString("nombre");
                                    String biografia = documentSnapshot.getString("biografia");
                                    String fotoPerfilUrl = documentSnapshot.getString("fotoPerfilUrl");
                                    int seguidores = documentSnapshot.getLong("seguidores").intValue();
                                    int siguiendo = documentSnapshot.getLong("siguiendo").intValue();
                                    Usuario nuevoUsuario = new Usuario(userId, nombre,
                                            biografia != null ? biografia : getRandomBio(),
                                            fotoPerfilUrl != null ? fotoPerfilUrl : "drawable://panda",
                                            seguidores, siguiendo);
                                    usuarioDAO.insert(nuevoUsuario);
                                    Log.d("UsuarioRepositorio", "Usuario sincronizado desde Firestore en Room con nombre: " + nombre);
                                } else {
                                    // Si no existe en Firestore, crea un usuario predeterminado con el nombre basado en el correo
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String email = firebaseUser.getEmail();
                                        String nombre = extraerNombreUsuario(email); // "grok" de "grok@gmail.com"
                                        Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), "drawable://panda", 0, 0);
                                        usuarioDAO.insert(nuevoUsuario);
                                        // Guardar en Firestore para consistencia
                                        guardarUsuarioEnFirestore(userId, nombre, getRandomBio(), "drawable://panda");
                                        Log.d("UsuarioRepositorio", "Usuario creado en Room y Firestore con nombre desde correo: " + nombre);
                                    } else {
                                        // Fallback si no hay usuario autenticado
                                        Usuario nuevoUsuario = new Usuario(userId, "Usuario", getRandomBio(), "drawable://panda", 0, 0);
                                        usuarioDAO.insert(nuevoUsuario);
                                        Log.d("UsuarioRepositorio", "Usuario creado en Room con nombre predeterminado: Usuario");
                                    }
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            executorService.execute(() -> { // Mover el fallback a executorService
                                Log.e("UsuarioRepositorio", "Error al verificar usuario en Firestore: " + e.getMessage());
                                // Fallback: crear usuario predeterminado con el nombre basado en el correo si está autenticado
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String nombre = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                                Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), "drawable://panda", 0, 0);
                                usuarioDAO.insert(nuevoUsuario);
                                if (firebaseUser != null) {
                                    guardarUsuarioEnFirestore(userId, nombre, getRandomBio(), "drawable://panda");
                                }
                                Log.d("UsuarioRepositorio", "Usuario creado en Room con fallback: " + nombre);
                            });
                        });
            }
        });
    }

    public void uploadProfileImage(Uri imageUri, String userId, UpdateCallback callback) {
        Log.d("UsuarioRepositorio", "Intentando subir imagen a Supabase para userId: " + userId + " con Uri: " + imageUri.toString());
        executorService.execute(() -> {
            try {
                File file = new File(context.getCacheDir(), "profile_" + userId + ".jpg");
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    Log.e("UsuarioRepositorio", "No se pudo abrir el InputStream para Uri: " + imageUri);
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

                Log.d("UsuarioRepositorio", "Creando archivo temporal en: " + file.getAbsolutePath());

                String bucketName = "imagenes-perfil";
                String uniqueFileName = "profile_" + UUID.randomUUID().toString() + ".jpg";
                String filePath = "public/usr7w8.0/" + userId + "/" + uniqueFileName;
                String photoUrl = SupabaseClient.uploadFile(bucketName, filePath, file);

                Log.d("UsuarioRepositorio", "URL generada por Supabase: " + photoUrl);

                Usuario usuario = usuarioDAO.getUsuarioSync(userId);
                if (usuario != null) {
                    usuario.fotoPerfilUrl = photoUrl;
                    usuarioDAO.update(usuario);
                    Log.d("UsuarioRepositorio", "Usuario actualizado en Room con fotoPerfilUrl: " + usuario.fotoPerfilUrl);
                } else {
                    Log.w("UsuarioRepositorio", "Usuario no encontrado, creando uno nuevo para userId: " + userId);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String nombre = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                    Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), photoUrl, 0, 0);
                    usuarioDAO.insert(nuevoUsuario); // Esto ya está en executorService, así que no hay problema
                    Log.d("UsuarioRepositorio", "Usuario creado en Room con fotoPerfilUrl: " + nuevoUsuario.fotoPerfilUrl);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onUpdateComplete();
                    });
                }
            } catch (IOException e) {
                Log.e("UsuarioRepositorio", "Error al subir imagen: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }
        });
    }

    public void updateProfile(String userId, String nombre, String biografia, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                Usuario usuario = usuarioDAO.getUsuarioSync(userId);
                if (usuario != null) {
                    usuario.nombre = nombre;
                    usuario.biografia = biografia;
                    usuarioDAO.update(usuario);
                    Log.d("UsuarioRepositorio", "Perfil actualizado en Room: " + nombre + ", " + biografia);
                } else {
                    Log.w("UsuarioRepositorio", "Usuario no encontrado para actualizar perfil: " + userId);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String nombreDefault = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                    Usuario nuevoUsuario = new Usuario(userId, nombreDefault, biografia, "drawable://panda", 0, 0);
                    usuarioDAO.insert(nuevoUsuario);
                    Log.d("UsuarioRepositorio", "Usuario creado en Room con perfil: " + nombreDefault + ", " + biografia);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onUpdateComplete();
                    });
                }
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al actualizar perfil: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }
        });
    }

    public void followUser(String currentUserId, String targetUserId, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // Verificar si ya sigues al usuario usando Room (simplificado, sin tabla Following por ahora)
                Usuario currentUser = usuarioDAO.getUsuarioSync(currentUserId);
                Usuario targetUser = usuarioDAO.getUsuarioSync(targetUserId);
                if (currentUser != null && targetUser != null) {
                    // Suponemos que solo incrementamos si no estás siguiendo (lógica simplificada)
                    currentUser.siguiendo += 1;
                    targetUser.seguidores += 1;
                    usuarioDAO.update(currentUser);
                    usuarioDAO.update(targetUser);
                    Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " ahora sigue a " + targetUserId);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onUpdateComplete();
                    });
                }
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al seguir usuario: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // Verificar si sigues al usuario usando Room (simplificado, sin tabla Following por ahora)
                Usuario currentUser = usuarioDAO.getUsuarioSync(currentUserId);
                Usuario targetUser = usuarioDAO.getUsuarioSync(targetUserId);
                if (currentUser != null && targetUser != null) {
                    if (currentUser.siguiendo > 0) currentUser.siguiendo -= 1;
                    if (targetUser.seguidores > 0) targetUser.seguidores -= 1;
                    usuarioDAO.update(currentUser);
                    usuarioDAO.update(targetUser);
                    Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " dejó de seguir a " + targetUserId);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onUpdateComplete();
                    });
                }
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al dejar de seguir usuario: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError(e.getMessage());
                    });
                }
            }
        });
    }

    public void isFollowing(String currentUserId, String targetUserId, final IsFollowingCallback callback) {
        executorService.execute(() -> {
            try {
                // Lógica simplificada para verificar si sigues al usuario usando los contadores
                Usuario currentUser = usuarioDAO.getUsuarioSync(currentUserId);
                if (currentUser != null) {
                    int followingCount = currentUser.siguiendo;
                    int followersCount = usuarioDAO.getSeguidoresCount(targetUserId);
                    boolean isFollowing = followingCount > 0 && followingCount == followersCount;
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onResult(isFollowing);
                    });
                } else {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onResult(false);
                    });
                }
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al verificar si sigue: " + e.getMessage());
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onResult(false);
                });
            }
        });
    }

    public LiveData<List<Usuario>> searchUsersByName(String query) {
        MutableLiveData<List<Usuario>> usuariosLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d("UsuarioRepositorio", "Buscando usuarios con query: " + query);
            db.collection("usuarios")
                    .whereGreaterThanOrEqualTo("nombre", query)
                    .whereLessThanOrEqualTo("nombre", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Usuario> usuarios = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot) {
                            Usuario usuario = document.toObject(Usuario.class);
                            if (usuario != null) {
                                Log.d("UsuarioRepositorio", "Usuario encontrado: " + usuario.nombre + " (ID: " + usuario.usuarioId + ")");
                                usuarios.add(usuario);
                            }
                        }
                        Log.d("UsuarioRepositorio", "Total usuarios encontrados: " + usuarios.size());
                        usuariosLiveData.postValue(usuarios);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UsuarioRepositorio", "Error al buscar usuarios: " + e.getMessage());
                        usuariosLiveData.postValue(new ArrayList<>());
                    });
        });
        return usuariosLiveData;
    }

    public LiveData<String> getUsuarioName(String usuarioId) {
        // Devuelve el nombre del usuario como LiveData usando Room
        return usuarioDAO.getUsuarioName(usuarioId);
    }

    private String extraerNombreUsuario(String email) {
        // Extrae la parte antes del "@" del correo (por ejemplo, "grok" de "grok@gmail.com")
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            return email.substring(0, atIndex);
        }
        return email; // Si no hay "@", devolvemos el email completo como fallback
    }

    private void guardarUsuarioEnFirestore(String userId, String nombre, String biografia, String photoUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(userId).set(
                        new Usuario(userId, nombre, biografia, photoUrl, 0, 0))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario guardado en Firestore con nombre: " + nombre))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al guardar usuario: " + e.getMessage()));
    }

    private String getRandomBio() {
        String[] bios = {"Amante de la vida", "Explorador del mundo", "Soñador sin límites"};
        return bios[(int) (Math.random() * bios.length)];
    }
}