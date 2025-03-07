// com.example.chinagram.Model.Post.java
package com.example.chinagram.Model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "post_table")
public class Post implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int postId;
    public String usuarioPostId;
    public String imagenUrl;
    public String descripcion;
    public long fecha;
    public String fileName; // Nuevo campo para el nombre del archivo en Supabase

    public Post(String usuarioPostId, String imagenUrl, String descripcion, long fecha, String fileName) {
        this.usuarioPostId = usuarioPostId;
        this.imagenUrl = imagenUrl;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.fileName = fileName;
    }

    protected Post(Parcel in) {
        postId = in.readInt();
        usuarioPostId = in.readString();
        imagenUrl = in.readString();
        descripcion = in.readString();
        fecha = in.readLong();
        fileName = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(postId);
        dest.writeString(usuarioPostId);
        dest.writeString(imagenUrl);
        dest.writeString(descripcion);
        dest.writeLong(fecha);
        dest.writeString(fileName);
    }
}