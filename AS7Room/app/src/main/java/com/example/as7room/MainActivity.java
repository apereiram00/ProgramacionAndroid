package com.example.as7room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.as7room.UI.FragmentListaTareas;
import com.example.as7room.UI.RegistroActivity;
import com.example.as7room.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_SHOW_COMPLETED = "show_completed";
    private SharedPreferences sharedPreferences;
    private boolean showCompleted = false;

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        configurarGoogleSignIn();

        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) {
            volverRegistro();
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        showCompleted = sharedPreferences.getBoolean(PREF_SHOW_COMPLETED, false);
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.listaFragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_listado) {
                navController.navigate(R.id.fragmentListaTareas);
                return true;
            } else if (item.getItemId() == R.id.nav_busqueda) {
                navController.navigate(R.id.fragmentBusqueda);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            navController.navigate(R.id.fragmentListaTareas);
        }
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signOutUsuario() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                volverRegistro(); // Solo aquí
            } else {
                Toast.makeText(MainActivity.this, "Error al cerrar sesión de Google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void volverRegistro() {
        Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem showCompletedItem = menu.findItem(R.id.action_show_completed);
        showCompletedItem.setChecked(showCompleted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_completed) {
            showCompleted = !item.isChecked();
            item.setChecked(showCompleted);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_SHOW_COMPLETED, showCompleted);
            editor.apply();

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.listaFragment);
            if (currentFragment instanceof FragmentListaTareas) {
                ((FragmentListaTareas) currentFragment).actualizarFiltroTareas(showCompleted);
            }
            return true;
        }
        else if (id == R.id.action_logout) {
            signOutUsuario();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
