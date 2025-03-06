// com.example.chinagram.Model.PostDAO.java
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

    @Query("SELECT * FROM post_table WHERE usuarioPostId = :usuarioPostId ORDER BY fecha DESC") // Consulta que obtiene todos los posts de un usuario específico
    LiveData<List<Post>> getPostsByUser(String usuarioPostId);

    @Query("DELETE FROM post_table WHERE postId = :postId") // Consulta para borrar un post
    void delete(int postId);

    @Query("SELECT * FROM post_table WHERE postId = :postId") // Consulta para obtener un Post por su postId
    Post getPostById(int postId);
}