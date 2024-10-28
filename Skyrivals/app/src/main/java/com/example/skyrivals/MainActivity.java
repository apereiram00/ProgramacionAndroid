package com.example.skyrivals;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.skyrivals.databinding.ActivityMainBinding;
import com.example.skyrivals.databinding.FragmentInicioBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.toolbar, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.avionesFragment) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });
    }

    public void toolBarActiva(boolean isVisible) {
        if (isVisible) {
            binding.toolbar.setVisibility(View.VISIBLE);
        } else {
            binding.toolbar.setVisibility(View.GONE);
        }
    }

    public void toolbarImagen(int imageResource, String title) {
        binding.toolbarImage.setImageResource(imageResource);
        binding.toolbarTitle.setText(title);
    }
}