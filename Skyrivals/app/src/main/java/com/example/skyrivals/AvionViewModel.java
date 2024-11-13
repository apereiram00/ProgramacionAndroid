package com.example.skyrivals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AvionViewModel extends ViewModel {
    private final MutableLiveData<List<Avion>> avionesLiveData = new MutableLiveData<>();
    private final List<Avion> aviones; // Variable de instancia

    public AvionViewModel() {
        // Inicializa la lista de aviones
        aviones = new ArrayList<>(); // Inicialización correcta de la variable de instancia

        // Aviones de la Luftwaffe
        aviones.add(new Avion("Bf 109E", R.drawable.bf109eperfil, R.drawable.bf109esim,600, 4, 7, "1937", "Caza de la Luftwaffe en la Segunda Guerra Mundial", "Alemania", "Caza"));
        aviones.add(new Avion("Bf 109F2", R.drawable.bf109f2perfil, R.drawable.bf109f2sim,650, 3, 6, "1941", "Versión mejorada del Bf 109E.", "Alemania", "Caza"));
        aviones.add(new Avion("Bf 109F4", R.drawable.bf109f4perfil, R.drawable.bf109f4sim,670, 3, 8, "1942", "Una de las variantes más exitosas.", "Alemania", "Caza"));
        aviones.add(new Avion("Bf 109G6", R.drawable.bf109g6perfil, R.drawable.bf109g6sim,700, 3, 7, "1943", "Un avión polivalente con gran versatilidad.", "Alemania", "Caza", "Interceptor"));
        aviones.add(new Avion("Bf 109K4", R.drawable.bf109k4perfil, R.drawable.bf109k4sim,750, 3, 9, "1944", "Última variante del Bf 109.", "Alemania", "Caza", "Interceptor"));
        aviones.add(new Avion("Fw 190D9", R.drawable.fw190d9perfil, R.drawable.fw190d9sim,710, 4, 6, "1944", "Caza de superioridad aérea.", "Alemania", "Interceptor", "Caza"));
        aviones.add(new Avion("Fw 190A8", R.drawable.fw190a8perfil, R.drawable.fw190a8sim,670, 4, 5, "1943", "Versión muy utilizada durante la guerra.", "Alemania", "Caza", "Interceptor"));

        // Aviones británicos
        aviones.add(new Avion("Hurricane Mk.II", R.drawable.hurricanemk2perfil, R.drawable.hurricanemk2sim,540, 8, 6, "1940", "Caza polivalente británico utilizado en la Batalla de Inglaterra.", "Inglaterra", "Interceptor"));
        aviones.add(new Avion("Spitfire Mk.XIV", R.drawable.spitfiremk14perfil, R.drawable.spitfiremk14sim,720, 4, 9, "1944", "Caza de superioridad aérea que jugó un papel crucial en la guerra.", "Inglaterra", "Caza"));
        aviones.add(new Avion("Spitfire Mk.XIVe", R.drawable.spitfiremk14eperfil, R.drawable.spitfiremk14esim,730, 4, 9, "1944", "Versión mejorada del Mk.XIV con un motor más potente.", "Inglaterra", "Caza"));
        aviones.add(new Avion("Tempest Mk.V", R.drawable.tempestmk5perfil, R.drawable.tempestmk5sim,750, 4, 8, "1944", "Caza de alta velocidad utilizado en misiones de interceptación.", "Inglaterra", "Interceptor", "Caza"));
        aviones.add(new Avion("Typhoon Mk.I", R.drawable.typhoonmk1bperfil, R.drawable.typhoonmk1bsim,650, 4, 7, "1941", "Caza diseñado para combatir bombarderos alemanes.", "Inglaterra", "Interceptor"));

        // Aviones estadounidenses
        aviones.add(new Avion("P-47 D.22", R.drawable.p47d22perfil, R.drawable.p47d22sim,650, 8, 6, "1943", "Caza pesado que se destacó en la Segunda Guerra Mundial.", "EEUU", "Caza"));
        aviones.add(new Avion("P-47 D.28", R.drawable.p47d28perfil, R.drawable.p47d28sim,700, 8, 7, "1944", "Versión mejorada del P-47 con mejor rendimiento en combate.", "EEUU", "Caza"));
        aviones.add(new Avion("P-51B", R.drawable.p51bperfil, R.drawable.p51bsim,720, 6, 8, "1943", "Caza de largo alcance que se convirtió en el principal interceptor de la USAF.", "EEUU", "Caza"));
        aviones.add(new Avion("P-51D", R.drawable.p51dperfil, R.drawable.p51dsim,730, 6, 8, "1944", "Mejoras en el rendimiento y armamento hicieron de este avión un favorito en combate.", "EEUU", "Caza"));
        aviones.add(new Avion("P-38 J.25", R.drawable.p38j25perfil, R.drawable.p38j25sim,660, 4, 7, "1943", "Caza interceptor bimotor conocido por su velocidad y maniobrabilidad.", "EEUU", "Interceptor", "Caza"));

        // Aviones soviéticos
        aviones.add(new Avion("I-16", R.drawable.i16perfil, R.drawable.i16sim,560, 4, 5, "1933", "Caza monoplano soviético que tuvo un impacto significativo en la Guerra Civil Española.", "URSS", "Caza"));
        aviones.add(new Avion("La-5F ser.3", R.drawable.la5fser38perfil, R.drawable.la5fser38sim,600, 6, 8, "1944", "Uno de los cazas más eficaces de la Segunda Guerra Mundial, usado extensamente en el Frente Oriental.", "URSS", "Interceptor"));
        aviones.add(new Avion("La-5 ser.8", R.drawable.la5ser8perfil, R.drawable.la5ser8sim,615, 6, 8, "1944", "Caza que mejoró las capacidades de la serie anterior, con gran maniobrabilidad.", "URSS", "Interceptor", "Caza"));
        aviones.add(new Avion("Yak-1 ser.69", R.drawable.yak1ser69perfil, R.drawable.yak1ser69sim,590, 6, 6, "1941", "Caza de combate que destacó en el inicio de la guerra.", "URSS", "Caza"));
        aviones.add(new Avion("Yak-7B ser.36", R.drawable.yak7bser36perfil, R.drawable.yak7bser36sim,540, 4, 6, "1942", "Versión mejorada del Yak-7, usado tanto en roles de caza como de ataque.", "URSS", "Caza"));
        aviones.add(new Avion("Yak-9", R.drawable.yak9perfil, R.drawable.yak9sim,650, 4, 7, "1943", "Uno de los cazas más populares del ejército rojo, conocido por su eficacia en combate.", "URSS", "Caza"));

        // Asigna la lista de aviones al LiveData
        avionesLiveData.setValue(aviones);
    }

    public LiveData<List<Avion>> getAviones() {
        return avionesLiveData;
    }

    public LiveData<List<Avion>> getAvionesLuftwaffe() {
        return filterAviones("Alemania");
    }

    public LiveData<List<Avion>> getAvionesUsaaf() {
        return filterAviones("EEUU");
    }

    public LiveData<List<Avion>> getAvionesRaf() {
        return filterAviones("Inglaterra");
    }

    public LiveData<List<Avion>> getAvionesUssr() {
        return filterAviones("URSS");
    }

    private LiveData<List<Avion>> filterAviones(String pais) {
        MutableLiveData<List<Avion>> filteredListLiveData = new MutableLiveData<>();
        List<Avion> filteredList = new ArrayList<>();

        // Filtrar la lista de aviones según el país
        for (Avion avion : aviones) {
            if (avion.getPais().equals(pais)) {
                filteredList.add(avion);
            }
        }
        filteredListLiveData.setValue(filteredList);
        return filteredListLiveData;
    }
}
