// Author: Álvaro Pereira
// Date: 24-02-2025 (Updated: 06-03-2025)

package com.example.chinagram.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UsuarioDAO { // Anotación que marca esta interfaz como un DAO para Room

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Inserta un nuevo Usuario en la tabla "user_table". Si ya existe un usuario con el mismo usuarioId, lo reemplaza
    void insert(Usuario usuario);

    @Update
    void update(Usuario usuario); // Actualiza un usuario existente en la base de datos

    @Query("SELECT * FROM user_table WHERE usuarioId = :usuarioId") // Consulta que obtiene un usuario por su ID
    LiveData<Usuario> getUsuario(String usuarioId);

    @Query("SELECT * FROM user_table WHERE usuarioId = :usuarioId") // Versión síncrona de la consulta anterior
    Usuario getUsuarioSync(String usuarioId);

    @Query("SELECT nombre FROM user_table WHERE usuarioId = :usuarioId")
    LiveData<String> getUsuarioName(String usuarioId);

    @Query("DELETE FROM user_table WHERE usuarioId = :usuarioId")
    void deleteById(String usuarioId);

    // Nuevo método para actualizar solo el campo seguidores
    @Query("UPDATE user_table SET seguidores = :seguidores WHERE usuarioId = :usuarioId")
    void updateSeguidores(String usuarioId, int seguidores);
}