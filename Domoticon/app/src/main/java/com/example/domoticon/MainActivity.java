package com.example.domoticon;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import com.example.domoticon.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener los elementos del array
        String[] numBombillas = getResources().getStringArray(R.array.num_bombillas);

        // Crear el adaptador personalizado
        SpinnerAmarillo adapter = new SpinnerAmarillo(this, numBombillas);

        // Establecer el adaptador en el spinner
        binding.spinner.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Liberar el binding
    }
}
