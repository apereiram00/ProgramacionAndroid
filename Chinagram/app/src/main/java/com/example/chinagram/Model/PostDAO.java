// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDAO { // Anotación que marca esta interfaz como un DAO para Room

    @Insert
    void insert(Post post); // Inserta un nuevo Post en la tabla "post_table"

    @Query("SELECT * FROM post_table WHERE usuarioId = :usuarioId ORDER BY fecha DESC") // Consulta que obtiene todos los posts de un usuario específico
    LiveData<List<Post>> getPostsByUser(String usuarioId);
}