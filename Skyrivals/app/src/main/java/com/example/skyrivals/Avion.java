package com.example.skyrivals;

import java.util.Arrays;
import java.util.List;

public class Avion {
    private String nombre;
    private int imagen;
    private int imagen2;
    private List<String> tipos;
    private boolean favorito;
    private int velocidadMaxima; // Cambiado a int para cálculos
    private int numeroArmas;
    private int maniobrabilidad; // Nuevo atributo
    private String fechaConstruccion;
    private String descripcionHistorica;
    private String pais;

    // Modificación del constructor para incluir maniobrabilidad
    public Avion(String nombre, int imagen, int imagen2, int velocidadMaxima, int numeroArmas, int maniobrabilidad, String fechaConstruccion, String descripcionHistorica, String pais, String... tipos) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.imagen2 = imagen2;
        this.velocidadMaxima = velocidadMaxima;
        this.numeroArmas = numeroArmas;
        this.maniobrabilidad = maniobrabilidad; // Inicializar maniobrabilidad
        this.fechaConstruccion = fechaConstruccion;
        this.descripcionHistorica = descripcionHistorica;
        this.tipos = Arrays.asList(tipos);
        this.favorito = false; // Por defecto no es favorito
        this.pais = pais;
    }

    @Override
    public String toString() {
        return nombre; // Devuelve el nombre del avión
    }

    // Getters existentes
    public String getNombre() {
        return nombre;
    }

    public int getImagen() {
        return imagen;
    }

    public int getImagen2() {
        return imagen2;
    }

    public List<String> getTipos() {
        return tipos;
    }

    public boolean isFavorito() {
        return favorito; // Getter para saber si el avión es favorito
    }

    public void setFavorito(boolean favorito) {
        if (favorito) {
            this.favorito = true;
        }
    }

    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }

    public int getNumeroArmas() {
        return numeroArmas;
    }

    public int getManiobrabilidad() { // Nuevo método getter
        return maniobrabilidad;
    }

    public String getFechaConstruccion() {
        return fechaConstruccion;
    }

    public String getDescripcionHistorica() {
        return descripcionHistorica;
    }

    public String getPais() {
        return pais;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avion avion = (Avion) o;
        return nombre.equals(avion.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }
}
