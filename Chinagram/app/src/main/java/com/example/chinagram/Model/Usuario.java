package com.example.chinagram.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class Usuario {
    @PrimaryKey
    @NonNull
    public String usuarioId;
    public String nombre;
    public String biografia;
    public String fotoPerfilUrl;
    public int seguidores;
    public int siguiendo;

    // Constructor sin argumentos (no-args constructor) requerido por Firestore
    public Usuario() {
    }

    // Constructor con parámetros (mantén el que ya tienes)
    public Usuario(String usuarioId, String nombre, String biografia, String fotoPerfilUrl, int seguidores, int siguiendo) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.biografia = biografia;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.seguidores = seguidores;
        this.siguiendo = siguiendo;
    }

    // Getters y setters (opcionales, pero recomendados para Firestore y Room)
    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public int getSiguiendo() {
        return siguiendo;
    }

    public void setSiguiendo(int siguiendo) {
        this.siguiendo = siguiendo;
    }
}