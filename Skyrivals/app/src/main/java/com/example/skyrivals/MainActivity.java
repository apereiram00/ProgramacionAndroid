package com.example.skyrivals;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.skyrivals.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private FavoritosViewModel favoritosViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // Inicializa el ViewModel
        favoritosViewModel = new ViewModelProvider(this).get(FavoritosViewModel.class);

        // Obtiene el NavHostFragment y verifica que no sea nulo
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Configura el BottomNavigationView con el NavController
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_aviones) {
                navController.navigate(R.id.avionesFragment);
                return true;
            } else if (itemId == R.id.nav_avionesFav) {
                navController.navigate(R.id.favoritosFragment);
                return true;
            } else if (itemId == R.id.nav_combate) {
                navController.navigate(R.id.combateFragment);
                return true;
            }
            return false;
        });

        // Listener para actualizar la Toolbar y el icono según el fragmento actual
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.avionesFragment) {
                binding.toolbar.setTitle("Aviones");
                binding.toolbarIcon.setImageResource(R.drawable.aviones);
            } else if (destination.getId() == R.id.favoritosFragment) {
                binding.toolbar.setTitle("Favoritos");
                binding.toolbarIcon.setImageResource(R.drawable.fav);
            } else if (destination.getId() == R.id.combateFragment) {
                binding.toolbar.setTitle("Combate");
                binding.toolbarIcon.setImageResource(R.drawable.combate);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
