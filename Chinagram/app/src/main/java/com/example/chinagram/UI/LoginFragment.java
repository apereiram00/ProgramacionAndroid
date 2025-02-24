// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.example.chinagram.Model.SharedPreferencesHelper;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private int intentos_fallidos = 0;     // Contador de intentos fallidos de login
    private static final int max_intentos = 3; // Límite máximo de intentos fallidos antes de ocultar la imagen

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance(); // Inicializo FirebaseAuth

        // Configuro y registro el flujo de Google Sign-In
        configurarGoogleSignIn();
        registrarLauncherGoogleSignIn();

        // Verifico preferencias guardadas para "Recuérdame"
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
        if (sharedPreferencesHelper.recuerdameOn()) {
            String savedEmail = sharedPreferencesHelper.recuperarEmail();
            if (savedEmail != null) {
                binding.loginEmail.setText(savedEmail); // Relleno el campo de email
                binding.loginRememberMe.setChecked(true); // Marco el checkbox
            }
        }

        // Listener para el botón de login (imagen)
        binding.loginImage.setOnClickListener(view -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString().trim();

            // Valido que los campos no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                mostrarDialogo("Error", "Los campos no pueden estar vacíos.");
            } else {
                // Intento autenticar al usuario
                logarUsuario(email, password);
            }
        });

        // Listener para el botón de Google Sign-In
        binding.loginGoogleButton.setOnClickListener(view -> iniciarSignInConGoogle());

        // Listener para el botón de registro
        binding.registerButton.setOnClickListener(view -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString().trim();

            // Valido que los campos no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                mostrarDialogo("Error", "Los campos no pueden estar vacíos.");
            } else {
                // Intento registrar al usuario
                crearUsuario(email, password);
            }
        });

        return binding.getRoot(); // Devuelvo la vista raíz
    }

    // Configuro las opciones de Google Sign-In
    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ID del cliente web de Firebase
                .requestEmail() // Solicito el email del usuario
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    // Registro el launcher para manejar el resultado de Google Sign-In
    private void registrarLauncherGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                // Autentico con Firebase usando el token de Google
                                autenticarConFirebaseGoogle(account.getIdToken());
                            }
                        } catch (ApiException ignored) {
                            ignored.printStackTrace();
                            // (24-02-2025 || Está vacío aunque no hace falta rellenar nada)
                        }
                    }
                }
        );
    }

    // Inicio el flujo de Google Sign-In
    private void iniciarSignInConGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // Autentico con Firebase usando las credenciales de Google
    private void autenticarConFirebaseGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Vinculo la cuenta de Google a una cuenta existente
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            navegarAlHome();
                        }
                    });
        } else {
            // Inicio sesión con Google
            mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    navegarAlHome();
                }
            });
        }
    }

    // Creo un nuevo usuario con email y contraseña
    private void crearUsuario(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                mostrarDialogo("Registro", "¡Registrado en CHINAGRAM!");
            } else {
                mostrarDialogo("Error", "La cuenta ya existe o hubo un problema.");
            }
        });
    }

    // Inicio sesión con email y contraseña
    private void logarUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                intentos_fallidos = 0; // Reinicio el contador de intentos
                boolean recuerdame = binding.loginRememberMe.isChecked();
                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
                if (recuerdame) {
                    // Guardo el email y marca "Recuérdame"
                    sharedPreferencesHelper.guardarLogin(email, true);
                } else {
                    // Limpio las preferencias de login
                    sharedPreferencesHelper.limpiarLogin();
                }
                navegarAlHome();
            } else {
                intentos_fallidos++; // Incremento el contador
                if (intentos_fallidos < max_intentos) {
                    mostrarDialogo("Error", "Credenciales incorrectas.");
                }
                // Si se supera el límite, oculto la imagen
                if (intentos_fallidos >= max_intentos) {
                    binding.loginImage.setVisibility(View.INVISIBLE);
                    mostrarDialogo("Intentos fallidos", "Has alcanzado el número máximo de intentos fallidos.");
                }
            }
        });
    }

    // Navego al fragmento Home limpiando la pila de navegación
    private void navegarAlHome() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true) // Elimino el LoginFragment de la pila
                    .build();
            navController.navigate(R.id.action_loginFragment_to_homeFragment, null, navOptions);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // Manejo errores de navegación
        }
    }

    // Muestro un diálogo simple con título y mensaje
    private void mostrarDialogo(String titulo, String mensaje) {
        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}