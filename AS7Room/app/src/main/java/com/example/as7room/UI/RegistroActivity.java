package com.example.as7room.UI;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.as7room.MainActivity;
import com.example.as7room.R;
import com.example.as7room.databinding.FragmentRegistroBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

public class RegistroActivity extends AppCompatActivity {

    private FragmentRegistroBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        configurarGoogleSignIn();
        registrarLauncherGoogleSignIn();
        binding.btnLogin.setOnClickListener(view -> logarUsuario());
        binding.btnRegistro.setOnClickListener(view -> crearUsuario());
        binding.btnGoogleSignIn.setOnClickListener(view -> iniciarSignInConGoogle());
        binding.btnGitHubSignIn.setOnClickListener(view -> iniciarSesionConGitHub());
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void registrarLauncherGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                autenticarConFirebaseGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(RegistroActivity.this,
                                    "Error al iniciar con Google: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistroActivity.this,
                                "Inicio de sesión con Google cancelado.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void iniciarSignInConGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void iniciarSesionConGitHub() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                Toast.makeText(this, "Bienvenido con GitHub: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                iniciarMainActivity();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al autenticar con GitHub: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            mAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        Toast.makeText(this, "Inicio exitoso con GitHub: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        iniciarMainActivity();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al iniciar sesión con GitHub: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void autenticarConFirebaseGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(RegistroActivity.this,
                                    "Cuenta de Google vinculada a la cuenta de Firebase.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    "Error al vincular la cuenta de Google: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser usuario = mAuth.getCurrentUser();
                            if (usuario != null) {
                                Toast.makeText(RegistroActivity.this,
                                        "Bienvenido con Google: " + usuario.getEmail(),
                                        Toast.LENGTH_SHORT).show();
                            }
                            iniciarMainActivity();
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    "Error al autenticar con Firebase: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void crearUsuario() {
        String email = binding.textoMail.getText().toString().trim();
        String password = binding.textoPassword.getText().toString().trim();
        String password2 = binding.textoPassword2.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(password2)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = mAuth.getCurrentUser();
                        if (usuario != null) {
                            Toast.makeText(RegistroActivity.this,
                                    "Bienvenido, nuevo usuario: " + usuario.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        iniciarMainActivity();
                    } else {
                        Toast.makeText(RegistroActivity.this,
                                "Error al crear usuario: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logarUsuario() {
        String email = binding.textoMail.getText().toString().trim();
        String password = binding.textoPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = mAuth.getCurrentUser();
                        if (usuario != null) {
                            Toast.makeText(RegistroActivity.this,
                                    "Bienvenido de nuevo: " + usuario.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        iniciarMainActivity();
                    } else {
                        Toast.makeText(RegistroActivity.this,
                                "Error al iniciar sesión: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void iniciarMainActivity() {
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual != null) {
            iniciarMainActivity();
        }
    }
}