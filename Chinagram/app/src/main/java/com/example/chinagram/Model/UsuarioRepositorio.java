package com.example.chinagram.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.chinagram.utils.DrawableUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioRepositorio {
    private final UsuarioDAO usuarioDAO;
    private final ExecutorService executorService;
    private final Context context;
    private final FirebaseFirestore db;

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
        this.db = FirebaseFirestore.getInstance();
    }

    public LiveData<Usuario> getUsuario(String usuarioId) {
        return usuarioDAO.getUsuario(usuarioId);
    }

    public void ensureUserExists(String userId) {
        executorService.execute(() -> {
            Usuario usuario = usuarioDAO.getUsuarioSync(userId);
            if (usuario == null) {
                Log.w("UsuarioRepositorio", "Usuario no encontrado en Room, verificando en Firestore para userId: " + userId);
                db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            executorService.execute(() -> {
                                if (documentSnapshot.exists()) {
                                    String nombre = documentSnapshot.getString("nombre");
                                    String biografia = documentSnapshot.getString("biografia");
                                    String fotoPerfilUrl = documentSnapshot.getString("fotoPerfilUrl");
                                    Long seguidoresLong = documentSnapshot.getLong("seguidores");
                                    Long siguiendoLong = documentSnapshot.getLong("siguiendoCount");
                                    int seguidores = seguidoresLong != null ? seguidoresLong.intValue() : 0;
                                    int siguiendo = siguiendoLong != null ? siguiendoLong.intValue() : 0;
                                    int publicaciones = documentSnapshot.getLong("publicaciones") != null ? documentSnapshot.getLong("publicaciones").intValue() : 0;
                                    List<String> siguiendoList = (List<String>) documentSnapshot.get("siguiendo");
                                    List<String> seguidoresList = (List<String>) documentSnapshot.get("seguidoresList");
                                    if (siguiendoList == null) {
                                        siguiendoList = new ArrayList<>();
                                    }
                                    if (seguidoresList == null) {
                                        seguidoresList = new ArrayList<>();
                                    }
                                    Usuario nuevoUsuario = new Usuario(userId, nombre,
                                            biografia != null ? biografia : getRandomBio(),
                                            fotoPerfilUrl != null ? fotoPerfilUrl : DrawableUtils.obtenerUrlDrawableAleatoria(),
                                            seguidores, siguiendo, publicaciones);
                                    nuevoUsuario.siguiendoList = siguiendoList;
                                    nuevoUsuario.seguidoresList = seguidoresList;
                                    usuarioDAO.insert(nuevoUsuario);
                                    Log.d("UsuarioRepositorio", "Usuario sincronizado desde Firestore en Room con nombre: " + nombre);
                                } else {
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String email = firebaseUser.getEmail();
                                        String nombre = extraerNombreUsuario(email);
                                        String fotoUrl = DrawableUtils.obtenerUrlDrawableAleatoria();
                                        Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), fotoUrl, 0, 0, 0);
                                        usuarioDAO.insert(nuevoUsuario);
                                        guardarUsuarioEnFirestore(userId, nombre, getRandomBio(), fotoUrl);
                                        Log.d("UsuarioRepositorio", "Usuario creado en Room y Firestore con nombre: " + nombre);
                                    } else {
                                        String fotoUrl = DrawableUtils.obtenerUrlDrawableAleatoria();
                                        Usuario nuevoUsuario = new Usuario(userId, "Usuario", getRandomBio(), fotoUrl, 0, 0, 0);
                                        usuarioDAO.insert(nuevoUsuario);
                                        Log.d("UsuarioRepositorio", "Usuario creado en Room con nombre predeterminado: Usuario");
                                    }
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            executorService.execute(() -> {
                                Log.e("UsuarioRepositorio", "Error al verificar usuario en Firestore: " + e.getMessage());
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String nombre = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                                String fotoUrl = DrawableUtils.obtenerUrlDrawableAleatoria();
                                Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), fotoUrl, 0, 0, 0);
                                usuarioDAO.insert(nuevoUsuario);
                                if (firebaseUser != null) {
                                    guardarUsuarioEnFirestore(userId, nombre, getRandomBio(), fotoUrl);
                                }
                                Log.d("UsuarioRepositorio", "Usuario creado en Room con fallback: " + nombre);
                            });
                        });
            }
        });
    }

    public void ensureUserExistsAndSync(String userId, Usuario usuario, UpdateCallback callback) {
        executorService.execute(() -> {
            usuarioDAO.insert(usuario);
            Log.d("UsuarioRepositorio", "Usuario sincronizado en Room con nombre: " + usuario.nombre);
            guardarUsuarioEnFirestore(userId, usuario.nombre, usuario.biografia, usuario.fotoPerfilUrl);
            Log.d("UsuarioRepositorio", "Usuario sincronizado en Firestore con nombre: " + usuario.nombre);
            if (callback != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
            }
        });
    }

    public void deleteUser(String userId) {
        executorService.execute(() -> {
            usuarioDAO.deleteById(userId);
            Log.d("UsuarioRepositorio", "Usuario eliminado de Room con ID: " + userId);
        });
    }

    public void updateFollowers(String userId, int newFollowers, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                usuarioDAO.updateSeguidores(userId, newFollowers);
                Log.d("UsuarioRepositorio", "Seguidores actualizados en Room para userId: " + userId + ", nuevos seguidores: " + newFollowers);

                db.collection("usuarios").document(userId)
                        .update("seguidores", newFollowers)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("UsuarioRepositorio", "Seguidores actualizados en Firestore para userId: " + userId);
                            if (callback != null) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UsuarioRepositorio", "Error al actualizar seguidores en Firestore: " + e.getMessage());
                            if (callback != null) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                            }
                        });
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al actualizar seguidores en Room: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }
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

                    db.collection("usuarios").document(userId)
                            .update("fotoPerfilUrl", photoUrl)
                            .addOnSuccessListener(aVoid -> Log.d("UsuarioRepositorio", "Foto de perfil sincronizada con Firestore: " + photoUrl))
                            .addOnFailureListener(e -> Log.e("UsuarioRepositorio", "Error al sincronizar foto con Firestore: " + e.getMessage()));
                } else {
                    Log.w("UsuarioRepositorio", "Usuario no encontrado, creando uno nuevo para userId: " + userId);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String nombre = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                    String fotoUrl = DrawableUtils.obtenerUrlDrawableAleatoria();
                    Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), photoUrl, 0, 0, 0);
                    usuarioDAO.insert(nuevoUsuario);
                    guardarUsuarioEnFirestore(userId, nombre, getRandomBio(), photoUrl);
                    Log.d("UsuarioRepositorio", "Usuario creado en Room con fotoPerfilUrl: " + nuevoUsuario.fotoPerfilUrl);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                }
            } catch (IOException e) {
                Log.e("UsuarioRepositorio", "Error al subir imagen: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
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

                    db.collection("usuarios").document(userId)
                            .update("nombre", nombre, "biografia", biografia)
                            .addOnSuccessListener(aVoid -> Log.d("UsuarioRepositorio", "Perfil sincronizado con Firestore: " + nombre + ", " + biografia))
                            .addOnFailureListener(e -> Log.e("UsuarioRepositorio", "Error al sincronizar perfil con Firestore: " + e.getMessage()));
                } else {
                    Log.w("UsuarioRepositorio", "Usuario no encontrado para actualizar perfil: " + userId);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String nombreDefault = firebaseUser != null ? extraerNombreUsuario(firebaseUser.getEmail()) : "Usuario";
                    String fotoUrl = DrawableUtils.obtenerUrlDrawableAleatoria();
                    Usuario nuevoUsuario = new Usuario(userId, nombre, getRandomBio(), fotoUrl, 0, 0, 0);
                    usuarioDAO.insert(nuevoUsuario);
                    guardarUsuarioEnFirestore(userId, nombreDefault, biografia, fotoUrl);
                    Log.d("UsuarioRepositorio", "Usuario creado en Room con perfil: " + nombreDefault + ", " + biografia);
                }
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                }
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al actualizar perfil: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void followUser(String currentUserId, String targetUserId, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // Verificar si ya sigue al usuario en Firestore
                db.collection("usuarios").document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> siguiendo = (List<String>) documentSnapshot.get("siguiendo");
                                if (siguiendo != null && siguiendo.contains(targetUserId)) {
                                    Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " ya sigue a " + targetUserId);
                                    if (callback != null) {
                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                                    }
                                    return;
                                }

                                // Ejecutar operaciones de Room en un hilo secundario
                                executorService.execute(() -> {
                                    try {
                                        Usuario currentUser = usuarioDAO.getUsuarioSync(currentUserId);
                                        Usuario targetUser = usuarioDAO.getUsuarioSync(targetUserId);
                                        if (currentUser != null && targetUser != null) {
                                            if (currentUser.siguiendoList.contains(targetUserId)) {
                                                Log.d("UsuarioRepositorio", "Seguimiento ya existe en Room para " + currentUserId);
                                                if (callback != null) {
                                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                                                }
                                                return;
                                            }

                                            // Incremento único
                                            currentUser.siguiendo += 1;
                                            currentUser.siguiendoList.add(targetUserId);
                                            targetUser.seguidores += 1;
                                            targetUser.seguidoresList.add(currentUserId);
                                            usuarioDAO.update(currentUser);
                                            usuarioDAO.update(targetUser);
                                            Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " ahora sigue a " + targetUserId + " en Room");

                                            // Actualizar en Firestore
                                            Map<String, Object> updatesCurrent = new HashMap<>();
                                            updatesCurrent.put("siguiendo", FieldValue.arrayUnion(targetUserId));
                                            updatesCurrent.put("siguiendoCount", FieldValue.increment(1));
                                            db.collection("usuarios").document(currentUserId)
                                                    .update(updatesCurrent)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " ahora sigue a " + targetUserId + " en Firestore");
                                                        Map<String, Object> updatesTarget = new HashMap<>();
                                                        updatesTarget.put("seguidores", FieldValue.increment(1));
                                                        updatesTarget.put("seguidoresList", FieldValue.arrayUnion(currentUserId));
                                                        db.collection("usuarios").document(targetUserId)
                                                                .update(updatesTarget)
                                                                .addOnSuccessListener(aVoid2 -> {
                                                                    Log.d("UsuarioRepositorio", "Seguidores incrementados para " + targetUserId + " en Firestore");
                                                                    if (callback != null) {
                                                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e("UsuarioRepositorio", "Error al incrementar seguidores en Firestore: " + e.getMessage());
                                                                    // Revertir cambios en Room
                                                                    currentUser.siguiendo -= 1;
                                                                    currentUser.siguiendoList.remove(targetUserId);
                                                                    targetUser.seguidores -= 1;
                                                                    targetUser.seguidoresList.remove(currentUserId);
                                                                    usuarioDAO.update(currentUser);
                                                                    usuarioDAO.update(targetUser);
                                                                    if (callback != null) {
                                                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                                                                    }
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("UsuarioRepositorio", "Error al seguir usuario en Firestore: " + e.getMessage());
                                                        // Revertir cambios en Room
                                                        currentUser.siguiendo -= 1;
                                                        currentUser.siguiendoList.remove(targetUserId);
                                                        targetUser.seguidores -= 1;
                                                        targetUser.seguidoresList.remove(currentUserId);
                                                        usuarioDAO.update(currentUser);
                                                        usuarioDAO.update(targetUser);
                                                        if (callback != null) {
                                                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                                                        }
                                                    });
                                        } else {
                                            Log.e("UsuarioRepositorio", "Usuario no encontrado en Room: " + currentUserId + " o " + targetUserId);
                                            if (callback != null) {
                                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Usuario no encontrado en Room"));
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("UsuarioRepositorio", "Error al actualizar Room: " + e.getMessage());
                                        if (callback != null) {
                                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                                        }
                                    }
                                });
                            } else {
                                Log.e("UsuarioRepositorio", "Documento de usuario no encontrado para " + currentUserId);
                                if (callback != null) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Usuario no encontrado"));
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UsuarioRepositorio", "Error al verificar seguimiento en Firestore: " + e.getMessage());
                            if (callback != null) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                            }
                        });
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al seguir usuario: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId, UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                // Ejecutar operaciones de Room en un hilo secundario
                executorService.execute(() -> {
                    try {
                        Usuario currentUser = usuarioDAO.getUsuarioSync(currentUserId);
                        Usuario targetUser = usuarioDAO.getUsuarioSync(targetUserId);
                        if (currentUser != null && targetUser != null) {
                            if (!currentUser.siguiendoList.contains(targetUserId)) {
                                Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " no sigue a " + targetUserId);
                                if (callback != null) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                                }
                                return;
                            }

                            if (currentUser.siguiendo > 0) currentUser.siguiendo -= 1;
                            currentUser.siguiendoList.remove(targetUserId);
                            if (targetUser.seguidores > 0) targetUser.seguidores -= 1;
                            targetUser.seguidoresList.remove(currentUserId);
                            usuarioDAO.update(currentUser);
                            usuarioDAO.update(targetUser);
                            Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " dejó de seguir a " + targetUserId + " en Room");

                            // Actualizar en Firestore
                            Map<String, Object> updatesCurrent = new HashMap<>();
                            updatesCurrent.put("siguiendo", FieldValue.arrayRemove(targetUserId));
                            updatesCurrent.put("siguiendoCount", FieldValue.increment(-1));
                            db.collection("usuarios").document(currentUserId)
                                    .update(updatesCurrent)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("UsuarioRepositorio", "Usuario " + currentUserId + " dejó de seguir a " + targetUserId + " en Firestore");
                                        Map<String, Object> updatesTarget = new HashMap<>();
                                        updatesTarget.put("seguidores", FieldValue.increment(-1));
                                        updatesTarget.put("seguidoresList", FieldValue.arrayRemove(currentUserId));
                                        db.collection("usuarios").document(targetUserId)
                                                .update(updatesTarget)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Log.d("UsuarioRepositorio", "Seguidores decrementados para " + targetUserId + " en Firestore");
                                                    if (callback != null) {
                                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(callback::onUpdateComplete);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("UsuarioRepositorio", "Error al decrementar seguidores en Firestore: " + e.getMessage());
                                                    // Revertir cambios en Room
                                                    if (currentUser.siguiendo < currentUser.siguiendoList.size()) currentUser.siguiendo += 1;
                                                    currentUser.siguiendoList.add(targetUserId);
                                                    if (targetUser.seguidores < targetUser.seguidoresList.size()) targetUser.seguidores += 1;
                                                    targetUser.seguidoresList.add(currentUserId);
                                                    usuarioDAO.update(currentUser);
                                                    usuarioDAO.update(targetUser);
                                                    if (callback != null) {
                                                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UsuarioRepositorio", "Error al dejar de seguir usuario en Firestore: " + e.getMessage());
                                        // Revertir cambios en Room
                                        if (currentUser.siguiendo < currentUser.siguiendoList.size()) currentUser.siguiendo += 1;
                                        currentUser.siguiendoList.add(targetUserId);
                                        if (targetUser.seguidores < targetUser.seguidoresList.size()) targetUser.seguidores += 1;
                                        targetUser.seguidoresList.add(currentUserId);
                                        usuarioDAO.update(currentUser);
                                        usuarioDAO.update(targetUser);
                                        if (callback != null) {
                                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                                        }
                                    });
                        } else {
                            Log.e("UsuarioRepositorio", "Usuario no encontrado en Room: " + currentUserId + " o " + targetUserId);
                            if (callback != null) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Usuario no encontrado en Room"));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("UsuarioRepositorio", "Error al actualizar Room: " + e.getMessage());
                        if (callback != null) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al dejar de seguir usuario: " + e.getMessage());
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void isFollowing(String currentUserId, String targetUserId, IsFollowingCallback callback) {
        executorService.execute(() -> {
            try {
                db.collection("usuarios").document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> siguiendo = (List<String>) documentSnapshot.get("siguiendo");
                                boolean isFollowing = siguiendo != null && siguiendo.contains(targetUserId);
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(isFollowing));
                            } else {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(false));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UsuarioRepositorio", "Error al verificar seguimiento: " + e.getMessage());
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(false));
                        });
            } catch (Exception e) {
                Log.e("UsuarioRepositorio", "Error al verificar si sigue: " + e.getMessage());
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(false));
            }
        });
    }

    public LiveData<List<Usuario>> searchUsersByName(String query) {
        MutableLiveData<List<Usuario>> usuariosLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            if (query == null || query.trim().isEmpty()) {
                Log.d("UsuarioRepositorio", "Query vacía, devolviendo lista vacía");
                usuariosLiveData.postValue(new ArrayList<>());
                return;
            }

            String queryLowerCase = query.trim().toLowerCase();
            db.collection("usuarios")
                    .whereGreaterThanOrEqualTo("nombre", queryLowerCase)
                    .whereLessThanOrEqualTo("nombre", queryLowerCase + "\uf8ff")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Usuario> usuarios = new ArrayList<>();
                        Log.d("UsuarioRepositorio", "Documentos devueltos por Firestore: " + querySnapshot.size());
                        for (DocumentSnapshot document : querySnapshot) {
                            Log.d("UsuarioRepositorio", "Documento encontrado: " + document.getData());
                            String usuarioId = document.getString("usuarioId");
                            String nombre = document.getString("nombre");
                            String biografia = document.getString("biografia");
                            String fotoPerfilUrl = document.getString("fotoPerfilUrl");
                            Long seguidoresLong = document.getLong("seguidores");
                            Long siguiendoLong = document.getLong("siguiendoCount");
                            int seguidores = seguidoresLong != null ? seguidoresLong.intValue() : 0;
                            int siguiendo = siguiendoLong != null ? siguiendoLong.intValue() : 0;
                            int publicaciones = document.getLong("publicaciones") != null ? document.getLong("publicaciones").intValue() : 0;
                            List<String> siguiendoList = (List<String>) document.get("siguiendo");
                            List<String> seguidoresList = (List<String>) document.get("seguidoresList");
                            if (siguiendoList == null) {
                                siguiendoList = new ArrayList<>();
                            }
                            if (seguidoresList == null) {
                                seguidoresList = new ArrayList<>();
                            }
                            Usuario usuario = new Usuario(usuarioId, nombre, biografia, fotoPerfilUrl, seguidores, siguiendo, publicaciones);
                            usuario.siguiendoList = siguiendoList;
                            usuario.seguidoresList = seguidoresList;
                            usuarios.add(usuario);
                            Log.d("UsuarioRepositorio", "Usuario encontrado: " + usuario.nombre + " (ID: " + usuario.usuarioId + ")");
                        }
                        Log.d("UsuarioRepositorio", "Total usuarios encontrados: " + usuarios.size());
                        usuariosLiveData.postValue(usuarios);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UsuarioRepositorio", "Error al buscar usuarios: " + e.getMessage(), e);
                        if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.e("UsuarioRepositorio", "Permiso denegado para la consulta. Verifica las reglas de Firestore.");
                        }
                        usuariosLiveData.postValue(new ArrayList<>());
                    });
        });
        return usuariosLiveData;
    }

    public LiveData<String> getUsuarioName(String usuarioId) {
        return usuarioDAO.getUsuarioName(usuarioId);
    }

    private String extraerNombreUsuario(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            return email.substring(0, atIndex);
        }
        return email;
    }

    private void guardarUsuarioEnFirestore(String userId, String nombre, String biografia, String photoUrl) {
        String nombreLowerCase = nombre != null ? nombre.toLowerCase() : "usuario";
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("usuarioId", userId);
        usuarioData.put("nombre", nombreLowerCase);
        usuarioData.put("biografia", biografia);
        usuarioData.put("fotoPerfilUrl", photoUrl);
        usuarioData.put("seguidores", 0);
        usuarioData.put("seguidoresList", new ArrayList<String>()); // Nueva lista de seguidores
        usuarioData.put("siguiendo", new ArrayList<String>());
        usuarioData.put("siguiendoCount", 0);
        usuarioData.put("publicaciones", 0);

        db.collection("usuarios").document(userId).set(usuarioData)
                .addOnSuccessListener(aVoid -> Log.d("UsuarioRepositorio", "Usuario guardado en Firestore con nombre: " + nombreLowerCase))
                .addOnFailureListener(e -> Log.e("UsuarioRepositorio", "Error al guardar usuario: " + e.getMessage()));
    }

    private String getRandomBio() {
        String[] bios = {"Amante de la vida", "Explorador del mundo", "Soñador sin límites"};
        return bios[(int) (Math.random() * bios.length)];
    }
}