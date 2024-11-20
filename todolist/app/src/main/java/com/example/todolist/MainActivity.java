package com.example.todolist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.todolist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding; // ViewBinding
    private TareasViewModel tareasViewModel; // ViewModel compartido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);

        // Inicializar ViewModel
        tareasViewModel = new ViewModelProvider(this).get(TareasViewModel.class);

        // Observar cambios en la lista de tareas
        tareasViewModel.getListaTareas().observe(this, tareas -> {
            int cantidadTareas = (tareas != null) ? tareas.size() : 0;
            binding.toolbar.setTitle("Listado de tareas (" + cantidadTareas + ")");
        });
    }
}
