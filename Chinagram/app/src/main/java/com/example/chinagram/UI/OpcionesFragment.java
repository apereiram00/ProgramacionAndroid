// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;

import com.example.chinagram.MainActivity;
import com.example.chinagram.Model.SharedPreferencesHelper;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentOpcionesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class OpcionesFragment extends Fragment {

    private FragmentOpcionesBinding binding;
    private int intentosEliminar = 0; // Contador de intentos para eliminar la cuenta
    private SharedPreferencesHelper prefsHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOpcionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa SharedPreferencesHelper y configuro el switch de tema
        prefsHelper = new SharedPreferencesHelper(requireContext());
        int savedTheme = prefsHelper.recuperarTema();
        binding.switchModo.setChecked(savedTheme == AppCompatDelegate.MODE_NIGHT_YES);

        // Listener para cambiar entre modo oscuro y claro
        binding.switchModo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newTheme = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            prefsHelper.guardarTema(newTheme); // Guardo el tema en preferencias
            AppCompatDelegate.setDefaultNightMode(newTheme); // Aplico el tema
            new AlertDialog.Builder(requireContext())
                    .setMessage(isChecked ? "Modo Oscuro activado, camarada." : "Modo Claro activado, camarada.")
                    .setPositiveButton("Entendido", null)
                    .show();
        });

        // Listener para cerrar sesión
        binding.cerrarSesionButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setMessage("Camarada, ¿seguro que quieres irte? Aquí estás a salvo.")
                    .setPositiveButton("Sí", (d, which) -> {
                        if (getActivity() instanceof MainActivity) {
                            // Llama al método del MainActivity para cerrar sesión
                            ((MainActivity) getActivity()).signOutUsuario();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Listener para eliminar cuenta con lógica de intentos
        binding.eliminarCuentaButton.setOnClickListener(v -> {
            intentosEliminar++;
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            if (intentosEliminar <= 3) {
                // Mensajes progresivos para disuadir al usuario
                String[] mensajes = {
                        "¿Y si solo tomas un descanso?",
                        "Piensa en lo que estás dejando atrás, camarada.",
                        "Última advertencia, camarada. ¿Estás seguro?"
                };
                builder.setMessage(mensajes[intentosEliminar - 1])
                        .setPositiveButton("Sí", (dialog, which) -> {
                            if (intentosEliminar < 3) {
                                moverDialogo((AlertDialog) dialog, intentosEliminar); // Muevo el diálogo
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> intentosEliminar = 0) // Reinicio el contador
                        .show();
            } else {
                // Confirmación final para eliminar la cuenta
                builder.setMessage("Es un honor ser parte de Chinagram. ¿Seguro que quieres eliminar tu cuenta?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                user.delete()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d("OpcionesFragment", "Cuenta eliminada con éxito"); // Log de depuración del proceso
                                                if (getActivity() instanceof MainActivity) {
                                                    NavOptions navOptions = new NavOptions.Builder()
                                                            .setPopUpTo(R.id.homeFragment, true) // Limpio la pila
                                                            .build();
                                                    ((MainActivity) getActivity()).getNavController()
                                                            .navigate(R.id.loginFragment, null, navOptions);
                                                }
                                            } else {
                                                Log.w("OpcionesFragment", "Error al eliminar cuenta", task.getException()); // Log de depuración del proceso
                                            }
                                        });
                            }
                            intentosEliminar = 0; // Reinicio el contador
                        })
                        .setNegativeButton("No", (dialog, which) -> intentosEliminar = 0)
                        .show();
            }
        });

        // Listener para el botón de soporte
        binding.soporteButton.setOnClickListener(v -> {
            String[] respuestas = {
                    "Chinagram nunca falla, revisa tu conexión.",
                    "Respira hondo, todo está bien.",
                    "Confía en el sistema."
            };
            Random random = new Random();
            new AlertDialog.Builder(requireContext())
                    .setMessage(respuestas[random.nextInt(respuestas.length)])
                    .setPositiveButton("Entendido", null)
                    .show();
        });
    }

    // Muevo el diálogo en la pantalla según el intento
    private void moverDialogo(AlertDialog dialog, int intento) {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            switch (intento) {
                case 1:
                    params.x = 100;  // Esquina superior derecha
                    params.y = -200;
                    break;
                case 2:
                    params.x = -100; // Esquina superior izquierda
                    params.y = -200;
                    break;
                case 3:
                    params.x = 100;  // Centro-derecha
                    params.y = 0;
                    break;
            }
            window.setAttributes(params); // Aplico la nueva posición
        }
    }

    // Libero el binding al destruir la vista para evitar memory leaks
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}