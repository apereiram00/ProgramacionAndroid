// com.example.chinagram.MainActivity.java
package com.example.chinagram;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.chinagram.Model.SharedPreferencesHelper;
import com.example.chinagram.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;
    private boolean isNavigating = false; // Flag para evitar múltiples navegaciones simultáneas
    private Map<Integer, Integer> fragmentOrder; // Mapa para asignar un orden a los fragmentos principales para animaciones

    // Constantes para IDs de fragmentos
    private static final int ID_HOME_FRAGMENT = R.id.homeFragment;
    private static final int ID_LOGIN_FRAGMENT = R.id.loginFragment;
    private static final int ID_BUSCAR_FRAGMENT = R.id.buscarFragment;
    private static final int ID_SUBIR_FRAGMENT = R.id.subirFragment;
    private static final int ID_REELS_FRAGMENT = R.id.reelsFragment;
    private static final int ID_PERFIL_FRAGMENT = R.id.perfilFragment;
    private static final int ID_OPCIONES_FRAGMENT = R.id.opcionesFragment;
    private static final int ID_EDITAR_PERFIL_FRAGMENT = R.id.editarPerfilFragment;
    private static final int ID_DETALLES_POST_FRAGMENT = R.id.detallesPostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicializo SharedPreferencesHelper y aplico el tema guardado con AppCompatDelegate
        // Preferencias compartidas
        SharedPreferencesHelper prefsHelper = new SharedPreferencesHelper(this);
        int savedThemeMode = prefsHelper.recuperarTema();
        AppCompatDelegate.setDefaultNightMode(savedThemeMode); // Aplico el modo guardado (MODE_NIGHT_YES, MODE_NIGHT_NO)
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Oculto inicialmente la toolbar y el bottom_nav
        binding.toolbar.setVisibility(View.GONE);
        binding.bottomNavigation.setVisibility(View.GONE);

        // Configuro el NavController desde el NavHostFragment
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
            } else {
                throw new IllegalStateException("NavHostFragment no encontrado");
            }
        } catch (Exception e) {
            finish(); // Cierro la actividad si falla el inicio
            return;
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.bottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background));
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_background));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_background));
        }

        // Ajustar el tinte de los íconos según el modo actual
        int tintColor = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ?
                ContextCompat.getColor(this, android.R.color.white) :
                ContextCompat.getColor(this, android.R.color.black);
        binding.bottomNavigation.setItemIconTintList(ColorStateList.valueOf(tintColor));

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Defino el orden de los fragmentos para animaciones de navegación
        fragmentOrder = new HashMap<>();
        fragmentOrder.put(ID_HOME_FRAGMENT, 0);
        fragmentOrder.put(ID_BUSCAR_FRAGMENT, 1);
        fragmentOrder.put(ID_SUBIR_FRAGMENT, 2);
        fragmentOrder.put(ID_REELS_FRAGMENT, 3);
        fragmentOrder.put(ID_PERFIL_FRAGMENT, 4);

        // Listener para cambios de destino en la navegación
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int currentFragmentId = destination.getId();
            Log.d("MainActivity", "Cambiando a fragmento: " + currentFragmentId);
            binding.bottomNavigation.setSelectedItemId(currentFragmentId);

            // Determino si el fragmento actual es uno de los principales
            boolean isMainFragment = currentFragmentId == ID_HOME_FRAGMENT ||
                    currentFragmentId == ID_BUSCAR_FRAGMENT || // Incluimos BuscarFragment como principal para mantener bottomNavigation
                    currentFragmentId == ID_SUBIR_FRAGMENT ||
                    currentFragmentId == ID_REELS_FRAGMENT ||
                    currentFragmentId == ID_PERFIL_FRAGMENT ||
                    currentFragmentId == ID_OPCIONES_FRAGMENT ||
                    currentFragmentId == ID_EDITAR_PERFIL_FRAGMENT ||
                    currentFragmentId == ID_DETALLES_POST_FRAGMENT;

            // Muestro y oculto la toolbar y navegación según el fragmento
            binding.toolbar.setVisibility(isMainFragment && currentFragmentId != ID_BUSCAR_FRAGMENT ? View.VISIBLE : View.GONE);
            binding.bottomNavigation.setVisibility(isMainFragment ? View.VISIBLE : View.GONE);

            // Limpio el menú de la toolbar antes de personalizarlo
            binding.toolbar.getMenu().clear();
            if (currentFragmentId == ID_PERFIL_FRAGMENT) {
                binding.toolbar.inflateMenu(R.menu.toolbar_menu);
                MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_menu);
                if (menuItem != null) {
                    // Obtener el color basado en el modo actual
                    int color = getResources().getColor(
                            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ?
                                    android.R.color.white : android.R.color.black, getTheme());
                    Drawable icon = menuItem.getIcon();
                    if (icon != null) {
                        icon.mutate().setTintList(ColorStateList.valueOf(color));
                    }
                }
            }

            // Actualizo el título de la toolbar según el fragmento actual
            if (currentFragmentId == ID_PERFIL_FRAGMENT) {
                binding.toolbar.setTitle("Perfil");
            } else if (currentFragmentId == ID_HOME_FRAGMENT) {
                binding.toolbar.setTitle("Para ti");
            } else if (currentFragmentId == ID_SUBIR_FRAGMENT) {
                binding.toolbar.setTitle("Subir");
            } else if (currentFragmentId == ID_REELS_FRAGMENT) {
                binding.toolbar.setTitle("Reels");
            } else if (currentFragmentId == ID_OPCIONES_FRAGMENT) {
                binding.toolbar.setTitle("Opciones");
            } else if (currentFragmentId == ID_EDITAR_PERFIL_FRAGMENT) {
                binding.toolbar.setTitle("Editar perfil");
            } else if (currentFragmentId == ID_DETALLES_POST_FRAGMENT) {
                binding.toolbar.setTitle("Publicaciones");
            }
        });

        // Listener para la selección de ítems en la barra de navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (!isNavigating) {
                isNavigating = true;
                int currentFragmentId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentFragmentId == item.getItemId()) {
                    Log.d("MainActivity", "Ignorando navegación al mismo fragmento: " + currentFragmentId);
                    isNavigating = false;
                    return true;
                }

                // Resto de la lógica de navegación...
                if (currentFragmentId == ID_OPCIONES_FRAGMENT && item.getItemId() == ID_PERFIL_FRAGMENT) {
                    navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
                } else if (fragmentOrder.containsKey(currentFragmentId) && fragmentOrder.containsKey(item.getItemId())) {
                    int currentOrder = fragmentOrder.get(currentFragmentId);
                    int targetOrder = fragmentOrder.get(item.getItemId());
                    NavOptions navOptions = new NavOptions.Builder()
                            .setEnterAnim(targetOrder > currentOrder ? R.anim.slide_in_left : R.anim.slide_in_right)
                            .setExitAnim(targetOrder > currentOrder ? R.anim.slide_out_right : R.anim.slide_out_left)
                            .setPopEnterAnim(targetOrder > currentOrder ? R.anim.slide_in_right : R.anim.slide_in_left)
                            .setPopExitAnim(targetOrder > currentOrder ? R.anim.slide_out_left : R.anim.slide_out_right)
                            .build();
                    navigateToFragment(item.getItemId(), navOptions);
                } else {
                    navigateToFragment(item.getItemId());
                }
                isNavigating = false;
            }
            return true;
        });

        // Listener para clics en el menú de la toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {
            Log.d("MainActivity", "Clic en ítem del menú: " + item.getItemId());
            if (item.getItemId() == R.id.action_menu) {
                navController.navigate(R.id.action_perfilFragment_to_opcionesFragment);
                return true;
            }
            return false;
        });

        // Verifico el estado del usuario al iniciar
        comprobarUsuario();

        // Manejo del botón "atrás"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentFragmentId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentFragmentId == ID_LOGIN_FRAGMENT || currentFragmentId == ID_HOME_FRAGMENT) {
                    finish(); // Cierra la app si está en login o home
                } else if (currentFragmentId == ID_OPCIONES_FRAGMENT) {
                    navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
                } else if (currentFragmentId == R.id.editarPerfilFragment) {
                    navController.popBackStack(R.id.perfilFragment, false); // Vuelvo a PerfilFragment
                } else if (currentFragmentId == ID_PERFIL_FRAGMENT ||
                        currentFragmentId == ID_BUSCAR_FRAGMENT ||
                        currentFragmentId == ID_SUBIR_FRAGMENT ||
                        currentFragmentId == ID_REELS_FRAGMENT) {
                    // Navego a Home con animaciones
                    NavOptions navOptions = new NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build();
                    navController.navigate(ID_HOME_FRAGMENT, null, navOptions);
                } else {
                    if (!isNavigating && navController.getCurrentDestination() != null) {
                        navController.popBackStack(); // Retrocedo en la pila de navegación
                    } else {
                        finish(); // Cierro si no hay más para retroceder
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // Verifico si hay un usuario autenticado y navega al fragmento adecuado
    private void comprobarUsuario() {
        try {
            FirebaseUser usuarioActual = mAuth.getCurrentUser();
            int currentDestinationId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;

            if (usuarioActual == null) {
                if (currentDestinationId != ID_LOGIN_FRAGMENT) {
                    navController.navigate(ID_LOGIN_FRAGMENT);
                }
            } else {
                // Solo navega a Home si no estás en un fragmento válido
                if (currentDestinationId != ID_HOME_FRAGMENT &&
                        currentDestinationId != ID_PERFIL_FRAGMENT &&
                        currentDestinationId != ID_OPCIONES_FRAGMENT &&
                        currentDestinationId != R.id.editarPerfilFragment &&
                        currentDestinationId != ID_BUSCAR_FRAGMENT &&
                        currentDestinationId != ID_SUBIR_FRAGMENT &&
                        currentDestinationId != ID_REELS_FRAGMENT) {
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(ID_LOGIN_FRAGMENT, true)
                            .setLaunchSingleTop(true)
                            .build();
                    navController.navigate(ID_HOME_FRAGMENT, null, navOptions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método sobrecargado para navegar a un fragmento con opciones personalizadas
    private void navigateToFragment(int itemId, NavOptions navOptions) {
        try {
            if (itemId == ID_HOME_FRAGMENT) {
                navController.navigate(ID_HOME_FRAGMENT, null, navOptions);
            } else if (itemId == ID_BUSCAR_FRAGMENT) {
                navController.navigate(ID_BUSCAR_FRAGMENT, null, navOptions);
            } else if (itemId == ID_SUBIR_FRAGMENT) {
                navController.navigate(ID_SUBIR_FRAGMENT, null, navOptions);
            } else if (itemId == ID_REELS_FRAGMENT) {
                navController.navigate(ID_REELS_FRAGMENT, null, navOptions);
            } else if (itemId == ID_PERFIL_FRAGMENT) {
                navController.navigate(ID_PERFIL_FRAGMENT, null, navOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navegación simple sin opciones adicionales
    private void navigateToFragment(int itemId) {
        try {
            if (itemId == ID_HOME_FRAGMENT) {
                navController.navigate(ID_HOME_FRAGMENT);
            } else if (itemId == ID_BUSCAR_FRAGMENT) {
                navController.navigate(ID_BUSCAR_FRAGMENT);
            } else if (itemId == ID_SUBIR_FRAGMENT) {
                navController.navigate(ID_SUBIR_FRAGMENT);
            } else if (itemId == ID_REELS_FRAGMENT) {
                navController.navigate(ID_REELS_FRAGMENT);
            } else if (itemId == ID_PERFIL_FRAGMENT) {
                navController.navigate(ID_PERFIL_FRAGMENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cierro sesión y redirijo al login
    public void signOutUsuario() {
        try {
            mAuth.signOut();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(ID_HOME_FRAGMENT, true)
                    .build();
            navController.navigate(ID_LOGIN_FRAGMENT, null, navOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getter público para el NavController (útil para otros componentes)
    public NavController getNavController() {
        return navController;
    }

    @Override
    protected void onStart() {
        super.onStart();
        comprobarUsuario(); // Reviso el estado del usuario al volver a la actividad
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Libero la referencia al binding para evitar memory leaks
    }
}