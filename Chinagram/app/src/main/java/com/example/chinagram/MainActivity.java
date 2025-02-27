// Author: Álvaro Pereira
// Date: 24-02-2025

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
    private SharedPreferencesHelper prefsHelper; // Preferencias compartidas

    // Constantes para IDs de fragmentos
    private static final int ID_HOME_FRAGMENT = R.id.homeFragment;
    private static final int ID_LOGIN_FRAGMENT = R.id.loginFragment;
    private static final int ID_BUSCAR_FRAGMENT = R.id.buscarFragment;
    private static final int ID_SUBIR_FRAGMENT = R.id.subirFragment;
    private static final int ID_REELS_FRAGMENT = R.id.reelsFragment;
    private static final int ID_PERFIL_FRAGMENT = R.id.perfilFragment;
    private static final int ID_OPCIONES_FRAGMENT = R.id.opcionesFragment;
    private static final int ID_EDITAR_PERFIL_FRAGMENT = R.id.editarPerfilFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefsHelper = new SharedPreferencesHelper(this);
        int savedTheme = prefsHelper.recuperarTema();
        Log.d("MainActivity", "Cargando tema: " + (savedTheme == AppCompatDelegate.MODE_NIGHT_YES ? "Oscuro" : "Claro"));
        AppCompatDelegate.setDefaultNightMode(savedTheme); // Aplico el tema guardado por defecto

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Oculto inicialmente la toolbar y el bottom_nav
        binding.toolbar.setVisibility(View.GONE);
        binding.bottomNavigation.setVisibility(View.GONE);

        Log.d("MainActivity", "Color del ícono en BottomNavigation: " + binding.bottomNavigation.getItemIconTintList());
        // Configuro el NavController desde el NavHostFragment
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
            } else {
                throw new IllegalStateException("NavHostFragment no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish(); // Cierro la actividad si falla el starting de la app (aunque esto es una pijotad en verdad)
            return;
        }

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
                    currentFragmentId == ID_BUSCAR_FRAGMENT ||
                    currentFragmentId == ID_SUBIR_FRAGMENT ||
                    currentFragmentId == ID_REELS_FRAGMENT ||
                    currentFragmentId == ID_PERFIL_FRAGMENT ||
                    currentFragmentId == ID_OPCIONES_FRAGMENT ||
                    currentFragmentId == ID_EDITAR_PERFIL_FRAGMENT;

            // Muestro y oculto la toolbar y navegación según el fragment
            binding.toolbar.setVisibility(isMainFragment ? View.VISIBLE : View.GONE);
            binding.bottomNavigation.setVisibility(isMainFragment ? View.VISIBLE : View.GONE);

            // Limpio el menú de la toolbar antes de personalizarlo
            binding.toolbar.getMenu().clear();
            if (currentFragmentId == ID_PERFIL_FRAGMENT) {
                binding.toolbar.inflateMenu(R.menu.toolbar_menu);
                MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_menu);
                if (menuItem != null) {
                    int currentTheme = AppCompatDelegate.getDefaultNightMode();
                    Log.d("MainActivity", "Tema actual: " + (currentTheme == AppCompatDelegate.MODE_NIGHT_YES ? "Oscuro" : "Claro"));
                    int color;
                    if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
                        color = ContextCompat.getColor(this, android.R.color.background_dark); // Negro explícito en modo claro
                    } else {
                        color = ContextCompat.getColor(this, android.R.color.white); // Blanco explícito en modo oscuro
                    }
                    Log.d("MainActivity", "Color del ícono del menú antes de aplicar: " + color + " (Hex: #" + String.format("%06X", (0xFFFFFF & color)) + ")");
                    Drawable icon = menuItem.getIcon();
                    if (icon != null) {
                        icon.mutate().setTintList(ColorStateList.valueOf(color));
                        Log.d("MainActivity", "Color aplicado al ícono: " + color + " (Hex: #" + String.format("%06X", (0xFFFFFF & color)) + ")");
                    }
                    int size = (int) (24 * getResources().getDisplayMetrics().density);
                    menuItem.getIcon().setBounds(0, 0, size, size);
                }
                Log.d("MainActivity", "Menú mostrado para PerfilFragment. Ítems: " + binding.toolbar.getMenu().size());
            }

            // Actualizo el título de la toolbar según el fragmento actual
            if (currentFragmentId == ID_PERFIL_FRAGMENT) {
                binding.toolbar.setTitle("Perfil");
            } else if (currentFragmentId == ID_HOME_FRAGMENT) {
                binding.toolbar.setTitle("Para ti");
            } else if (currentFragmentId == ID_BUSCAR_FRAGMENT) {
                binding.toolbar.setTitle("Buscar");
            } else if (currentFragmentId == ID_SUBIR_FRAGMENT) {
                binding.toolbar.setTitle("Subir");
            } else if (currentFragmentId == ID_REELS_FRAGMENT) {
                binding.toolbar.setTitle("Reels");
            } else if (currentFragmentId == ID_OPCIONES_FRAGMENT) {
                binding.toolbar.setTitle("Opciones");
            } else if (currentFragmentId == ID_EDITAR_PERFIL_FRAGMENT) {
                binding.toolbar.setTitle("Editar perfil");
            }
        });

        // Listener para la selección de ítems en la barra de navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (!isNavigating) {
                isNavigating = true; // Evito clics múltiples para que no se reseteé la vista
                int currentFragmentId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentFragmentId == item.getItemId()) {
                    Log.d("MainActivity", "Ignorando navegación al mismo fragmento: " + currentFragmentId);
                    isNavigating = false;
                    return true;
                }


                // Transición de opciones -> perfil
                if (currentFragmentId == ID_OPCIONES_FRAGMENT && item.getItemId() == ID_PERFIL_FRAGMENT) {
                    navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
                } else if (fragmentOrder.containsKey(currentFragmentId) && fragmentOrder.containsKey(item.getItemId())) {
                    // Animaciones
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
                    navigateToFragment(item.getItemId()); // Navegación sin animaciones
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

        // Manejo del botón "atrás" (esto da mucho problemas, también es una pijotada)
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentFragmentId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentFragmentId == ID_LOGIN_FRAGMENT || currentFragmentId == ID_HOME_FRAGMENT) {
                    finish(); // Cierra la app si está en login o home (lógica natural de la app)
                } else if (currentFragmentId == ID_OPCIONES_FRAGMENT) {
                    navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
                } else if (currentFragmentId == R.id.editarPerfilFragment){
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
                        currentDestinationId != R.id.editarPerfilFragment && // Añadido
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

    // Cierro sesión y redirigo al login
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