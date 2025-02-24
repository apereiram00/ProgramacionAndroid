// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "post_table") // Anotación que define esta clase como una entidad de Room
public class Post {

    @PrimaryKey(autoGenerate = true) // Clave primaria con generación automática de IDs. Cada nuevo post tendrá un ID único incrementado automáticamente
    public int postId;
    public String usuarioId; // ID del usuario que crea el post, sirve como relación con la tabla de usuarios
    public String imagenUrl; // URL de la imagen del post
    public String descripcion; // Descripción del post
    public long fecha; // Fecha temporal de creación del post

    public Post(String usuarioId, String imagenUrl, String descripcion, long fecha) { // Constructor
        this.usuarioId = usuarioId;
        this.imagenUrl = imagenUrl;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }
}