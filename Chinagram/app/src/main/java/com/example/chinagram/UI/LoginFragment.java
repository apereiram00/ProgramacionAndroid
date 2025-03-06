// com.example.chinagram.UI.LoginFragment.java
package com.example.chinagram.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.chinagram.Model.Usuario;
import com.example.chinagram.Model.UsuarioRepositorio;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentLoginBinding;
import com.example.chinagram.utils.DrawableUtils;
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
    private int intentos_fallidos = 0;
    private static final int max_intentos = 3;
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        usuarioRepositorio = new UsuarioRepositorio(requireContext());

        configurarGoogleSignIn();
        registrarLauncherGoogleSignIn();

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
        if (sharedPreferencesHelper.recuerdameOn()) {
            String savedEmail = sharedPreferencesHelper.recuperarEmail();
            if (savedEmail != null) {
                binding.loginEmail.setText(savedEmail);
                binding.loginRememberMe.setChecked(true);
            }
        }

        // Listener para el botón de login (imagen) - Funciona como "puerta"
        binding.loginImage.setOnClickListener(view -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                mostrarDialogo("Error", "Los campos no pueden estar vacíos.");
            } else {
                logarUsuario(email, password);
            }
        });

        binding.loginGoogleButton.setOnClickListener(view -> iniciarSignInConGoogle());

        binding.registerButton.setOnClickListener(view -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                mostrarDialogo("Error", "Los campos no pueden estar vacíos.");
            } else {
                crearUsuario(email, password);
            }
        });

        return binding.getRoot();
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

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
                                autenticarConFirebaseGoogle(account.getIdToken());
                            }
                        } catch (ApiException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }
        );
    }

    private void iniciarSignInConGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void autenticarConFirebaseGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            navegarAlHome();
                        }
                    });
        } else {
            mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    navegarAlHome();
                }
            });
        }
    }

    private void crearUsuario(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String username = extraerNombreUsuario(email);
                    String fotoUrl = DrawableUtils.getRandomDrawableUrl();
                    Usuario nuevoUsuario = new Usuario(user.getUid(), username, "Explorador del mundo", fotoUrl, 0, 0, 0);
                    usuarioRepositorio.ensureUserExistsAndSync(user.getUid(), nuevoUsuario, new UsuarioRepositorio.UpdateCallback() {
                        @Override
                        public void onUpdateComplete() {
                            requireActivity().runOnUiThread(() -> {
                                mostrarDialogo("Registro", "¡Registrado en CHINAGRAM!");
                            });
                        }
                        @Override
                        public void onError(String error) {
                            requireActivity().runOnUiThread(() -> mostrarDialogo("Error", "Fallo al sincronizar: " + error));
                        }
                    });
                }
            } else {
                mostrarDialogo("Error", "La cuenta ya existe o hubo un problema.");
            }
        });
    }

    private void logarUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                intentos_fallidos = 0; // Reinicio el contador de intentos
                binding.loginImage.setVisibility(View.VISIBLE); // Aseguro que la imagen sea visible tras un login exitoso
                boolean recuerdame = binding.loginRememberMe.isChecked();
                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
                if (recuerdame) {
                    sharedPreferencesHelper.guardarLogin(email, true);
                } else {
                    sharedPreferencesHelper.limpiarLogin();
                }
                navegarAlHome();
            } else {
                intentos_fallidos++;
                if (intentos_fallidos < max_intentos) {
                    mostrarDialogo("Error", "Credenciales incorrectas.");
                }
                if (intentos_fallidos >= max_intentos) {
                    binding.loginImage.setVisibility(View.INVISIBLE); // Oculto la imagen ("puerta")
                    mostrarDialogo("Intentos fallidos", "Has alcanzado el número máximo de intentos fallidos.");
                }
            }
        });
    }

    private void navegarAlHome() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build();
            navController.navigate(R.id.action_loginFragment_to_homeFragment, null, navOptions);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String extraerNombreUsuario(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            return email.substring(0, atIndex);
        }
        return email;
    }
}