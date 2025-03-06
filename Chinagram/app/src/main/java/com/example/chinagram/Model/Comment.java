// com.example.chinagram.Model.Comment.java
package com.example.chinagram.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId; // ID de la publicación
    public String usuarioCommentId; // ID del usuario que comenta
    public String nombreUsuarioPost; // Nombre del usuario que comenta
    public String text;
    public long fecha; // Uso la fecha en lugar de timestamp para consistencia

    public Comment(int postId, String usuarioCommentId, String nombreUsuarioPost, String text, long fecha) {
        this.postId = postId;
        this.usuarioCommentId = usuarioCommentId;
        this.nombreUsuarioPost = nombreUsuarioPost;
        this.text = text;
        this.fecha = fecha;
    }
}