package com.example.skyrivals;

public class Simulador {

    public static String simular(Avion avion1, Avion avion2) {
        int puntuacionAvion1 = avion1.getVelocidadMaxima() + avion1.getNumeroArmas() + avion1.getManiobrabilidad();
        int puntuacionAvion2 = avion2.getVelocidadMaxima() + avion2.getNumeroArmas() + avion2.getManiobrabilidad();

        if (puntuacionAvion1 > puntuacionAvion2) {
            return "Ganador: " + avion1.getNombre();
        } else if (puntuacionAvion2 > puntuacionAvion1) {
            return "Ganador: " + avion2.getNombre();
        } else {
            return "Empate entre " + avion1.getNombre() + " y " + avion2.getNombre();
        }
    }
}
