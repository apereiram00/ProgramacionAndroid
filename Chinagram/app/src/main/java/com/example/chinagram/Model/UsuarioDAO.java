// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UsuarioDAO { // Anotación que marca esta interfaz como un DAO para Room

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Inserta un nuevo Usuario en la tabla "user_table. Si ya existe un usuario con el mismo usuarioId, lo reemplaza
    void insert(Usuario usuario);

    @Query("SELECT * FROM user_table WHERE usuarioId = :usuarioId") // Consulta que obtiene un usuario por su ID
    LiveData<Usuario> getUsuario(String usuarioId);

    @Query("SELECT * FROM user_table WHERE usuarioId = :usuarioId") // Versión síncrona de la consulta anterior
    Usuario getUsuarioSync(String usuarioId);

    @Update
    void update(Usuario usuario); // Actualiza un usuario existente en la base de datos
}