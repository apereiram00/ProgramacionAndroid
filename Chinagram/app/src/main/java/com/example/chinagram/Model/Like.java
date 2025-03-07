// com.example.chinagram.Model.Like.java
package com.example.chinagram.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "likes", primaryKeys = {"usuarioLikeId", "postLikeId"})
public class Like {
    @NonNull
    public String usuarioLikeId; // Añadimos @NonNull para indicar que no puede ser nulo
    public int postLikeId;

    public Like(String usuarioLikeId, int postLikeId) { // Actualizo el constructor para reflejar @NonNull
        this.usuarioLikeId = usuarioLikeId;
        this.postLikeId = postLikeId;
    }
}