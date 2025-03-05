// com.example.chinagram.Model.LikeDAO.java
package com.example.chinagram.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface LikeDAO {
    @Insert
    void insert(Like like);

    @Query("DELETE FROM likes WHERE usuarioId = :usuarioId AND postId = :postId") // Usamos usuarioId y postId
    void delete(String usuarioId, int postId);

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE usuarioId = :usuarioId AND postId = :postId)") // Usamos usuarioId y postId
    LiveData<Boolean> existsLiveData(String usuarioId, int postId); // Nuevo método LiveData

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    LiveData<Integer> getLikeCount(int postId);// Nuevo método para contar los likes por postId
}