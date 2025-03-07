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
    private boolean estaNavegando = false; // Flag para evitar múltiples navegaciones simultáneas (antes: isNavigating)
    private Map<Integer, Integer> ordenFragmentos; // Mapa para asignar un orden a los fragmentos (antes: fragmentOrder)

    // Constantes para IDs de fragmentos (sin cambios, ya son descriptivas)
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
        // Inicializo SharedPreferencesHelper y aplico el tema guardado
        SharedPreferencesHelper prefsHelper = new SharedPreferencesHelper(this);
        int modoTemaGuardado = prefsHelper.recuperarTema(); // Renombrado: savedThemeMode -> modoTemaGuardado
        AppCompatDelegate.setDefaultNightMode(modoTemaGuardado);
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
            finish();
            return;
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.bottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background));
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_background));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_background));
        }

        // Ajustar el tinte de los íconos según el modo actual
        int colorTinte = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ?
                ContextCompat.getColor(this, android.R.color.white) :
                ContextCompat.getColor(this, android.R.color.black); // Renombrado: tintColor -> colorTinte
        binding.bottomNavigation.setItemIconTintList(ColorStateList.valueOf(colorTinte));

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Defino el orden de los fragmentos para animaciones de navegación
        ordenFragmentos = new HashMap<>();
        ordenFragmentos.put(ID_HOME_FRAGMENT, 0);
        ordenFragmentos.put(ID_BUSCAR_FRAGMENT, 1);
        ordenFragmentos.put(ID_SUBIR_FRAGMENT, 2);
        ordenFragmentos.put(ID_REELS_FRAGMENT, 3);
        ordenFragmentos.put(ID_PERFIL_FRAGMENT, 4);

        // Listener para cambios de destino en la navegación
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int idFragmentoActual = destination.getId(); // Renombrado: currentFragmentId -> idFragmentoActual
            Log.d("MainActivity", "Cambiando a fragmento: " + idFragmentoActual);
            binding.bottomNavigation.setSelectedItemId(idFragmentoActual);

            // Determino si el fragmento actual es uno de los principales
            boolean esFragmentoPrincipal = idFragmentoActual == ID_HOME_FRAGMENT ||
                    idFragmentoActual == ID_BUSCAR_FRAGMENT ||
                    idFragmentoActual == ID_SUBIR_FRAGMENT ||
                    idFragmentoActual == ID_REELS_FRAGMENT ||
                    idFragmentoActual == ID_PERFIL_FRAGMENT ||
                    idFragmentoActual == ID_OPCIONES_FRAGMENT ||
                    idFragmentoActual == ID_EDITAR_PERFIL_FRAGMENT ||
                    idFragmentoActual == ID_DETALLES_POST_FRAGMENT; // Renombrado: isMainFragment -> esFragmentoPrincipal

            // Muestro y oculto la toolbar y navegación según el fragmento
            binding.toolbar.setVisibility(esFragmentoPrincipal && idFragmentoActual != ID_BUSCAR_FRAGMENT ? View.VISIBLE : View.GONE);
            binding.bottomNavigation.setVisibility(esFragmentoPrincipal ? View.VISIBLE : View.GONE);

            // Limpio el menú de la toolbar antes de personalizarlo
            binding.toolbar.getMenu().clear();
            if (idFragmentoActual == ID_PERFIL_FRAGMENT) {
                binding.toolbar.inflateMenu(R.menu.toolbar_menu);
                MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_menu);
                if (menuItem != null) {
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
            actualizarTituloToolbar(idFragmentoActual); // Nueva función extraída
        });

        // Listener para la selección de ítems en la barra de navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (!estaNavegando) {
                estaNavegando = true;
                int idFragmentoActual = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (idFragmentoActual == item.getItemId()) {
                    Log.d("MainActivity", "Ignorando navegación al mismo fragmento: " + idFragmentoActual);
                    estaNavegando = false;
                    return true;
                }

                if (idFragmentoActual == ID_OPCIONES_FRAGMENT && item.getItemId() == ID_PERFIL_FRAGMENT) {
                    navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
                } else if (ordenFragmentos.containsKey(idFragmentoActual) && ordenFragmentos.containsKey(item.getItemId())) {
                    int ordenActual = ordenFragmentos.get(idFragmentoActual); // Renombrado: currentOrder -> ordenActual
                    int ordenDestino = ordenFragmentos.get(item.getItemId()); // Renombrado: targetOrder -> ordenDestino
                    NavOptions navOptions = new NavOptions.Builder()
                            .setEnterAnim(ordenDestino > ordenActual ? R.anim.slide_in_left : R.anim.slide_in_right)
                            .setExitAnim(ordenDestino > ordenActual ? R.anim.slide_out_right : R.anim.slide_out_left)
                            .setPopEnterAnim(ordenDestino > ordenActual ? R.anim.slide_in_right : R.anim.slide_in_left)
                            .setPopExitAnim(ordenDestino > ordenActual ? R.anim.slide_out_left : R.anim.slide_out_right)
                            .build();
                    navegarAFragmento(item.getItemId(), navOptions);
                } else {
                    navegarAFragmento(item.getItemId());
                }
                estaNavegando = false;
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
                manejarBotonAtras(); // Nueva función extraída
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // Verifico si hay un usuario autenticado y navega al fragmento adecuado
    private void comprobarUsuario() {
        try {
            FirebaseUser usuarioActual = mAuth.getCurrentUser(); // Renombrado: currentUser -> usuarioActual
            int idDestinoActual = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1; // Renombrado: currentDestinationId -> idDestinoActual

            if (usuarioActual == null) {
                if (idDestinoActual != ID_LOGIN_FRAGMENT) {
                    navController.navigate(ID_LOGIN_FRAGMENT);
                }
            } else {
                if (idDestinoActual != ID_HOME_FRAGMENT &&
                        idDestinoActual != ID_PERFIL_FRAGMENT &&
                        idDestinoActual != ID_OPCIONES_FRAGMENT &&
                        idDestinoActual != R.id.editarPerfilFragment &&
                        idDestinoActual != ID_BUSCAR_FRAGMENT &&
                        idDestinoActual != ID_SUBIR_FRAGMENT &&
                        idDestinoActual != ID_REELS_FRAGMENT) {
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

    // Método para actualizar el título de la toolbar según el fragmento
    private void actualizarTituloToolbar(int idFragmentoActual) {
        if (idFragmentoActual == ID_PERFIL_FRAGMENT) {
            binding.toolbar.setTitle("Perfil");
        } else if (idFragmentoActual == ID_HOME_FRAGMENT) {
            binding.toolbar.setTitle("Para ti");
        } else if (idFragmentoActual == ID_SUBIR_FRAGMENT) {
            binding.toolbar.setTitle("Subir");
        } else if (idFragmentoActual == ID_REELS_FRAGMENT) {
            binding.toolbar.setTitle("Reels");
        } else if (idFragmentoActual == ID_OPCIONES_FRAGMENT) {
            binding.toolbar.setTitle("Opciones");
        } else if (idFragmentoActual == ID_EDITAR_PERFIL_FRAGMENT) {
            binding.toolbar.setTitle("Editar perfil");
        } else if (idFragmentoActual == ID_DETALLES_POST_FRAGMENT) {
            binding.toolbar.setTitle("Publicaciones");
        }
    }

    // Método para manejar el botón "atrás"
    private void manejarBotonAtras() {
        int idFragmentoActual = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
        if (idFragmentoActual == ID_LOGIN_FRAGMENT || idFragmentoActual == ID_HOME_FRAGMENT) {
            finish();
        } else if (idFragmentoActual == ID_OPCIONES_FRAGMENT) {
            navController.navigate(R.id.action_opcionesFragment_to_perfilFragment);
        } else if (idFragmentoActual == R.id.editarPerfilFragment) {
            navController.popBackStack(R.id.perfilFragment, false);
        } else if (idFragmentoActual == ID_PERFIL_FRAGMENT ||
                idFragmentoActual == ID_BUSCAR_FRAGMENT ||
                idFragmentoActual == ID_SUBIR_FRAGMENT ||
                idFragmentoActual == ID_REELS_FRAGMENT) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(ID_HOME_FRAGMENT, null, navOptions);
        } else {
            if (!estaNavegando && navController.getCurrentDestination() != null) {
                navController.popBackStack();
            } else {
                finish();
            }
        }
    }

    // Método sobrecargado para navegar a un fragmento con opciones personalizadas
    private void navegarAFragmento(int itemId, NavOptions navOptions) {
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
    private void navegarAFragmento(int itemId) {
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
    public void cerrarSesionUsuario() { // Renombrado: signOutUsuario -> cerrarSesionUsuario
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

    // Getter público para el NavController
    public NavController obtenerNavController() { // Renombrado: getNavController -> obtenerNavController
        return navController;
    }

    @Override
    protected void onStart() {
        super.onStart();
        comprobarUsuario();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}