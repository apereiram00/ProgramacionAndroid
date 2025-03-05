// com.example.chinagram.Model.Comment.java
package com.example.chinagram.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId; // ID de la publicación, usamos postId
    public String usuarioId; // ID del usuario que comentó, usamos usuarioId
    public String nombre; // Nombre del usuario que comentó
    public String text;
    public long fecha; // Usamos fecha en lugar de timestamp para consistencia

    public Comment(int postId, String usuarioId, String nombre, String text, long fecha) {
        this.postId = postId;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.text = text;
        this.fecha = fecha;
    }
}