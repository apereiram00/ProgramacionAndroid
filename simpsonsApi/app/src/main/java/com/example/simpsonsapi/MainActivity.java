package com.example.simpsonsapi;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.simpsonsapi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar); // Establezco la toolbar como la action bar de la actividad
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Con esto desactivo la visualización del título de la action bar para configurarlo manualmente

        // Obtengo el NavHostFragment que es el contenedor para la navegación de fragmentos
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        binding.toolbar.setTitle("Búsqueda");  // Establezco el título inicial de la toolbar (que siempre será el primer fragment al que accedo)

        // Añado un listener sencillo para escuchar y cambiar el titulo de la toolbar
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.busquedaFragment) {
                binding.toolbar.setTitle("Búsqueda");
            } else if (destination.getId() == R.id.listadoFragment) {
                binding.toolbar.setTitle("Listado");
            }
        });
    }
}
