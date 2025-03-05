// com.example.chinagram.Model.CommentDAO.java
package com.example.chinagram.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDAO {
    @Insert
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY fecha DESC") // Usamos postId y fecha
    LiveData<List<Comment>> getCommentsByPost(int postId);
}