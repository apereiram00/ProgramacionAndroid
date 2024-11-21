package com.example.todolist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.todolist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private TareasViewModel tareasViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(binding.listaFragmentsolo.getId());
        navController = navHostFragment.getNavController();

        tareasViewModel = new ViewModelProvider(this).get(TareasViewModel.class);
        tareasViewModel.getListaTareas().observe(this, tareas -> {
            int cantidadTareas = (tareas != null) ? tareas.size() : 0;
            setTitle("Listado de tareas (" + cantidadTareas + ")");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
