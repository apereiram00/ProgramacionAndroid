// com.example.chinagram.UI.EditarPerfilFragment.java
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
import com.example.chinagram.Model.Usuario;
import com.example.chinagram.Model.UsuarioRepositorio;
import com.example.chinagram.R;
import com.example.chinagram.databinding.FragmentEditarPerfilBinding;
import com.example.chinagram.utils.DrawableUtils;
import com.google.firebase.auth.FirebaseAuth;

public class EditarPerfilFragment extends Fragment {

    private FragmentEditarPerfilBinding binding;
    private PerfilViewModel perfilViewModel;
    private String userId;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri; // Variable para almacenar temporalmente la Uri de la imagen seleccionada

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        perfilViewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        binding.aceptarButton.setBackground(null);
        binding.cambiarFotoButton.setBackground(null);

        perfilViewModel.getUsuario(userId).observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                binding.nombreEditText.setText(usuario.nombre);
                binding.bioEditText.setText(usuario.biografia);
                Glide.with(this)
                        .load(usuario.fotoPerfilUrl.startsWith("drawable://") ? getResourceFromDrawable(usuario.fotoPerfilUrl) : usuario.fotoPerfilUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.fotoPerfilImageView);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Log.d("EditarPerfilFragment", "Foto seleccionada (vista previa): " + selectedImageUri);
                    Glide.with(this).load(selectedImageUri).into(binding.fotoPerfilImageView);
                }
            }
        });

        binding.cambiarFotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        binding.aceptarButton.setOnClickListener(v -> {
            String nuevoNombre = binding.nombreEditText.getText().toString().trim();
            String nuevaBio = binding.bioEditText.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                NavController navController = Navigation.findNavController(v);
                binding.progressBar.setVisibility(View.VISIBLE); // Muestro ProgressBar
                binding.aceptarButton.setEnabled(false); // Deshabilito botón para evitar clics múltiples

                if (selectedImageUri != null) {
                    perfilViewModel.uploadProfileImage(selectedImageUri, userId, new UsuarioRepositorio.UpdateCallback() {
                        @Override
                        public void onUpdateComplete() {
                            perfilViewModel.updateProfile(userId, nuevoNombre, nuevaBio, new UsuarioRepositorio.UpdateCallback() {
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

                        @Override
                        public void onError(String error) {
                            Log.e("EditarPerfilFragment", "Error al subir imagen: " + error);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.aceptarButton.setEnabled(true);
                        }
                    });
                } else {
                    perfilViewModel.updateProfile(userId, nuevoNombre, nuevaBio, new UsuarioRepositorio.UpdateCallback() {
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
            } else {
                Log.w("EditarPerfilFragment", "El nombre no puede estar vacío");
            }
        });
    }

    private int getResourceFromDrawable(String drawableUrl) {
        return DrawableUtils.getResourceFromDrawable(drawableUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}