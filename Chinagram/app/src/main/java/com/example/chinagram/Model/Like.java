// com.example.chinagram.Model.Like.java
package com.example.chinagram.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "likes", primaryKeys = {"usuarioId", "postId"}) // Usamos usuarioId y postId
public class Like {
    @NonNull
    public String usuarioId; // Añadimos @NonNull para indicar que no puede ser nulo
    public int postId;

    public Like(@NonNull String usuarioId, int postId) { // Actualizamos el constructor para reflejar @NonNull
        this.usuarioId = usuarioId;
        this.postId = postId;
    }
}