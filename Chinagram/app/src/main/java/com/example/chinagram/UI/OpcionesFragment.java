package com.example.chinagram.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.chinagram.Model.UsuarioRepositorio;
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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOpcionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializo SharedPreferencesHelper
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usuarioRepositorio = new UsuarioRepositorio(requireContext());
        prefsHelper = new SharedPreferencesHelper(requireContext());

        // Configuro el estado inicial del Switch según el modo actual
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        binding.switchModo.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Listener para cambiar entre modo oscuro y claro
        binding.switchModo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Cambio a modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefsHelper.guardarTema(AppCompatDelegate.MODE_NIGHT_YES); // Guardo modo oscuro
            } else {
                // Cambio a modo claro
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
            if (isAdded()) {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                        .setMessage("Camarada, ¿seguro que quieres irte? Aquí estás a salvo.")
                        .setPositiveButton("Sí", (d, which) -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).cerrarSesionUsuario();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // Listener para eliminar cuenta
        binding.eliminarCuentaButton.setOnClickListener(v -> {
            intentosEliminar++;
            if (!isAdded()) {
                Log.w("OpcionesFragment", "Fragmento no adjunto, cancelando acción");
                return;
            }

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
                        .setPositiveButton("Sí", (dialog, which) -> eliminarUsuario())
                        .setNegativeButton("No", (dialog, which) -> intentosEliminar = 0)
                        .show();
            }
        });

        // Listener para el botón de soporte
        binding.soporteButton.setOnClickListener(v -> {
            if (isAdded()) {
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
            }
        });
    }

    private void eliminarUsuario() {
        if (!isAdded()) {
            Log.w("OpcionesFragment", "Fragmento no adjunto, cancelando eliminación");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("OpcionesFragment", "Usuario no autenticado");
            if (isAdded()) {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                        .setMessage("No hay usuario autenticado para eliminar")
                        .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            intentosEliminar = 0;
            return;
        }

        String userId = user.getUid();
        // Elimino de Firestore
        db.collection("usuarios").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) {
                        Log.w("OpcionesFragment", "Fragmento no adjunto tras eliminar de Firestore");
                        return;
                    }
                    Log.d("OpcionesFragment", "Datos del usuario eliminados de Firestore");

                    // Elimino de Room
                    usuarioRepositorio.deleteUser(userId);

                    // Elimino de Authentication
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (!isAdded()) {
                                    Log.w("OpcionesFragment", "Fragmento no adjunto tras eliminar de Authentication");
                                    return;
                                }
                                if (task.isSuccessful()) {
                                    Log.d("OpcionesFragment", "Cuenta eliminada con éxito de Authentication");
                                    if (isAdded()) {
                                        // Navegar al LoginFragment después de cerrar el diálogo
                                        NavController navController = Navigation.findNavController(requireView());
                                        NavOptions navOptions = new NavOptions.Builder()
                                                .setPopUpTo(R.id.homeFragment, true)
                                                .build();
                                        try {
                                            navController.navigate(R.id.loginFragment, null, navOptions);
                                        } catch (IllegalArgumentException e) {
                                            Log.e("OpcionesFragment", "Error al navegar: " + e.getMessage());
                                        }
                                    }
                                } else {
                                    Log.w("OpcionesFragment", "Error al eliminar cuenta de Authentication", task.getException());
                                    if (isAdded()) {
                                        new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                                                .setMessage("Error al eliminar cuenta: " + task.getException().getMessage())
                                                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                                                .show();
                                    }
                                }
                                intentosEliminar = 0;
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Log.e("OpcionesFragment", "Error al eliminar datos de Firestore: " + e.getMessage());
                        new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                                .setMessage("Error al eliminar datos: " + e.getMessage())
                                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                                .show();
                        intentosEliminar = 0;
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}