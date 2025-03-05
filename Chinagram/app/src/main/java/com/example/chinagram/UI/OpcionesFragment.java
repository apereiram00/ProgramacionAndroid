package com.example.chinagram.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.example.chinagram.MainActivity;
import com.example.chinagram.Model.SharedPreferencesHelper;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentOpcionesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class OpcionesFragment extends Fragment {

    private FragmentOpcionesBinding binding;
    private int intentosEliminar = 0;
    private SharedPreferencesHelper prefsHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOpcionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializo SharedPreferencesHelper
        prefsHelper = new SharedPreferencesHelper(requireContext());

        // Configuro el estado inicial del Switch según el modo actual
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        binding.switchModo.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Listener para cambiar entre modo oscuro y claro
        binding.switchModo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Cambiar a modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefsHelper.guardarTema(AppCompatDelegate.MODE_NIGHT_YES); // Guardo modo oscuro
            } else {
                // Cambiar a modo claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefsHelper.guardarTema(AppCompatDelegate.MODE_NIGHT_NO); // Guardo modo claro
            }
            // Recreo la actividad para aplicar el cambio
            requireActivity().recreate();
        });

        // Fuerzo transparencia en los botones (en el xml no funciona)
        binding.cerrarSesionButton.setBackground(null);
        binding.eliminarCuentaButton.setBackground(null);
        binding.soporteButton.setBackground(null);

        // Listener para cerrar sesión
        binding.cerrarSesionButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    .setMessage("Camarada, ¿seguro que quieres irte? Aquí estás a salvo.")
                    .setPositiveButton("Sí", (d, which) -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).signOutUsuario();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Listener para eliminar cuenta
        binding.eliminarCuentaButton.setOnClickListener(v -> {
            intentosEliminar++;
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom);
            if (intentosEliminar <= 3) {
                String[] mensajes = {
                        "¿Y si solo tomas un descanso?",
                        "Piensa en lo que estás dejando atrás, camarada.",
                        "Última advertencia, camarada. ¿Estás seguro?"
                };
                builder.setMessage(mensajes[intentosEliminar - 1])
                        .setPositiveButton("Sí", (dialog, which) -> {})
                        .setNegativeButton("No", (dialog, which) -> intentosEliminar = 0)
                        .show();
            } else {
                builder.setMessage("Es un honor ser parte de Chinagram. ¿Seguro que quieres eliminar tu cuenta?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                user.delete()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d("OpcionesFragment", "Cuenta eliminada con éxito de Authentication");
                                                // Borrar datos en Firestore inmediatamente después
                                                borrarDatosUsuarioEnFirestore(userId);
                                                if (getActivity() instanceof MainActivity) {
                                                    NavOptions navOptions = new NavOptions.Builder()
                                                            .setPopUpTo(R.id.homeFragment, true)
                                                            .build();
                                                    ((MainActivity) getActivity()).getNavController()
                                                            .navigate(R.id.loginFragment, null, navOptions);
                                                }
                                            } else {
                                                Log.w("OpcionesFragment", "Error al eliminar cuenta de Authentication", task.getException());
                                                Toast.makeText(requireContext(), "Error al eliminar cuenta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            intentosEliminar = 0;
                                        });
                            } else {
                                Log.e("OpcionesFragment", "Usuario no autenticado");
                                Toast.makeText(requireContext(), "No hay usuario autenticado para eliminar", Toast.LENGTH_SHORT).show();
                                intentosEliminar = 0;
                            }
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
            new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    .setMessage(respuestas[random.nextInt(respuestas.length)])
                    .setPositiveButton("Entendido", null)
                    .show();
        });
    }

    private void borrarDatosUsuarioEnFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Datos del usuario borrados de Firestore"))
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al borrar datos del usuario: " + e.getMessage());
                    // Mostrar un Toast para depurar errores visibles
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error al borrar datos en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}