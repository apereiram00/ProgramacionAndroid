package com.example.chinagram.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.chinagram.Model.PerfilViewModel;
import com.example.chinagram.Model.UsuarioRepositorio;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentEditarPerfilBinding;
import com.example.chinagram.utils.DrawableUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class EditarPerfilFragment extends Fragment {

    private FragmentEditarPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String idUsuario; // Renombrado: userId -> idUsuario
    private ActivityResultLauncher<Intent> lanzadorSeleccionImagen; // Renombrado: pickImageLauncher -> lanzadorSeleccionImagen
    private Uri uriImagenSeleccionada; // Renombrado: selectedImageUri -> uriImagenSeleccionada

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idUsuario = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        binding.aceptarButton.setBackground(null);
        binding.cambiarFotoButton.setBackground(null);

        perfilViewModel.getUsuario(idUsuario).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                binding.nombreEditText.setText(usuario.nombre);
                binding.bioEditText.setText(usuario.biografia);
                Glide.with(this)
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? obtenerRecursoDesdeDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);
            }
        });

        lanzadorSeleccionImagen = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                uriImagenSeleccionada = result.getData().getData();
                if (uriImagenSeleccionada != null) {
                    Log.d("EditarPerfilFragment", "Foto seleccionada (vista previa): " + uriImagenSeleccionada);
                    Glide.with(this).load(uriImagenSeleccionada).into(binding.fotoPerfilImageView);
                }
            }
        });

        binding.cambiarFotoButton.setOnClickListener(v -> abrirGaleria()); // Renombrado: lambda -> abrirGaleria
        binding.aceptarButton.setOnClickListener(v -> guardarCambiosPerfil()); // Renombrado: lambda -> guardarCambiosPerfil
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        lanzadorSeleccionImagen.launch(intent);
    }

    private void guardarCambiosPerfil() {
        String nuevoNombre = binding.nombreEditText.getText().toString().trim();
        String nuevaBio = binding.bioEditText.getText().toString().trim();
        if (!nuevoNombre.isEmpty()) {
            NavController navController = Navigation.findNavController(binding.getRoot());
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.aceptarButton.setEnabled(false);

            if (uriImagenSeleccionada != null) {
                perfilViewModel.uploadProfileImage(uriImagenSeleccionada, idUsuario, new UsuarioRepositorio.UpdateCallback() {
                    @Override
                    public void onUpdateComplete() {
                        actualizarPerfil(navController, nuevoNombre, nuevaBio);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("EditarPerfilFragment", "Error al subir imagen: " + error);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.aceptarButton.setEnabled(true);
                    }
                });
            } else {
                actualizarPerfil(navController, nuevoNombre, nuevaBio);
            }
        } else {
            Log.w("EditarPerfilFragment", "El nombre no puede estar vacío");
        }
    }

    private void actualizarPerfil(NavController navController, String nuevoNombre, String nuevaBio) {
        perfilViewModel.updateProfile(idUsuario, nuevoNombre, nuevaBio, new UsuarioRepositorio.UpdateCallback() {
            @Override
            public void onUpdateComplete() {
                navController.popBackStack(R.id.perfilFragment, false);
                binding.progressBar.setVisibility(View.GONE);
                binding.aceptarButton.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                Log.e("EditarPerfilFragment", "Error al actualizar perfil: " + error);
                binding.progressBar.setVisibility(View.GONE);
                binding.aceptarButton.setEnabled(true);
            }
        });
    }

    private int obtenerRecursoDesdeDrawable(String urlDrawable) { // Renombrado: getResourceFromDrawable -> obtenerRecursoDesdeDrawable
        return DrawableUtils.obtenerRecursoDesdeDrawable(urlDrawable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}