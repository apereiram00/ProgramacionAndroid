package com.example.as7room.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tabla_tareas")
public class Tarea implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String descripcion;
    private long fecha;
    private byte[] imagen;
    private boolean completada;
    private String autor;

    public Tarea(String nombre, String descripcion, long fecha, byte[] imagen, String autor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.imagen = imagen;
        this.autor = autor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public long getFecha() {
        return fecha;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public void setAutor(String autor) { this.autor = autor; }

    public String getAutor() { return autor; }

}
