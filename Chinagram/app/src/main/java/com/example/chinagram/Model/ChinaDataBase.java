// Author: Álvaro Pereira
// Date: 24-02-2025
package com.example.chinagram.Model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;


@Database(entities = {Usuario.class, Post.class, Like.class, Comment.class}, version = 1, exportSchema = false) // Anotación que define la base de datos con Room
@TypeConverters({ListConverter.class})
public abstract class ChinaDataBase extends RoomDatabase {

    // Métodos abstractos que devuelven los DAOs. Estos DAOs permiten interactuar con las tablas de Usuario y Post
    public abstract UsuarioDAO usuarioDAO();
    public abstract PostDAO postDAO();
    public abstract LikeDAO likeDAO();
    public abstract CommentDAO commentDAO();

    // Instancia única y volátil de la base de datos. Volatile asegura que los cambios sean visibles entre hilos
    private static volatile ChinaDataBase INSTANCE;

    // Método estático para obtener la instancia de la base de datos
    public static ChinaDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChinaDataBase.class) { // Sincronización para evitar que múltiples hilos creen instancias simultáneamente
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder( // Construyo la base de datos usando Room
                                    context.getApplicationContext(), // Uso el contex de la app para persistencia
                                    ChinaDataBase.class, // Clase de la base de datos
                                    "chinagram_database" // Nombre del archivo de la base de datos
                            )
                            .fallbackToDestructiveMigration()
                            .build(); // Creo la instancia
                }
            }
        }
        return INSTANCE; // Devuelvo la instance
    }
}