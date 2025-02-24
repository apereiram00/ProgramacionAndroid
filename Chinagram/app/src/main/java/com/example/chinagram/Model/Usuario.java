// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table") // Anotación que define esta clase como una entidad de Room
public class Usuario {

    @PrimaryKey
    @NonNull
    public String usuarioId; // Clave primaria, no se genera automáticamente ya que es un ID único del usuario.  @NonNull asegura que no pueda ser null
    public String nombre; // Nombre del usuario
    public String biografia; // Biografía o descripción del perfil
    public String fotoPerfilUrl; // URL de la foto de perfil
    public int seguidores; // Cantidad de seguidores del usuario
    public int siguiendo; // Cantidad de usuarios que sigue este usuario

    public Usuario(@NonNull String usuarioId, String nombre, String biografia, String fotoPerfilUrl, int seguidores, int siguiendo) { // Constructor
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.biografia = biografia;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.seguidores = seguidores;
        this.siguiendo = siguiendo;
    }
}